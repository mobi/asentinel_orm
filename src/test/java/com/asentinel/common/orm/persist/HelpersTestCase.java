package com.asentinel.common.orm.persist;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.easymock.Capture;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.DefaultPreparedStatementParametersSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.orm.TargetMember;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.mappers.dynamic.DefaultDynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCallback;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCreator;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementSetter;

/**
 * Tests the {@link SimpleUpdater} helper inner classes in isolation with a very simple entity type.
 * 
 * @author Razvan Popian
 */
public class HelpersTestCase {
	
	private final SqlQuery qEx = createMock(SqlQuery.class);
	private final JdbcFlavor jdbcFlavor = createMock(JdbcFlavor.class);
	
	private final TargetMembers targetMembers = TargetMembersHolder.getInstance()
			.getTargetMembers(SimpleEntity.class);
	
	private final TargetMember pkMember = targetMembers.getPkColumnMember();
	private final List<TargetMember> insertableMembers = targetMembers.getInsertableColumnMembers();
	private final List<TargetMember> updatableMembers = targetMembers.getUpdatableColumnMembers();
	
	private final Set<DynamicColumn> dynamicColumns = singleton(new DefaultDynamicColumn("dynamicString", String.class));
	
	private final List<SimpleEntity> list = Arrays.asList(
			new SimpleEntity(10, "test1", 100, 1000, "dynamic1"),
			new SimpleEntity(20, "test2", 200, 2000, "dynamic2")
	);
	
	private final PreparedStatement ps = createMock(PreparedStatement.class);
	
	private final ResultSet rs = createMock(ResultSet.class);
	
	private final ResultSetMetaData rsmd = createMock(ResultSetMetaData.class);
	
	private final Connection con = createMock(Connection.class);
	
	private final Capture<String[]> capturePkName = Capture.newInstance();

	@Test
	public void testCustomBatchPreparedStatementSetter() throws SQLException {
		expect(jdbcFlavor.getPreparedStatementParametersSetter()).andReturn(new DefaultPreparedStatementParametersSetter()).anyTimes();

		CustomBatchPreparedStatementSetter psSetter = new SimpleUpdater(jdbcFlavor, qEx)
			.new CustomBatchPreparedStatementSetter(list, UpdateType.INSERT, insertableMembers, dynamicColumns);
		
		ps.setObject(1, 10L);
		ps.setObject(2, 100);
		ps.setString(3, "test1");
		ps.setString(4, "dynamic1");
		ps.setObject(1, 20L);
		ps.setObject(2, 200);
		ps.setString(3, "test2");
		ps.setString(4, "dynamic2");
		
		// simulate the lifecycle of the CustomBatchPreparedStatementSetter
		replay(ps, jdbcFlavor);
		psSetter.setValues(ps, 0);
		psSetter.setValues(ps, 1);
		assertNull(ReflectionTestUtils.getField(psSetter, "lobCreator"));
		psSetter.cleanupParameters();
		verify(ps, jdbcFlavor);
		
		assertEquals(list.size(), psSetter.getBatchSize());
		assertNull(ReflectionTestUtils.getField(psSetter, "lobCreator"));
		assertEquals(Arrays.asList(10L, 100, "test1", "dynamic1", 20L, 200, "test2", "dynamic2"), ReflectionTestUtils.getField(psSetter, "allArgs"));
	}

