package com.asentinel.common.jdbc.flavors;

import org.slf4j.Logger;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.ResultSetSqlParameter;

/**
 * Strategy interface to isolate the database specific stuff.
 * 
 * @author Razvan Popian
 */
public interface JdbcFlavor {
	
	SqlTemplates getSqlTemplates();
	
	int getCursorType();
	
	PreparedStatementParametersSetter getPreparedStatementParametersSetter();
	
	CallableStatementCreator buildCallableStatementCreator(String spName, int resultCount, Logger logger, Object ... inParams);
	
	CallableStatementCallback<?> buildCallableStatementCallback(ResultSetSqlParameter[] rsParams, Object ... inParams);
	
	/**
	 * Factory method for {@link LobHandler} instances. It is recommended
	 * to use this method for any {@link LobHandler} instance that is needed.
	 * 
	 * @deprecated in favor of injecting the right {@link LobHandler} implementation
	 * 		where it is necessary
	 */
	@Deprecated
	LobHandler buildLobHandler();
	
	default LobHandler getLobHandler() {
		return getPreparedStatementParametersSetter().getLobHandler();
	}	
	
	// TODO: Maybe this method should be a parameter of the SqlQuery and InOutCall implementations ?
	default boolean isWrapDoubleParameterWithBigDecimal() {
		return false;
	}

	// TODO: Maybe this method should be a parameter of the SqlQuery and InOutCall implementations ?
	default boolean isWrapFloatParameterWithBigDecimal() {
		return false;
	}
	
	/**
	 * @param keyColumnNames the names of the columns whose value after an insert/update operation should be returned.
	 * @return the names array to be used.
	 */
	default String[] preprocessKeyColumnNames(String ... keyColumnNames) {
		return keyColumnNames;
	}
}
