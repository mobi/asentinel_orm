package com.asentinel.common.jdbc.flavors.postgres;

import static com.asentinel.common.jdbc.flavors.postgres.PgEchoingJdbcTemplate.LOG_MESSAGE_TEMPLATE;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.PSQLWarning;
import org.postgresql.util.ServerErrorMessage;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

public class PgEchoingJdbcTemplateTestCase {
	
	static final String DEBUG = "DEBUG";
	static final String LOG = "LOG";
	static final String INFO = "INFO";
	static final String NOTICE = "NOTICE";
	static final String WARNING = "WARNING"; 
	
	Logger log = mock(Logger.class);
	
	Statement s = mock(Statement.class);

	PgEchoingJdbcTemplate t = new PgEchoingJdbcTemplate() {
		@Override
		protected Logger getLogger() {
			return log;
		}
	};
	
	ServerErrorMessage m0 = mock(ServerErrorMessage.class);
	ServerErrorMessage m1 = mock(ServerErrorMessage.class);
	ServerErrorMessage m2 = mock(ServerErrorMessage.class);
	ServerErrorMessage m3 = mock(ServerErrorMessage.class);
	ServerErrorMessage m4 = mock(ServerErrorMessage.class);
	
	PSQLWarning w0 = new PSQLWarning(m0); 
	PSQLWarning w1 = new PSQLWarning(m1);
	PSQLWarning w2 = new PSQLWarning(m2);
	PSQLWarning w3 = new PSQLWarning(m3);
	PSQLWarning w4 = new PSQLWarning(m4);
	
	@Before
	public void setup() throws SQLException {
		assertFalse(t.isIgnoreWarnings());
		
		when(m0.getSeverity()).thenReturn(DEBUG);
		when(m0.getMessage()).thenReturn(DEBUG);
		
		when(m1.getSeverity()).thenReturn(LOG);
		when(m1.getMessage()).thenReturn(LOG);
		
		when(m2.getSeverity()).thenReturn(INFO);
		when(m2.getMessage()).thenReturn(INFO);

		when(m3.getSeverity()).thenReturn(NOTICE);
		when(m3.getMessage()).thenReturn(NOTICE);

		when(m4.getSeverity()).thenReturn(WARNING);
		when(m4.getMessage()).thenReturn(WARNING);

		
		w0.setNextWarning(w1);
		w1.setNextWarning(w2);
		w2.setNextWarning(w3);
		w3.setNextWarning(w4);
		
		when(s.getWarnings()).thenReturn(w0);
	}
	
	@Test
	public void traceEnabled() {
		when(log.isTraceEnabled()).thenReturn(true);
		t.myHandleWarnings(s);
		verify(log).isTraceEnabled();
		verify(log).trace(String.format(LOG_MESSAGE_TEMPLATE, DEBUG, DEBUG));
		verify(log).trace(String.format(LOG_MESSAGE_TEMPLATE, LOG, LOG));
		verify(log).trace(String.format(LOG_MESSAGE_TEMPLATE, INFO, INFO));
		verify(log).trace(String.format(LOG_MESSAGE_TEMPLATE, NOTICE, NOTICE));
		verify(log).trace(String.format(LOG_MESSAGE_TEMPLATE, WARNING, WARNING));
		verifyNoMoreInteractions(log);
	}
	

	@Test
	public void traceDisabled() {
		when(log.isTraceEnabled()).thenReturn(false);
		t.myHandleWarnings(s);
		verify(log).isTraceEnabled();
		verifyNoMoreInteractions(log);
	}
	
	/**
	 * Validates that {@link PgEchoingJdbcTemplate} does not use the default null
	 * setting logic of the {@link JdbcTemplate}. We are making sure that just a
	 * simple setNull call is executed on the prepared statement.
	 */
	@Test
	public void nullParamSet() throws SQLException {
		PgEchoingJdbcTemplate template = new PgEchoingJdbcTemplate();
		PreparedStatementSetter pss = template.newArgPreparedStatementSetter(new Object[] {null});
		
		PreparedStatement ps = mock(PreparedStatement.class);
		pss.setValues(ps);
		verify(ps, times(1)).setNull(1, java.sql.Types.NULL);
		verifyNoMoreInteractions(ps);
	}
}
