package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * @since 1.63.0
 * @author Razvan Popian
 */
public class SimpleUpdaterDeleteTestCase {
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private final SqlQuery ex = mock(SqlQuery.class);
	private final Updater u = new SimpleUpdater(jdbcFlavor, ex);
	
	@Test
	public void deleteNoIds() {
		assertEquals(0, u.delete(DeleteTestEntity.class));
	}

	@Test
	public void delete() {
		when(ex.update(matches("delete\\s+from\\s+table\\s+where\\s+id\\s+in\\s+\\(\\?\\)"), eq(10)))
			.thenReturn(1);
		assertEquals(1, u.delete(DeleteTestEntity.class, 10));
	}
	
	@Test
	public void deleteSomeIds() {
		when(ex.update(matches("delete\\s+from\\s+table\\s+where\\s+id\\s+in\\s+\\(\\?,\\?\\)"), eq(1), eq(2)))
			.thenReturn(2);
		assertEquals(2, u.delete(DeleteTestEntity.class, 1, 2));
	}

	@Table("table")
	private static class DeleteTestEntity {
		
		@PkColumn("id")
		int id;
	}
}
