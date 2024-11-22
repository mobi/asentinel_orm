package com.asentinel.common.orm.persist;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Validates the upsert behavior of the {@link Updater} for entities that have
 * no mapped columns other than the primary key.
 * 
 * @since 1.66.0
 * @author Razvan Popian
 */
public class SimpleUpdaterNoColumnsUpsertTestCase {
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private SqlQuery ex = mock(SqlQuery.class);
	private Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	private final ArgumentCaptor<String> cSql = ArgumentCaptor.forClass(String.class);
	private final ArgumentCaptor<KeyHolder> cKeyHolder = ArgumentCaptor.forClass(KeyHolder.class);
	
	
	@Before
	public void setup() {
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
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void upsert_insertAutoId_NoSeq() {
		PkOnlyBean bean = new PkOnlyBean(0);
		u.upsert(bean, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void upsert_insertAutoId_WithSeq() {
		PkOnlySeqBean bean = new PkOnlySeqBean(0);
		u.upsert(bean, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)");
	}

	@Test(expected = IllegalArgumentException.class)
	public void upsert_insert() {
		PkOnlySeqBean bean = new PkOnlySeqBean(0);
		u.upsert(bean, UpdateType.INSERT, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(name)" );
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
