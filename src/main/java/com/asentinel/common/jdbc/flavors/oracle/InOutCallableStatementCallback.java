package com.asentinel.common.jdbc.flavors.oracle;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.asentinel.common.jdbc.BooleanParameterConverter;
import com.asentinel.common.jdbc.InOutCall;
import com.asentinel.common.jdbc.InOutCallTemplate;
import com.asentinel.common.jdbc.ResultSetSqlParameter;
import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.jdbc.flavors.CallableStatementCallbackSupport;


/**
 * CallableStatementCallback that executes a CallableStatement created by an {@link InOutCallableStatementCreator}
 * instance and extracts the results according to the ResultSetSqlParameter array/list received on construction. 
 * You should not use this class directly. The {@link InOutCall} interface and 
 * {@link InOutCallTemplate} class should provide the methods to run CallableStatements. 
 * 
 * @see InOutCall
 * @see InOutCallTemplate
 * 
 * @author Razvan Popian
 */
class InOutCallableStatementCallback extends CallableStatementCallbackSupport {

	/**
	 * Constructor.
	 * @see #InOutCallableStatementCallback(ResultSetSqlParameter[], Object...)
	 * @see ResultSetSqlParameter
	 */
	public InOutCallableStatementCallback(ResultSetSqlParameter[] rsParams, Object ... inParams) {
		this(Arrays.asList(rsParams), inParams);
	}
	
	/**
	 * Constructor.
	 * @param rsParams the parameters used for processing the resultsets.
	 * @param inParams  the IN parameters.
	 * 
	 * @see ResultSetSqlParameter
	 */
	public InOutCallableStatementCallback(List<ResultSetSqlParameter> rsParams, Object ... inParams) {
		super(rsParams, inParams);
	}
	
	
	@Override
	public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
		// realInParamLength will end up storing the inParamLength - [number of Boolean and NULL_BOOLEAN parameters]
		int realInParamLength = countPreparedInputParams(inParams);		
		
		long t0 = System.nanoTime();
		cs.execute();
		long t1 = System.nanoTime();
		if (log.isTraceEnabled()){
			log.trace("doInCallableStatement - SP executed in " + ((t1 - t0)/1000000) + " ms" 
					+ " / fetch: " + cs.getFetchSize());
		}
		return ResultSetUtils.getResults(cs, rsParams, realInParamLength);
	}
	
	/**
	 * Method to be used for calculating the resultsets positions for an {@link InOutCall} call. The boolean
	 * parameters are passed as clear text to the database (not prepared) and they need to be ignored
	 * in the parameter count.
	 * 
	 * @param inParams the input parameters that will be passed to a stored procedure.
	 * @return if the inParams is null this method returns 0.<br>
	 * 			if the inParams contains any Boolean or <code>BooleanParameterConverter.NULL_BOOLEAN</code>
	 * 			this method returns <code>inParams.length</code> minus the number of Boolean or 
	 * 			<code>BooleanParameterConverter.NULL_BOOLEAN</code> parameters.<br>
	 * 			otherwise this method returns <code>inParams.length</code>.
	 * 
	 * @see InOutCall
	 */
	static int countPreparedInputParams(Object ... inParams) {
		if (inParams == null) {
			return 0;
		}
		int realInParamLength = inParams.length;		
		for (int i=0; i<inParams.length; i++) {
			if (inParams[i] instanceof Boolean
					|| inParams[i] == BooleanParameterConverter.NULL_BOOLEAN) {
				realInParamLength--;
			}
		}
		return realInParamLength;
	}
	
}
