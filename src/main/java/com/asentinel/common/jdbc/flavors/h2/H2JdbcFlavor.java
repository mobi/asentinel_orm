package com.asentinel.common.jdbc.flavors.h2;

import org.slf4j.Logger;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.ResultSetSqlParameter;
import com.asentinel.common.jdbc.flavors.DefaultPreparedStatementParametersSetter;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.PreparedStatementParametersSetter;
import com.asentinel.common.jdbc.flavors.SqlTemplates;

/**
 * @since 1.70.0
 * @author Razvan Popian
 */
public class H2JdbcFlavor implements JdbcFlavor {
	
	private final SqlTemplates paginationSqlTemplates = new H2SqlTemplates();
	
	private final PreparedStatementParametersSetter pps = new DefaultPreparedStatementParametersSetter();

	@Override
	public SqlTemplates getSqlTemplates() {
		return paginationSqlTemplates;
	}

	@Override
	public int getCursorType() {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support callable statements.");
	}

	@Override
	public PreparedStatementParametersSetter getPreparedStatementParametersSetter() {
		return pps;
	}

	@Override
	public CallableStatementCreator buildCallableStatementCreator(String spName, int resultCount, Logger logger,
			Object... inParams) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support callable statements.");
	}

	@Override
	public CallableStatementCallback<?> buildCallableStatementCallback(ResultSetSqlParameter[] rsParams,
			Object... inParams) {
		throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " does not support callable statements.");
	}

	@Override
	public LobHandler buildLobHandler() {
		return new DefaultLobHandler();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
