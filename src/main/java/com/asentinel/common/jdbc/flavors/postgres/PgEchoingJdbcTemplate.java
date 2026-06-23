package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import javax.sql.DataSource;

import org.postgresql.jdbc.PgConnection;
import org.postgresql.util.PSQLWarning;
import org.postgresql.util.ServerErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import com.asentinel.common.jdbc.flavors.CustomArgumentPreparedStatementSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

/**
 * Extension of the {@link JdbcTemplate} class that echoes the messages raised
 * by the Postgres database to the java logging system. The name of the logger
 * used for this is {@code DB-ECHO}. It can also output the Postgres PID
 * associated with the current SQL statement. The logger for this is
 * {@code DB-PID}. This class also supports the following:
 * <li>injecting a custom {@code JdbcFlavor}, by default a
 * {@link PostgresJdbcFlavor} is used.
 * <li>sets the {@code SQLExceptionTranslator} to a default
 * {@link PgBetterSQLExceptionTranslator}.
 * 
 * @see #LOGGER_ECHO
 * @see #LOGGER_PID
 * 
 * @author Razvan Popian
 */
public class PgEchoingJdbcTemplate extends JdbcTemplate {
	
	/**
	 * The name of the default logger used for echoing.  
	 */
	public final static String LOGGER_ECHO = "DB-ECHO";
	private final static Logger logEcho = LoggerFactory.getLogger(LOGGER_ECHO);
	
	/**
	 * The name of the default logger used for echoing.  
	 */
	public final static String LOGGER_PID = "DB-PID";
	private final static Logger logPid = LoggerFactory.getLogger(LOGGER_PID);
	
	
	final static String LOG_MESSAGE_TEMPLATE = "%s: %s";
	
	final static String NO_MESSAGE = "Unknown error.";
	
	/*
	 * Marker translator needed because we can not access the translator in the super class
	 * without causing it to be initialized. See the #getExceptionTranslator method.
	 */
	private final static SQLExceptionTranslator NOT_INITIALIZED = (task, sql, ex) -> null;
	
	private JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	public PgEchoingJdbcTemplate() {
		setIgnoreWarnings(false);
		this.setExceptionTranslator(NOT_INITIALIZED);
	}

	public PgEchoingJdbcTemplate(DataSource dataSource) {
		super(dataSource);
		setIgnoreWarnings(false);
		this.setExceptionTranslator(NOT_INITIALIZED);
	}

	public JdbcFlavor getJdbcFlavor() {
		return jdbcFlavor;
	}

	/**
	 * Sets the {@code JdbcFlavor} to use, the default is
	 * {@link PostgresJdbcFlavor}. The {@code JdbcFlavor} is used for setting
	 * statement parameters.
	 * 
	 * @param jdbcFlavor
	 */
	public void setJdbcFlavor(JdbcFlavor jdbcFlavor) {
		this.jdbcFlavor = jdbcFlavor;
	}	
	
	// method for helping with testing, do not directly access the log member
	protected Logger getLogger() {
		return logEcho;
	}

	@Override
	protected void handleWarnings(SQLWarning warning) throws SQLWarningException {
		Logger log = getLogger();
		if (!log.isTraceEnabled()) {
			return;
		}

		while (warning != null) {
			PSQLWarning pWarning = (PSQLWarning) warning;
			ServerErrorMessage sem = pWarning.getServerErrorMessage();
			if (sem == null) {
				log.trace(NO_MESSAGE);
			} else {
				log.trace(String.format(LOG_MESSAGE_TEMPLATE, sem.getSeverity(), warning.getMessage()));
			}
			warning = warning.getNextWarning();
		}
	}
	
	/**
	 * We had to override this method because the
	 * {@code SQLErrorCodeSQLExceptionTranslator} will create a database connection
	 * to get database metadata on construction. That behavior will not play nicely
	 * with an {@code AbstractRoutingDataSource}.
	 */
	@Override
	public SQLExceptionTranslator getExceptionTranslator() {
		// we follow the logic of the super method, but we change the 
		// default exception translator and we don't test for null
		// but for our own marker for not initialized
		SQLExceptionTranslator exceptionTranslator = super.getExceptionTranslator();
		if (exceptionTranslator != NOT_INITIALIZED) {
			return exceptionTranslator;
		}
		synchronized (this) {
			exceptionTranslator = super.getExceptionTranslator();
			if (exceptionTranslator == NOT_INITIALIZED) {
				DataSource dataSource = getDataSource();
				if (dataSource != null) {
					exceptionTranslator = new PgBetterSQLExceptionTranslator(new SQLErrorCodeSQLExceptionTranslator(dataSource));
				} else {
					throw new IllegalStateException("DataSource not set, can not initialize the exception translator.");
				}
				setExceptionTranslator(exceptionTranslator);
			}
			return exceptionTranslator;
		}
	}
	
	@Override
	protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
		if (jdbcFlavor == null) {
			logEcho.warn("newArgPreparedStatementSetter - No " + JdbcFlavor.class.getSimpleName() 
					+ " injected, using the default " + PreparedStatementSetter.class.getSimpleName() + " from " + JdbcTemplate.class.getSimpleName()
					+ ". This may not be the fastest setter for null parameters.");
			return super.newArgPreparedStatementSetter(args);
		}
		return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
	}
	
	@Override
	protected void applyStatementSettings(Statement stmt) throws SQLException {
		super.applyStatementSettings(stmt);
		if (!logPid.isTraceEnabled()) {
			return;
		}
		int pid = getPid(stmt.getConnection());
		logPid.trace("applyStatementSettings - PG backend PID: {}", pid);	
	}
	
    private int getPid(Connection connection) {
		// we do not fail if we can not get the PID, we just return a negative value
    	try {
    		if (connection.isWrapperFor(PgConnection.class)) {
				PgConnection pgConn = connection.unwrap(PgConnection.class);
				return pgConn.getBackendPID();
    		} else {
    			logger.warn("getPid - Can not display the PG backend PID. Not a PG connection.");
    			return -1;
    		}
		} catch (Exception e) {
			logger.error("Exception while trying to get the PG backend PID.", e);
			return -1;
		}
    }
}
