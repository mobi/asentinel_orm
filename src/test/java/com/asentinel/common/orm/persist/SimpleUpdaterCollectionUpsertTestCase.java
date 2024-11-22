package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcOperations;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCallback;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCreator;

/**
 * FYI: this test does not cover the cleanup behavior of the callbacks
 * used for collection upserts. That is tested by the {@link SimpleUpdaterCollectionTestCase}.
 * 
 * @author Razvan Popian
 */
public class SimpleUpdaterCollectionUpsertTestCase {
	private static final String UPSERT_AUTO_ID_NO_SEQ = "insert into A(name, age, insertable) values(?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?";
	private static final String UPSERT_AUTO_ID_WITH_SEQ = "insert into B(aId, name, age, insertable) values(nextval('seq'), ?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?";
	private static final String UPSERT_INSERT = "insert into A(aId, name, age, insertable) values(?, ?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?, aId = ?";
	
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	private final JdbcOperations jdbcOps = mock(JdbcOperations.class);
	private final SqlQuery ex = mock(SqlQuery.class);
	
	private final NewRowOnUpsertDetector newRowOnUpsertDetector = mock(NewRowOnUpsertDetector.class);
	{
		// we configure the mock so that any upserted entity is considered new
		when(newRowOnUpsertDetector.getColumns()).thenReturn(new String[0]);
		when(newRowOnUpsertDetector.isNewRow(anyMap())).thenReturn(true);
	}
	
	private final SimpleUpdater u = new SimpleUpdater(jdbcFlavor, ex);
	
	private final Connection conn = mock(Connection.class);
	private final PreparedStatement ps = mock(PreparedStatement.class);
	private final ResultSet rs = mock(ResultSet.class);
	private final ResultSetMetaData rsmd = mock(ResultSetMetaData.class);

	private final ArgumentCaptor<CustomBatchPreparedStatementCreator> cCreator = ArgumentCaptor.forClass(CustomBatchPreparedStatementCreator.class);
	private final ArgumentCaptor<CustomBatchPreparedStatementCallback> cCallback = ArgumentCaptor.forClass(CustomBatchPreparedStatementCallback.class);
	
