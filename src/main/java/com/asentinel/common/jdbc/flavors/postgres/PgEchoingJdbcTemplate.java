package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import javax.sql.DataSource;

import org.postgresql.util.PSQLWarning;
import org.postgresql.util.ServerErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import com.asentinel.common.jdbc.flavors.CustomArgumentPreparedStatementSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

/**
 * Extension of the {@link JdbcTemplate} class that echoes the messages raised
 * by the Postgres database to the java logging system. The name of the logger
 * used for this is {@code DB-ECHO}. <br>
 * This class also supports the following:
 * <li>injecting a custom {@code JdbcFlavor}, by default a
 * {@link PostgresJdbcFlavor} is used.
 * <li>sets the {@code SQLExceptionTranslator} to a default
 * {@link PgBetterSQLExceptionTranslator}.
 * 
 * @see #LOGGER_NAME
 * 
 * @see {@link https://github.com/spring-projects/spring-framework/issues/23106}
 * 
 * @author Razvan Popian
 */
public class PgEchoingJdbcTemplate extends JdbcTemplate {
	
	/**
	 * The name of the default logger used for echoing.  
	 */
	public final static String LOGGER_NAME = "DB-ECHO";
	
	private final static Logger log = LoggerFactory.getLogger(LOGGER_NAME);
	
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
		return log;
	}

	@Override
	protected void handleWarnings(SQLWarning warning) throws SQLWarningException {
		// do nothing, the super throws exception, this is why we had to override this
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
	
	void myHandleWarnings(Statement s) {
		Logger log = getLogger();
		if (!log.isTraceEnabled()) {
			return;
		}

		try {
			SQLWarning warning = s.getWarnings();
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
		} catch (SQLException e) {
			logger.error("Failed to process warnings, but the exception is ignored.", e);
		}
	}
	
	/*
	 * The following methods override the #execute methods from the super class and process warnings even in case of statement failure. 
	 * This is needed until Spring resolves the issue https://github.com/spring-projects/spring-framework/issues/23106 that we opened. Once
	 * this is fixed the next methods can go away and we can do the warning processing in the #handleWarnings method. 
	 */
	
	@Override
	public <T> T execute(StatementCallback<T> action) throws DataAccessException {
		return super.execute((StatementCallback<T>) (stmt) -> {
			try {
				return action.doInStatement(stmt);
			} finally {
				myHandleWarnings(stmt);
			}
		});
	}
	
	
	@Override
	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action)
			throws DataAccessException {
		return super.execute(psc, (ps) -> {
			try {
				return action.doInPreparedStatement(ps);
			} finally {
				myHandleWarnings(ps);
			}
		});
	}
	
	
	@Override
	public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action)
			throws DataAccessException {	
		return super.execute(csc, (cs) -> {
			try {
				return action.doInCallableStatement(cs);
			} finally {
				myHandleWarnings(cs);
			}
		});
	}
	
	@Override
	protected PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
		if (jdbcFlavor == null) {
			log.warn("newArgPreparedStatementSetter - No " + JdbcFlavor.class.getSimpleName() 
					+ " injected, using the default " + PreparedStatementSetter.class.getSimpleName() + " from " + JdbcTemplate.class.getSimpleName()
					+ ". This may not be the fastest setter for null parameters.");
			return super.newArgPreparedStatementSetter(args);
		}
		return new CustomArgumentPreparedStatementSetter(jdbcFlavor, args);
	}
}
