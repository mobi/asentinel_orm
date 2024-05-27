package com.asentinel.common.jdbc.flavors.oracle;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.BooleanParameterConverter;
import com.asentinel.common.jdbc.InOutCall;
import com.asentinel.common.jdbc.InOutCallTemplate;
import com.asentinel.common.jdbc.flavors.CallableStatementCreatorSupport;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

/**
 * Oracle <code>CallableStatementCreator</code> that creates a <code>CallableStatement</code> from the stored procedure name string received,
 * the IN parameters array and the number of output parameters (resultCount member).
 * The OUT parameters must be the last declared parameters in the stored procedure database declaration.
 *  
 * This class is able to convert byte arrays and InputStreams to BLOB stored procedure parameters.
 * You should not use this class directly. The {@link InOutCall} implementations ({@link InOutCallTemplate}) 
 * should provide the methods to run CallableStatements.
 *  
 * Please note that the Oracle driver does not support boolean IN parameters. So this class will insert the boolean
 * 'true' or 'false' string value in the CallableStatement string. Null boolean values are supported by passing 
 * the {@link BooleanParameterConverter#NULL_BOOLEAN} value.
 * 
 * @see CallableStatementCreatorSupport#CallableStatementCreatorSupport(String, int, Logger, Object...)
 * 
 * @see InOutCall
 * @see InOutCallTemplate
 * 
 * @author Razvan Popian
 */
class InOutCallableStatementCreator extends CallableStatementCreatorSupport {
	
	/**
	 * @see CallableStatementCreatorSupport#CallableStatementCreatorSupport(JdbcFlavor, String, int, Logger, Object...)
	 */
	public InOutCallableStatementCreator(JdbcFlavor jdbcFlavor, String spName, int resultCount, Object ... inParams) {
		super(jdbcFlavor, spName, resultCount, null, inParams);
	}

	/**
	 * @see CallableStatementCreatorSupport#CallableStatementCreatorSupport(JdbcFlavor, String, int, Logger, Object...)
	 */
	public InOutCallableStatementCreator(JdbcFlavor jdbcFlavor, LobHandler lobHandler, String spName, int resultCount, Logger logger, Object ... inParams) {
		super(jdbcFlavor, spName, resultCount, logger, inParams);
	}
	
	@Override
	public CallableStatement createCallableStatement(Connection con) throws SQLException {
        
        StringBuilder sb = new StringBuilder(100);
        sb.append(spName).append("(");
        for (Object p:inParams) {
        	if (p == BooleanParameterConverter.NULL_BOOLEAN) {
        		sb.append("null");
        	} else if (p instanceof Boolean) {
        		sb.append(String.valueOf((Boolean)p));
        	} else {
        		sb.append("?");
        	}
        	sb.append(SEPARATOR);
        }
        if (resultCount != 0) {
        	for (int i=0; i < resultCount; i++) {
        		sb.append("?").append(SEPARATOR);
        	}
        }
        String sql;
        if (inParams.length > 0 || resultCount > 0) {
        	 sql = sb.substring(0, sb.length() - SEPARATOR.length()) + ")";
        } else {
        	sql = sb + ")";
        }
        
		CallableStatement cs = con.prepareCall("{call " + sql + " }");
        if (log.isTraceEnabled()){
        	log.trace("createCallableStatement - connection: " + con);
        }
        
		int index = 1;
		for (int i=0; i<inParams.length; i++) {
			if (inParams[i] instanceof SqlParameterValue) {
				SqlParameterValue paramValue = (SqlParameterValue) inParams[i];
				StatementCreatorUtils.setParameterValue(cs, index++, paramValue, paramValue.getValue());
			} else if (inParams[i] instanceof byte[]) {
				initLobCreator();
				lobCreator.setBlobAsBytes(cs, index++, (byte[]) inParams[i]);
			} else if (inParams[i] instanceof InputStream) {
				initLobCreator();
				InputStream in = (InputStream) inParams[i];
				
				// there is no way to determine the length of the content, 
				// so we can pass 0, it is not used anyway by the spring/oracle api
				lobCreator.setBlobAsBinaryStream(cs, index++, in, 0);
			} else if (inParams[i] instanceof Boolean
					|| inParams[i] == BooleanParameterConverter.NULL_BOOLEAN) {
				continue;
			} else {
				jdbcFlavor.getPreparedStatementParametersSetter().setParameter(cs, index++, SqlTypeValue.TYPE_UNKNOWN, inParams[i]);
			}
		}
        for (int i=1; i<=resultCount; i++) {
            cs.registerOutParameter(index++, jdbcFlavor.getCursorType());
        }
        return cs;
	}
	
}
