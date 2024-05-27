package com.asentinel.common.jdbc.flavors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.CallableStatementCallback;

import com.asentinel.common.jdbc.ResultSetSqlParameter;

public abstract class CallableStatementCallbackSupport implements CallableStatementCallback<Object> {

	protected final static Logger log = LoggerFactory.getLogger(CallableStatementCallbackSupport.class);
	
	protected final List<ResultSetSqlParameter> rsParams;
	protected final Object[] inParams;

	/**
	 * Constructor.
	 * @see #CallableStatementCallbackSupport(ResultSetSqlParameter[], Object...)
	 * @see ResultSetSqlParameter
	 */
	public CallableStatementCallbackSupport(ResultSetSqlParameter[] rsParams, Object ... inParams) {
		this(Arrays.asList(rsParams), inParams);
	}
	
	/**
	 * Constructor.
	 * @param rsParams the parameters used for processing the resultsets.
	 * @param inParams  the IN parameters.
	 * 
	 * @see ResultSetSqlParameter
	 */
	public CallableStatementCallbackSupport(List<ResultSetSqlParameter> rsParams, Object ... inParams) {
		if (rsParams == null) {
			rsParams = Collections.emptyList();
		}
		if (inParams == null) {
			inParams = new Object[0];
		}
		this.rsParams = rsParams;
		this.inParams = inParams;
	}
	
	
	@Override
	public String toString() {
		return "CallableStatementCallbackSupport [rsParams=" + rsParams
				+ ", inParams=" + Arrays.toString(inParams) + "]";
	}
	
}
