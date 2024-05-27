package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asentinel.common.jdbc.InOutCall;
import com.asentinel.common.jdbc.InOutCallTemplate;
import com.asentinel.common.jdbc.flavors.CallableStatementCreatorSupport;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

/**
 * Postgres <code>CallableStatementCreator</code> that creates a <code>CallableStatement</code> from the stored procedure 
 * name string received and the IN parameters array. Postgres only supports stored functions so only ONE parameter will exist.  
 *  
 * This class is able to convert byte arrays and InputStreams to BLOB stored procedure parameters.
 * You should not use this class directly. The {@link InOutCall} implementations ({@link InOutCallTemplate}) 
 * should provide the methods to run CallableStatements.
 *  
 * 
 * @see InOutCall
 * @see InOutCallTemplate
 * 
 * @author Razvan Popian
 */
public class InOutCallableStatementCreator extends CallableStatementCreatorSupport {
	
	/**
	 * @see CallableStatementCreatorSupport#CallableStatementCreatorSupport(JdbcFlavor, String, int, Logger, Object...)
	 */
	public InOutCallableStatementCreator(JdbcFlavor jdbcFlavor, String spName, Object ... inParams) {
		this(jdbcFlavor, spName, null, inParams);
	}

	/**
	 * @see CallableStatementCreatorSupport#CallableStatementCreatorSupport(JdbcFlavor, String, int, Logger, Object...)
	 */
	public InOutCallableStatementCreator(JdbcFlavor jdbcFlavor, String spName, Logger logger, Object ... inParams) {
		// only one resultset is supported in postgres by CallableStatement
		super(jdbcFlavor, spName, 1, logger, inParams);
	}

	@Override
	public CallableStatement createCallableStatement(Connection con) throws SQLException {
        StringBuilder sb = new StringBuilder(100);
        sb.append("{ ? = call ")
        	.append(spName).append("(");
        for (@SuppressWarnings("unused") Object p: inParams) {
       		sb.append("?").append(SEPARATOR);
        }
        
        String sql;
        if (inParams.length > 0) {
        	 sql = sb.substring(0, sb.length() - SEPARATOR.length());
        } else {
        	sql = sb.toString();
        }
        sql = sql + ") }"; 
        if (log.isTraceEnabled()) {
        	log.trace("createCallableStatement - Actual call string: " + sql);
        }
        
        
		CallableStatement cs = con.prepareCall(sql);
        if (log.isTraceEnabled()){
        	log.trace("createCallableStatement - connection: " + con);
        }
        
		int index = 1;
        cs.registerOutParameter(index++, jdbcFlavor.getCursorType());
        lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().setParametersWithIndex(cs, index, lobCreator, inParams);
        return cs;
	}	
	
}
