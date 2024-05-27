package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

import com.asentinel.common.jdbc.ResultSetSqlParameter;
import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.jdbc.flavors.CallableStatementCallbackSupport;

public class InOutCallableStatementCallback extends CallableStatementCallbackSupport {

	public InOutCallableStatementCallback(ResultSetSqlParameter[] rsParams, Object[] inParams) {
		super(rsParams, inParams);
	}

	@Override
	public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
		long t0 = System.nanoTime();
		cs.execute();
		long t1 = System.nanoTime();
		if (log.isTraceEnabled()){
			log.trace("doInCallableStatement - SP executed in " + ((t1 - t0)/1000000) + " ms" 
					+ " / fetch: " + cs.getFetchSize());
		}
		return ResultSetUtils.getResults(cs, rsParams, 0);
	}

}
