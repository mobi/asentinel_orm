package com.asentinel.common.jdbc.flavors;

import org.slf4j.Logger;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.ResultSetSqlParameter;

/**
 * Strategy interface to isolate the database specific stuff.
 * 
 * @author Razvan Popian
 */
public interface JdbcFlavor {
	
	public SqlTemplates getSqlTemplates();
	
	public int getCursorType();
	
	public PreparedStatementParametersSetter getPreparedStatementParametersSetter();
	
	public CallableStatementCreator buildCallableStatementCreator(String spName, int resultCount, Logger logger, Object ... inParams);
	
	public CallableStatementCallback<?> buildCallableStatementCallback(ResultSetSqlParameter[] rsParams, Object ... inParams);
	
	/**
	 * Factory method for {@link LobHandler} instances. It is recommended
	 * to use this method for any {@link LobHandler} instance that is needed.
	 * 
	 * @deprecated in favor of injecting the right {@link LobHandler} implementation
	 * 		where it is needed
	 */
	@Deprecated
	public LobHandler buildLobHandler();

	
	/**
	 * Factory method for {@link LobCreator} instances. Previously
	 * we used the now deprecated <code>OracleLobCreator</code>. It is recommended
	 * to use this method for any {@link LobCreator} instance that is needed.
	 * 
	 * @deprecated in favor of injecting the right {@link LobHandler} implementation
	 * 		where it is needed and using it as a factory of {@link LobCreator} instances.
	 */
	@Deprecated
	public LobCreator buildLobCreator();
	
	public default LobHandler getLobHandler() {
		return getPreparedStatementParametersSetter().getLobHandler();
	}	
	
	// TODO: Maybe this method should be a parameter of the SqlQuery and InOutCall implementations ?
	public default boolean isWrapDoubleParameterWithBigDecimal() {
		return false;
	}

	// TODO: Maybe this method should be a parameter of the SqlQuery and InOutCall implementations ?
	public default boolean isWrapFloatParameterWithBigDecimal() {
		return false;
	}
	
	/**
	 * @param keyColumnNames the names of the columns whose value after an insert/update operation should be returned.
	 * @return the names array to be used.
	 */
	public default String[] preprocessKeyColumnNames(String ... keyColumnNames) {
		return keyColumnNames;
	}
}
