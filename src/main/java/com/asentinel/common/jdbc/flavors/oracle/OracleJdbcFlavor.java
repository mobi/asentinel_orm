package com.asentinel.common.jdbc.flavors.oracle;

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

public class OracleJdbcFlavor implements JdbcFlavor {
	
	private final SqlTemplates paginationSqlTemplates = new OracleSqlTemplates();
	
	// replace with something oracle specific if needed. The LobHandler encapsulated by the PreparedStatementParametersSetter
	// must have the createTemporaryLob set to true for Oracle
	private final PreparedStatementParametersSetter pspSetter = new DefaultPreparedStatementParametersSetter();

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
		return 0; // FIXME: OracleTypes.CURSOR;
	}
	
	
	@Override
	public CallableStatementCreator buildCallableStatementCreator(String spName,
			int resultCount, Logger logger, Object... inParams) {
		return new InOutCallableStatementCreator(this, getLobHandler(), spName, resultCount, logger, inParams);
	}

	@Override
	public CallableStatementCallback<?> buildCallableStatementCallback(
			ResultSetSqlParameter[] rsParams, Object... inParams) {
		return new InOutCallableStatementCallback(rsParams, inParams);
	}
	
	/**
	 * Factory method for {@link LobHandler} instances. Previously
	 * we used the now deprecated <code>OracleLobHandler</code>. It is recommended
	 * to use this method for any {@link LobHandler} instance that is needed. 
	 */
	@Deprecated
	@Override
	public LobHandler buildLobHandler() {
		DefaultLobHandler lh = new DefaultLobHandler();
		// the next row is critical !!! Oracle appears to only 
		// work with temporary lobs for inserts/updates
		lh.setCreateTemporaryLob(true);
		return lh;
	}

	@Override
	public String toString() {
		return "OracleJdbcFlavor [paginationSqlTemplates="
				+ paginationSqlTemplates + "]";
	}
}
