package com.asentinel.common.jdbc.flavors.postgres;

import static com.asentinel.common.jdbc.flavors.postgres.PgTableIntrospector.SELECT_META;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.jdbc.SqlQuery;

public class PgTableIntrospectorTestCase {

	private final SqlQuery q = mock(SqlQuery.class);
	private final PgTableIntrospector i = new PgTableIntrospector(q);
	
	@Test
	public void test() {
		when(q.query(SELECT_META, ReusableRowMappers.ROW_MAPPER_STRING, "testtable"))
			.thenReturn(List.of("test0", "test1", "test2")); // columns are supposed to be lower cased by the SQL query
		
		assertTrue(i.supports("TestTABLE", "TEST0"));
		assertTrue(i.supports("TestTable", "TesT1"));
		assertTrue(i.supports("TESTTable", "TEsT2"));
		
		assertFalse(i.supports("TestTable", "TesT11"));
		
		verify(q, times(1)).query(SELECT_META, ReusableRowMappers.ROW_MAPPER_STRING, "testtable");
	}
}
