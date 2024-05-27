package com.asentinel.common.orm.persist;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCallback;

/**
 * @since 1.60.10
 * @author Razvan Popian
 */
public class AutoIdRollbackBatchTestCase {
	private final SqlQuery qEx = mock(SqlQuery.class);
	private final SimpleUpdater updater = new SimpleUpdater(new PostgresJdbcFlavor(), qEx);
	private final PreparedStatement ps = mock(PreparedStatement.class);
	private final ResultSet keysRs = mock(ResultSet.class);
	private final ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
	
	private final TestBean bean0 = new TestBean("test-desc-0");
	private final TestBean bean1 = new TestBean("test-desc-1");

	private final CustomBatchPreparedStatementCallback actionInsert = updater.new CustomBatchPreparedStatementCallback(
			List.of(bean0, bean1), TargetMembersHolder.getInstance().getTargetMembers(TestBean.class).getPkColumnMember(),
			new UpdateSettings<>(emptyList()));
	
	private final CustomBatchPreparedStatementCallback actionUpsertAutoId = updater.new CustomBatchPreparedStatementCallback(
			List.of(bean0, bean1), TargetMembersHolder.getInstance().getTargetMembers(TestBean.class).getPkColumnMember(),
			new UpdateSettings<>(emptyList()),
			true, UpdateType.INSERT_AUTO_ID);
	
	private final CustomBatchPreparedStatementCallback actionUpsertInsert = updater.new CustomBatchPreparedStatementCallback(
			List.of(bean0, bean1), TargetMembersHolder.getInstance().getTargetMembers(TestBean.class).getPkColumnMember(),
			new UpdateSettings<>(emptyList()),
			true, UpdateType.INSERT);
	
	
	@Before
	public void setup() {
		TransactionSynchronizationManager.initSynchronization();
	}
	
	@After
	public void teardown() {
		TransactionSynchronizationManager.clearSynchronization();
	}
	
	private void setupMocks() throws SQLException {
		when(ps.executeBatch()).thenReturn(new int[] {1, 1});
		when(ps.getGeneratedKeys()).thenReturn(keysRs);
		
		when(keysRs.getMetaData()).thenReturn(rsmd);
		when(rsmd.getColumnCount()).thenReturn(1);
		when(rsmd.getColumnLabel(1)).thenReturn("id");
		
		when(keysRs.next()).thenReturn(true, true, false);
		when(keysRs.getObject(1)).thenReturn(10, 11);
	}
	
	@Test
	public void batchInsertAutoIdCommit() throws SQLException {
		setupMocks();
		
		actionInsert.doInPreparedStatement(ps);
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
		
		// simulate commit
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
	}
	
	@Test
	public void batchInsertAutoIdRollback() throws SQLException {
		setupMocks();
		
		actionInsert.doInPreparedStatement(ps);
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
		
		// simulate rollback
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
		assertEquals(0, bean0.id);
		assertEquals(0, bean1.id);
	}

	@Test
	public void batchUpsertAutoIdCommit() throws SQLException {
		setupMocks();
		
		actionUpsertAutoId.doInPreparedStatement(ps);
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
		
		// simulate commit
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
	}
		
	
	@Test
	public void batchUpsertAutoIdRollback() throws SQLException {
		setupMocks();
		
		actionUpsertAutoId.doInPreparedStatement(ps);
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
		
		// simulate rollback
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
		assertEquals(0, bean0.id);
		assertEquals(0, bean1.id);
	}
	
	@Test
	public void batchUpsertInsert() throws SQLException {
		setupMocks();
		
		bean0.id = 10;
		bean1.id = 11;
		
		actionUpsertInsert.doInPreparedStatement(ps);
		assertEquals(10, bean0.id);
		assertEquals(11, bean1.id);
		
		assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
	}

	@Table("test")
	private static class TestBean {
		
		@PkColumn("id")
		int id;
		
		@Column("description")
		String description;

		public TestBean(String description) {
			this.description = description;
		}
	}
	
}
