package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.persist.SimpleUpdater.CustomBatchPreparedStatementCreator;

/**
 * Validates the update behavior of the {@link Updater} for entities that have
 * no mapped columns other than the primary key.
 * 
 * @since 1.66.0
 * @author Razvan Popian
 */
public class SimpleUpdaterNoColumnsInsertUpdateTestCase {
	
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private final JdbcOperations jdbcOps = mock(JdbcOperations.class);
	private final SqlQuery ex = mock(SqlQuery.class);
	private final Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	private final ArgumentCaptor<String> cSql = ArgumentCaptor.forClass(String.class);
	private final ArgumentCaptor<KeyHolder> cKeyHolder = ArgumentCaptor.forClass(KeyHolder.class);
	
	
	@Test(expected = IllegalArgumentException.class)
	public void insertAutoId() {
		PkOnlyBean b = new PkOnlyBean(0);	
		u.update(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void insertAutoIdCollection() {
		PkOnlyBean b0 = new PkOnlyBean(0);
		PkOnlyBean b1 = new PkOnlyBean(0);
		u.update(List.of(b0, b1));
	}
	
	@Test
	public void insertSeq() {
		PkOnlySeqBean b = new PkOnlySeqBean(0);
		
		when(ex.update(cSql.capture(), eq(new String[] {"id"}), cKeyHolder.capture()))
				.then(new Answer<Integer>() {

					@Override
					public Integer answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						for (Object arg: args) {
							if (arg instanceof GeneratedKeyHolder) {
								GeneratedKeyHolder generatedKeyHolder = (GeneratedKeyHolder) arg;
								List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
								generatedKeys.add(Map.of("id", 10));
								return 1;
							}
						}
						fail("Can not find KeyHolder argument.");
						// make the compiler happy, return null
						return null;
					}
				});
		
		int count = u.update(b);
		assertEquals(1, count);
		assertEquals(10, b.id);
		assertEquals("insert into PkOnly(id) values(nextval('seq'))".replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase()
		);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void insertSeqCollection() {
		PkOnlySeqBean b0 = new PkOnlySeqBean(0);
		PkOnlySeqBean b1 = new PkOnlySeqBean(0);
		
		ArgumentCaptor<CustomBatchPreparedStatementCreator> psc = ArgumentCaptor.forClass(CustomBatchPreparedStatementCreator.class);
		when(ex.getJdbcOperations()).thenReturn(jdbcOps);
		when(jdbcOps.execute(psc.capture(), any(PreparedStatementCallback.class)))
			.thenReturn(null);
		
		u.update(List.of(b0, b1));
		
		assertEquals("insert into PkOnly(id) values(nextval('seq'))".replaceAll("\\s", "").toLowerCase(), 
				psc.getValue().getSql().replaceAll("\\s", "")
				.toLowerCase()
		);		
	}
	
	@Test
	public void insert() {
		PkOnlyBean b = new PkOnlyBean(10);
		
		when(ex.update(cSql.capture(), eq(b.id)))
				.thenReturn(1);
		
		int count = u.update(b, UpdateType.INSERT); // forced insert is critical
		assertEquals(1, count);
		assertEquals("insert into PkOnly(id) values(?)".replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase()
		);		
	}
	
	@Test
	public void insertCollection() {
		PkOnlyBean b0 = new PkOnlyBean(10);
		PkOnlyBean b1 = new PkOnlyBean(11);
		
		when(ex.getJdbcOperations()).thenReturn(jdbcOps);
		when(jdbcOps.batchUpdate(cSql.capture(), any(BatchPreparedStatementSetter.class)))
			.thenReturn(new int[] {1, 1});
		
		u.update(List.of(b0, b1), UpdateType.INSERT); // forced insert is critical
		
		assertEquals("insert into PkOnly(id) values(?)".replaceAll("\\s", "").toLowerCase(), 
				cSql.getValue().replaceAll("\\s", "")
				.toLowerCase()
		);		
	}
		
	@Test
	public void update() {
		PkOnlyBean b = new PkOnlyBean(10);
		
		int count = u.update(b);
		
		// no columns to update, no rows updated
		assertEquals(0, count);
		verifyNoInteractions(ex);
	}
	
	
	@Test
	public void updateCollection() {
		PkOnlyBean b0 = new PkOnlyBean(10);
		PkOnlyBean b1 = new PkOnlyBean(11);
		
		u.update(List.of(b0, b1));
		
		// no columns to update, no rows updated
		verify(ex).getJdbcOperations();
		verifyNoMoreInteractions(ex);
	}

	
	@Table("PkOnly")
	private static class PkOnlyBean {

		@PkColumn("id")
		int id;
		
		PkOnlyBean(int id) {
			this.id = id;
		}

	}
	
	@Table("PkOnly")
	private static class PkOnlySeqBean {

		@PkColumn(value = "id", sequence = "seq")
		int id;
		
		PkOnlySeqBean(int id) {
			this.id = id;
		}

	}
	
}