	@Test
	public void testCustomBatchPreparedStatementCreator() throws SQLException {
		expect(jdbcFlavor.preprocessKeyColumnNames("id")).andReturn(new String[] {"id"});
		expect(jdbcFlavor.getPreparedStatementParametersSetter()).andReturn(new DefaultPreparedStatementParametersSetter()).anyTimes();
		
		CustomBatchPreparedStatementCreator psc = new SimpleUpdater(jdbcFlavor, qEx)
			.new CustomBatchPreparedStatementCreator(list, insertableMembers, updatableMembers, new UpdateSettings<>(dynamicColumns), pkMember, "dummy");
		
		expect(con.prepareStatement(anyObject(String.class), capture(capturePkName))).andReturn(ps);
		ps.setObject(1, 100);
		ps.setString(2, "test1");
		ps.setString(3, "dynamic1");
		
		ps.setObject(1, 200);
		ps.setString(2, "test2");
		ps.setString(3, "dynamic2");
		ps.addBatch();
		expectLastCall().times(list.size());

		// simulate the lifecycle of the CustomBatchPreparedStatementSetter
		replay(ps, con, jdbcFlavor);
		psc.createPreparedStatement(con);
		assertNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		psc.cleanupParameters();
		verify(ps, con, jdbcFlavor);

		assertNull(ReflectionTestUtils.getField(psc, "lobCreator"));
		assertEquals(Arrays.asList(100, "test1", "dynamic1", 200, "test2", "dynamic2"), ReflectionTestUtils.getField(psc, "allArgs"));
	}
	
	@Test
	public void testCustomBatchPreparedStatementCallback() throws DataAccessException, SQLException {
		CustomBatchPreparedStatementCallback action = new SimpleUpdater(jdbcFlavor, qEx)
				.new CustomBatchPreparedStatementCallback(list, pkMember, new UpdateSettings<>(emptyList()));
		
		expect(ps.executeBatch()).andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		expect(ps.getGeneratedKeys()).andReturn(rs);
		
		expect(rs.getMetaData()).andReturn(rsmd).times(2);
		expect(rsmd.getColumnCount()).andReturn(1).times(2);
		expect(rsmd.getColumnLabel(1)).andReturn("id").times(2);
		
		expect(rs.next()).andReturn(true).times(list.size());
		expect(rs.next()).andReturn(false);
		expect(rs.getObject(1))
			.andReturn(new BigDecimal(100))
			.andReturn(new BigDecimal(200));
		rs.close();
		
		replay(ps, rs, rsmd);
		action.doInPreparedStatement(ps);
		verify(ps, rs, rsmd);
		
		// ids did change
		assertEquals(100, list.get(0).id);
		assertEquals(200, list.get(1).id);
	}

	@Test
	public void testCustomBatchPreparedStatementCallback_Wrong_Ids() throws DataAccessException, SQLException {
		CustomBatchPreparedStatementCallback action = new SimpleUpdater(jdbcFlavor, qEx)
				.new CustomBatchPreparedStatementCallback(list, pkMember, new UpdateSettings<>(emptyList()));
		
		expect(ps.executeBatch()).andReturn(new int[]{Statement.SUCCESS_NO_INFO});
		expect(ps.getGeneratedKeys()).andReturn(rs);
		
		expect(rs.getMetaData()).andReturn(rsmd).times(1);
		expect(rsmd.getColumnCount()).andReturn(1).times(1);
		expect(rsmd.getColumnLabel(1)).andReturn("id").times(1);		
		
		expect(rs.next()).andReturn(true).times(1);
		expect(rs.next()).andReturn(false);
		expect(rs.getObject(1)).andReturn(new BigDecimal(100));
		rs.close();
		
		replay(ps, rs, rsmd);
		action.doInPreparedStatement(ps);
		verify(ps, rs, rsmd);
		
		// ids did not change
		assertEquals(10, list.get(0).id);
		assertEquals(20, list.get(1).id);
	}
	
	@Table("table")
	static class SimpleEntity implements DynamicColumnsEntity<DynamicColumn> {
		@PkColumn("id")
		long id;

		String name;
		
		@Column(value = "insertable", updatable = false)
		int insertable;
		
		@Column(value = "updatable", insertable = false)
		int updatable;	

		// just one simulated dynamic field 
		// so that we can implement the DynamicColumnsEntity interface
		String dynamicString;
		
		public SimpleEntity(long id, String name, int insertable, int updatable, String dynamicString) {
			this.id = id;
			this.name = name;
			this.insertable = insertable;
			this.updatable = updatable;
			this.dynamicString = dynamicString;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		@Column("name")
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public void setValue(DynamicColumn column, Object value) {
			this.dynamicString = value.toString();
			
		}

		@Override
		public Object getValue(DynamicColumn column) {
			return this.dynamicString;
		}
	}
}