	@Before
	public void setup() throws SQLException {
		when(ex.getJdbcOperations()).thenReturn(jdbcOps);
		when(conn.prepareStatement(anyString(), any(String[].class))).thenReturn(ps);
		when(ps.getGeneratedKeys()).thenReturn(rs);
		
		when(rs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(1);
		when(rsmd.getColumnLabel(1)).thenReturn("aId");	
		
		when(rs.next()).thenReturn(true, true, false);
		when(rs.getObject(1)).thenReturn(new BigDecimal(1), new BigDecimal(2));
	}
	
	private List<A> upsert_insertAutoId_NoSeq() throws SQLException {
		A a0 = new A(0, "test1", 10, 100, 1000);
		A a1 = new A(0, "test2", 20, 200, 2000);
		List<A> entities = Arrays.asList(a0, a1);
		when(jdbcOps.execute(cCreator.capture(), cCallback.capture())).thenReturn(new int[] {1,1});
		
		u.upsert(entities, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		assertEquals(UPSERT_AUTO_ID_NO_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cCreator.getValue().getSql().replaceAll("\\s", "")
				.toLowerCase());
		
		// CustomBatchPreparedStatementCreator lifecycle simulation
		cCreator.getValue().createPreparedStatement(conn);
		
		assertEquals(Arrays.asList(
				a0.name, a0.age, a0.insertable, a0.name, a0.age, a0.updatable,
				a1.name, a1.age, a1.insertable, a1.name, a1.age, a1.updatable
			), cCreator.getValue().getAllArgs());
		
		// CustomBatchPreparedStatementCallback lifecycle simulation
		cCallback.getValue().doInPreparedStatement(ps);
		
		assertEquals(1, entities.get(0).getId());
		assertEquals(2, entities.get(1).getId());
		return entities;
	}
	
	/**
	 * Besides the SQL string and the setting of the entities ids, this test
	 * validates that with a {@link NewRowOnUpsertDetector} injected the
	 * {@link NewRowOnUpsertAware#setNewRow(boolean)} method is called for each
	 * upserted entity.
	 */
	@Test
	public void upsert_insertAutoId_NoSeq_With_NewRowDetector() throws SQLException {
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		upsert_insertAutoId_NoSeq().forEach(e -> assertTrue(e.isNewRow()));
	}	

	/**
	 * Besides the SQL string and the setting of the entities ids, this test
	 * validates that without a {@link NewRowOnUpsertDetector} injected the
	 * {@link NewRowOnUpsertAware#setNewRow(boolean)} method is not called.
	 */
	@Test
	public void upsert_insertAutoId_NoSeq_Without_NewRowDetector() throws SQLException {
		upsert_insertAutoId_NoSeq().forEach(e -> assertFalse(e.isNewRow()));
	}	

	@Test
	public void upsert_insertAutoId_WithSeq() throws SQLException {
		B b0 = new B(0, "test1", 10, 100, 1000);
		B b1 = new B(0, "test2", 20, 200, 2000);
		List<B> entities = Arrays.asList(b0, b1);
		when(jdbcOps.execute(cCreator.capture(), cCallback.capture())).thenReturn(new int[] {1,1});
		
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		u.upsert(entities, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		assertEquals(UPSERT_AUTO_ID_WITH_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cCreator.getValue().getSql().replaceAll("\\s", "")
				.toLowerCase());
		
		// CustomBatchPreparedStatementCreator lifecycle simulation
		cCreator.getValue().createPreparedStatement(conn);
		
		assertEquals(Arrays.asList(
				b0.name, b0.age, b0.insertable, b0.name, b0.age, b0.updatable,
				b1.name, b1.age, b1.insertable, b1.name, b1.age, b1.updatable
				), cCreator.getValue().getAllArgs());
		
		// CustomBatchPreparedStatementCallback lifecycle simulation
		cCallback.getValue().doInPreparedStatement(ps);
		
		assertEquals(1, entities.get(0).getId());
		assertTrue(entities.get(0).isNewRow());
		assertEquals(2, entities.get(1).getId());
		assertTrue(entities.get(1).isNewRow());
	}

	@Test
	public void upsert_insert() throws SQLException {
		A a0 = new A(11, "test1", 10, 100, 1000);
		A a1 = new A(22, "test2", 20, 200, 2000);
		List<A> entities = Arrays.asList(a0, a1);
		when(jdbcOps.execute(cCreator.capture(), cCallback.capture())).thenReturn(new int[] {1,1});
		
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		u.upsert(entities, UpdateType.INSERT, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		assertEquals(UPSERT_INSERT.replaceAll("\\s", "").toLowerCase(), 
				cCreator.getValue().getSql().replaceAll("\\s", "")
				.toLowerCase());
		
		// CustomBatchPreparedStatementCreator lifecycle simulation
		cCreator.getValue().createPreparedStatement(conn);		
		
		assertEquals(Arrays.asList(
				a0.id, a0.name, a0.age, a0.insertable, a0.name, a0.age, a0.updatable, a0.id,
				a1.id, a1.name, a1.age, a1.insertable, a1.name, a1.age, a1.updatable, a1.id
				), cCreator.getValue().getAllArgs());
		
		// CustomBatchPreparedStatementCallback lifecycle simulation
		cCallback.getValue().doInPreparedStatement(ps);
	
		// we don't set a new id for the update type insert, we only set it for insert auto id
		assertEquals(11, entities.get(0).getId());
		assertTrue(entities.get(0).isNewRow());
		assertEquals(22, entities.get(1).getId());
		assertTrue(entities.get(1).isNewRow());
	}


	
	@Table("A")
	private static class A implements NewRowOnUpsertAware {
		int id;
		
		public int getId() {
			return id;
		}
		
		@PkColumn("aId")
		public void setId(int id) {
			this.id = id;
		}
		
		@Column("name")
		String name;
		
		@Column("age")
		int age;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;			
		
		transient boolean newRow = false;
		
		@SuppressWarnings("unused")
		public A() {
			
		}
		
		public A(int id, String name, int age, int insertable, int updatable) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.insertable = insertable;
			this.updatable = updatable;
		}

		@Override
		public void setNewRow(boolean newRow) {
			this.newRow = newRow;
		}
		
		public boolean isNewRow() {
			return newRow;
		}

	}

	@Table("B")
	private static class B implements NewRowOnUpsertAware {
		int id;
		
		public int getId() {
			return id;
		}
		
		@PkColumn(value = "aId", sequence = "seq")
		public void setId(int id) {
			this.id = id;
		}
		
		@Column("name")
		String name;
		
		@Column("age")
		int age;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;	
		
		transient boolean newRow = false;
		
		public B(int id, String name, int age, int insertable, int updatable) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.insertable = insertable;
			this.updatable = updatable;
		}
		
		@Override
		public void setNewRow(boolean newRow) {
			this.newRow = newRow;
		}
		
		public boolean isNewRow() {
			return newRow;
		}


	}
	
}
