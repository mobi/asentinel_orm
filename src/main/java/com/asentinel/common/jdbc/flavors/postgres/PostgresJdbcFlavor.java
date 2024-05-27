package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.Types;

import org.slf4j.Logger;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.ResultSetSqlParameter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.PreparedStatementParametersSetter;
import com.asentinel.common.jdbc.flavors.SqlTemplates;

public class PostgresJdbcFlavor implements JdbcFlavor {
	
	/**
	 * Placeholder key to be used in the {@code Updater#upsert(....))} methods {@code hints} argument to specify the SQL text that should
	 * follow the {@code on conflict} clause. Example:
	 * <pre>
	 * 		orm.upsert(e, PostgresJdbcFlavor.UPSERT_CONFLICT_PLACEHOLDER, "(UniqueId)");
	 * </pre> 
	 */
	public final static String UPSERT_CONFLICT_PLACEHOLDER = PostgresSqlTemplates.UPSERT_CONFLICT_PLACEHOLDER;
	
	private final SqlTemplates paginationSqlTemplates = new PostgresSqlTemplates();
	private final PreparedStatementParametersSetter pspSetter = new PgPreparedStatementParametersSetter();
	
	@Override
	public SqlTemplates getSqlTemplates() {
		return paginationSqlTemplates;
	}
	
	@Override
	public PreparedStatementParametersSetter getPreparedStatementParametersSetter() {
		return pspSetter;
	}	
	
	@Override
	public int getCursorType() {
		return Types.OTHER;
	}
	
	@Override
	public CallableStatementCreator buildCallableStatementCreator(String spName, int resultCount, Logger logger, Object... inParams) {
		if (resultCount != 0 && resultCount != 1) {
			throw new IllegalArgumentException("Only zero or one results are supported.");
		}
		return new InOutCallableStatementCreator(this, spName, logger, inParams);
	}

	@Override
	public CallableStatementCallback<?> buildCallableStatementCallback(ResultSetSqlParameter[] rsParams, Object... inParams) {
		return new InOutCallableStatementCallback(rsParams, inParams);
	}
	
	@Deprecated
	@Override
	public LobHandler buildLobHandler() {
		return getPreparedStatementParametersSetter().getLobHandler();
	}
	
	@Deprecated
	@Override
	public LobCreator buildLobCreator() {
		return getPreparedStatementParametersSetter().getLobHandler().getLobCreator();
	}
	
	@Override
	public boolean isWrapDoubleParameterWithBigDecimal() {
		return true;
	}

	@Override
	public boolean isWrapFloatParameterWithBigDecimal() {
		return true;
	}
	
	/**
	 * Postgres needs lower case keys.
	 */
	@Override
	public String[] preprocessKeyColumnNames(String ... keyColumnNames) {
		if (keyColumnNames == null) {
			return null;
		}
		String[] newKeyColumnNames = new String[keyColumnNames.length];
		for (int i = 0; i < newKeyColumnNames.length; i++) {
			if (keyColumnNames[i] == null) {
				newKeyColumnNames[i] = null;
			} else {
				newKeyColumnNames[i] = keyColumnNames[i].toLowerCase();
			}
		}
		return newKeyColumnNames;
	}
	
	
	@Override
	public String toString() {
		return "PostgresJdbcFlavor [paginationSqlTemplates="
				+ paginationSqlTemplates + "]";
	}
}
