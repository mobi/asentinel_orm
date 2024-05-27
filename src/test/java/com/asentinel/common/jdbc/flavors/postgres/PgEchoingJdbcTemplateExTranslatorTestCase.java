package com.asentinel.common.jdbc.flavors.postgres;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.StatementCallback;

import com.asentinel.common.jdbc.exceptions.BetterDuplicateKeyException;

public class PgEchoingJdbcTemplateExTranslatorTestCase {
	
	/**
	 * Test to validate that our custom exception translator is properly setup by
	 * default.
	 */
	@Test(expected = BetterDuplicateKeyException.class)
	public void customExceptionTranslator() throws DataAccessException, SQLException {
		Connection conn = mock(Connection.class);
		
		DatabaseMetaData dbm = mock(DatabaseMetaData.class);
		when(dbm.getDatabaseProductName()).thenReturn("PostgreSQL");
		when(conn.getMetaData()).thenReturn(dbm);
		
		Statement s = mock(Statement.class);
		when(conn.createStatement()).thenReturn(s);
		DataSource ds = mock(DataSource.class);
		when(ds.getConnection()).thenReturn(conn);
		
		PSQLException sqlEx = mock(PSQLException.class);
		when(sqlEx.getSQLState()).thenReturn("23505"); // DuplicateKeyException error code
		
		PgEchoingJdbcTemplate t = new PgEchoingJdbcTemplate(ds);

		@SuppressWarnings("unchecked")
		StatementCallback<Object> c = mock(StatementCallback.class);
		when(c.doInStatement(s)).thenThrow(sqlEx);
		t.execute(c);
	}
}