package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * @since 1.60.10
 * @author Razvan Popian
 */
public class AutoIdRollbackSingleEntityTestCase {

	private final SqlQuery qEx = mock(SqlQuery.class);
	private final SimpleUpdater updater = new SimpleUpdater(new PostgresJdbcFlavor(), qEx);
	
	private final TestBean bean = new TestBean("test-desc");
	
	@Before
	public void setup() {
		TransactionSynchronizationManager.initSynchronization();
	}
	
	@After
	public void teardown() {
		TransactionSynchronizationManager.clearSynchronization();
	}
	
	private void prepareForInsert() {
		when(qEx.update(anyString(), eq(new String[] {"id"}), any(KeyHolder.class), eq(bean.description)))
			.thenAnswer(invocation -> {
				KeyHolder kh = (KeyHolder) invocation.getArguments()[2];
				List<Map<String, Object>> list = kh.getKeyList();
				list.add(Map.of("id", 10));
				return 1;
			});		
	}
	
	private void prepareForUpsert() {
		when(qEx.update(anyString(), eq(new String[] {"id"}), any(KeyHolder.class), eq(bean.description), eq(bean.description)))
			.thenAnswer(invocation -> {
				KeyHolder kh = (KeyHolder) invocation.getArguments()[2];
				List<Map<String, Object>> list = kh.getKeyList();
				list.add(Map.of("id", 10));
				return 1;
			});		
	}

	@Test
	public void singleEntityInsertCommit() {
		prepareForInsert();
		
		updater.update(bean);
		assertEquals(10, bean.id);
		
		// simulate commit
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
		assertEquals(10, bean.id);
	}

	
	@Test
	public void singleEntityInsertRollback() {
		prepareForInsert();
		
		updater.update(bean);
		assertEquals(10, bean.id);
		
		// simulate rollback
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
		assertEquals(0, bean.id);
	}
	
	@Test
	public void singleEntityUpdate() {
		prepareForInsert();
		
		bean.id = 10;
		updater.update(bean);
		assertEquals(10, bean.id);

		// no cleanup regardless of commit/rollback outcome
		assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
	}
	
	@Test
	public void singleEntityUpsertCommit() {
		prepareForUpsert();
		
		updater.upsert(bean, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(description)");
		assertEquals(10, bean.id);
		
		// simulate commit
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
		assertEquals(10, bean.id);
	}
	
	@Test
	public void singleEntityUpsertRollback() {
		prepareForUpsert();
		
		updater.upsert(bean, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(description)");
		assertEquals(10, bean.id);
		
		// simulate rollback
		TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
		assertEquals(0, bean.id);
	}
	

	@Test
	public void singleEntityUpsertInsertNoAutoId() {
		prepareForUpsert();
		
		bean.id = 10;
		updater.upsert(bean, new UpdateSettings<>(UpdateType.INSERT), PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(description)");
		assertEquals(10, bean.id);
		
		// no cleanup regardless of commit/rollback outcome
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
