package com.asentinel.common.orm.persist;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.mappers.dynamic.DefaultDynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

public class SimpleUpdaterUpsertTestCase {
	private final static Logger log = LoggerFactory.getLogger(SimpleUpdaterUpsertTestCase.class);
	
	private final static String UPSERT_AUTO_ID_NO_SEQ = "insert into A(name, age, insertable, dynamicField) values(?, ?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?, dynamicField = ?";
	private final static String UPSERT_AUTO_ID_WITH_SEQ = "insert into B(aId, name, age, insertable, dynamicField) values(nextval('seq'), ?, ?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?, dynamicField = ?";
	private final static String UPSERT_INSERT = "insert into A(aId, name, age, insertable, dynamicField) values(?, ?, ?, ?, ?) on conflict (name) do update set name = ?, age = ?, updatable = ?, dynamicField = ?, aId = ?";
	
	private final static Number ID = 10;

	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private final SqlQuery ex = mock(SqlQuery.class);
	private final NewRowOnUpsertDetector newRowOnUpsertDetector = mock(NewRowOnUpsertDetector.class);
	{
		// we configure the mock so that any upserted entity is considered new
		when(newRowOnUpsertDetector.getColumns()).thenReturn(new String[0]);
		when(newRowOnUpsertDetector.isNewRow(anyMap())).thenReturn(true);
	}
	
	private final SimpleUpdater u = new SimpleUpdater(jdbcFlavor, ex);
	
	Set<DynamicColumn> dynamicColumns = singleton(new DefaultDynamicColumn("dynamicField", Integer.class));	
	
	
	private final ArgumentCaptor<String> cSql = ArgumentCaptor.forClass(String.class);
	private final ArgumentCaptor<String[]> cIdNames = ArgumentCaptor.forClass(String[].class);
	private final ArgumentCaptor<KeyHolder> cKeyHolder = ArgumentCaptor.forClass(KeyHolder.class);
	private final ArgumentCaptor<Object[]> cParams= ArgumentCaptor.forClass(Object[].class);
	
	@Before
	public void setup() {
		when(ex.update(cSql.capture(), cIdNames.capture(), cKeyHolder.capture(), cParams.capture()))
		.then(new Answer<Integer>() {

			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				for (Object arg: args) {
					if (arg instanceof GeneratedKeyHolder) {
						GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
						List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
						generatedKeys.add(new HashMap<String, Object>());
						generatedKeys.get(0).put("aId", ID);
						return 1;
					}
				}
				fail("Can not find KeyHolder argument.");
				// make the compiler happy, return null
				return null;
			}
		});
	}

	private A upsert_insertAutoId_NoSeq() {
		A a = new A(0, "test", 20, 30, 40, 50);
		
		int rows = u.upsert(a, new UpdateSettings<DynamicColumn>(dynamicColumns), 
				PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		log.debug("upsert_insertAutoId_NoSeq: " + cSql.getValue());
		
		assertEquals(UPSERT_AUTO_ID_NO_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase());
		assertEquals(Arrays.asList(
					a.name, a.age, a.insertable, a.dynamicField, 
					a.name, a.age, a.updatable, a.dynamicField
				), cParams.getAllValues());
		
		assertEquals(ID, a.getId());
		assertEquals(1, rows);
		return a;
	}

	/**
	 * Besides the SQL string and the setting of the entity id, this test validates
	 * that with a {@link NewRowOnUpsertDetector} injected the
	 * {@link NewRowOnUpsertAware#setNewRow(boolean)} method is called.
	 */
	@Test
	public void upsert_insertAutoId_NoSeq_With_NewRowDetector() {
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		A a = upsert_insertAutoId_NoSeq();
		assertTrue(a.isNewRow());
	}

	/**
	 * Besides the SQL string and the setting of the entity id, this test validates
	 * that without {@link NewRowOnUpsertDetector} injected the
	 * {@link NewRowOnUpsertAware#setNewRow(boolean)} method is not called.
	 */
	@Test
	public void upsert_insertAutoId_NoSeq_Without_NewRowDetector() {
		A a = upsert_insertAutoId_NoSeq();
		assertFalse(a.isNewRow());
	}
	
	@Test
	public void upsert_insertAutoId_WithSeq() {
		B b = new B(0, "test", 20, 30, 50, 60);
		
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		int rows = u.upsert(b, new UpdateSettings<DynamicColumn>(dynamicColumns), 
				PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		log.debug("upsert_insertAutoId_WithSeq: " + cSql.getValue());
		
		assertEquals(UPSERT_AUTO_ID_WITH_SEQ.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase());
		assertEquals(Arrays.asList(
				b.name, b.age, b.insertable, b.dynamicField, 
				b.name, b.age, b.updatable, b.dynamicField
			), cParams.getAllValues());
		
		assertEquals(ID, b.getId());
		assertTrue(b.isNewRow());
		assertEquals(1, rows);
	}
	
	
	@Test
	public void upsert_insert() {
		A a = new A(0, "test", 20, 30, 50, 60);
		
		u.setNewRowOnUpsertDetector(newRowOnUpsertDetector);
		int rows = u.upsert(a, new UpdateSettings<DynamicColumn>(UpdateType.INSERT, dynamicColumns), PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
		
		log.debug("upsert_insert: " + cSql.getValue());
		
		assertEquals(UPSERT_INSERT.replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase());
		assertEquals(Arrays.asList(
					a.id, a.name, a.age, a.insertable, a.dynamicField, 
					a.name, a.age, a.updatable, a.dynamicField, a.id
				), cParams.getAllValues());
		
		// we don't set a new id for the update type insert, we only set it for insert auto id
		assertEquals(0, a.getId());
		assertTrue(a.isNewRow());
		assertEquals(1, rows);
	}
	
	
	@Test
	public void upsert_preconditions_not_loaded_proxy() {
		A a = ProxyFactory.getInstance().newProxy(A.class, id -> new A());
		assertEquals(0, u.upsert(a));
	}

	@Test
	public void upsert_preconditions_loaded_proxy() {
		A a = ProxyFactory.getInstance().newProxy(A.class, id -> new A());
		EntityUtils.loadProxy(a);
		assertEquals(1, u.upsert(a, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "test"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void upsert_preconditions_odd_number_of_hints() {
		u.upsert(new A(), PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER);
	}
	
	
	@Table("A")
	private static class A implements DynamicColumnsEntity<DynamicColumn>, NewRowOnUpsertAware {
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
		
		int dynamicField;
		
		transient boolean newRow = false;
		
		public A() {
			
		}
		
		public A(int id, String name, int age, int insertable, int updatable, int dynamicField) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.insertable = insertable;
			this.updatable = updatable;
			this.dynamicField = dynamicField;
		}

		@Override
		public void setValue(DynamicColumn column, Object value) {
			this.dynamicField = (int) value;
		}

		@Override
		public Object getValue(DynamicColumn column) {
			return dynamicField;
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
	private static class B implements DynamicColumnsEntity<DynamicColumn>, NewRowOnUpsertAware {
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
		
		int dynamicField;
		
		transient boolean newRow = false;
		
		public B(int id, String name, int age, int insertable, int updatable, int dynammicField) {
			this.id = id;
			this.name = name;
			this.age = age;
			this.insertable = insertable;
			this.updatable = updatable;
			this.dynamicField = dynammicField;
		}

		@Override
		public void setValue(DynamicColumn column, Object value) {
			this.dynamicField = (int) value;
			
		}

		@Override
		public Object getValue(DynamicColumn column) {
			return dynamicField;
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
