package com.asentinel.common.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.time.temporal.Temporal;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.flavors.JdbcFlavorConfig;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.Utils;

/**
 * @author Razvan Popian
 */
public final class JdbcUtils  {
	private final static Logger log = LoggerFactory.getLogger(JdbcUtils.class);
	
	private JdbcUtils(){}
	
	/**
	 * This method deregisters all registered jdbc drivers. It should be used
	 * when there is no longer a need for database access in the application.
	 * @return true if all drivers were succesfully deregistered,
	 * 			false otherwise
	 */
	public static boolean unregisterDrivers() {
		boolean ret = true;
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.debug(String.format("unregisterDrivers - deregistered: %s", driver));
            } catch (Exception e) {
            	ret = false;
            	log.debug(String.format("unregisterDrivers - failed to deregistered: %s", driver), e);
            }
        }
        return ret;
	}
	
	public final static int LOG_MAX_SIZE = 200;
	final static int LOG_MAX_SHOW_IF_TOO_LONG = 10;
	
	/**
	 * @see #prepareObjectForLogging(Object, String, String)
	 */
	public static String prepareStringForLogging(CharSequence s) {
		return prepareObjectForLogging(s, "'", "'");
	}

	/**
	 * @return a {@code String} that is used for logging. If the
	 *         {@code Object#toString()} representation of the {@code o} parameter
	 *         is longer than {@value #LOG_MAX_SIZE}, the string length and first
	 *         {@value #LOG_MAX_SHOW_IF_TOO_LONG} characters are returned in a
	 *         string. Otherwise the entire string representation of the object
	 *         parameter is returned.
	 */
	public static String prepareObjectForLogging(Object o, String sd, String ed) {
		if (o == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		String s = o.toString();
		if (s.length() <= LOG_MAX_SIZE) {
			if (o instanceof CharSequence) {
				return sb.append(sd).append(s).append(ed).toString();
			}
			return s;
		}
		sb.append(o.getClass().getSimpleName())
			.append("[length=")
			.append(s.length()).append(", ")
			.append(sd).append(s.subSequence(0, LOG_MAX_SHOW_IF_TOO_LONG)).append(" ...").append(ed)
			.append("]");
		return sb.toString();
		
	}
	
	/**
	 * @see #parametersToString(String, boolean, Object...)
	 */
	public static String parametersToString(boolean squareBrackets, Object ... params) {
		return parametersToString(null, squareBrackets, params);
	}	
	
	/**
	 * Method used for creating a string representation of an array of parameters that are usually passed 
	 * to an {@link SqlQuery} or {@link InOutCall} implementation.
	 * @param separator the separator to use, if null ", " is used.
	 * @param squareBrackets wether to put the result in square brackets or not.
	 * @param params the parameters.
	 * @return the string representation of the array of parameters.
	 */
	public static String parametersToString(String separator, boolean squareBrackets, Object ... params) {
		if (separator == null) {
			separator = ", ";
		}
		if (params == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder(params.length * 15);
		if (squareBrackets) {
			sb.append("[");
		}
		for (Object p: params) {
    		if (p instanceof CharSequence) {
    			sb.append(prepareStringForLogging((CharSequence) p));
    		} else if (p instanceof SqlParameterValue) {
    			SqlParameterValue spv = (SqlParameterValue) p;
    			sb.append("SqlParamVal [")
    				.append("type=").append(spv.getSqlType())
    				.append(", value=");
    			if (spv.getValue() instanceof CharSequence) {
    				sb.append(prepareStringForLogging((CharSequence) spv.getValue()));
    			} else {
	    			sb.append(spv.getValue());
    			}
    			sb.append("]");
    		} else if (p instanceof Boolean) {
    			sb.append(String.valueOf((Boolean) p));
    		} else if (p == BooleanParameterConverter.NULL_BOOLEAN) {
    			sb.append("null");
    		} else if (p instanceof Temporal) {
    			// we want to log the java.util.Date string representation
    			// because a java.util.Date will be sent to the DB
    			try {
    				sb.append(Utils.toDate((Temporal) p));
    			} catch(IllegalArgumentException e) {
    				sb.append(p);
    			}
    		} else {
    			sb.append(prepareObjectForLogging(p, "<", ">"));
    		}
    		sb.append(separator);
		}
		if (params.length > 0) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}
		if (squareBrackets) {
			sb.append("]");
		}
		return sb.toString();
	}
	
	
	public static void cleanupArray(java.sql.Array array) {
		if (array == null) {
			return;
		}
		try {
			array.free();
			if (log.isTraceEnabled()) {
				log.trace("cleanupArray - Array successfully freed.");
			}
		} catch(Exception e) {
			log.warn("cleanupArray - Failed to free the array.", e);
		}
	}
	
	
	/**
	 * Factory method for {@link LobHandler} instances. Previously
	 * we used the now deprecated <code>OracleLobHandler</code>. It is recommended
	 * to use this method for any {@link LobHandler} instance that is needed.
	 * 
	 * @deprecated in favor of using an injected instance of {@code LobHandler};
	 */
	@Deprecated
	public static LobHandler buildLobHandler() {
		return JdbcFlavorConfig.getJdbcFlavor().buildLobHandler();
	}

	/** 
	 * Construct a PostgreSQL specific function call
	 * for the given <code>functionName</functionName> that will use 0 arguments;
	 *  
	 * @see #queryProcedureCall(String, int)
	 * @param functionName - the name of the function to be called
	 * @return the PostgreSQL specific query statement to return the result set(s) of a function call with 0 arguments
	 */
	public static String queryProcedureCall(String functionName) {
		return queryProcedureCall(functionName, 0);
	}
	
	/**
	 * Construct a PostgreSQL specific function call
	 * for the given <code>functionName</functionName> that will also
	 * append the correct number of argument placeholders "(?)";
	 * 
	 * i.e.: AdvancedFind.Invoice, 4 -&gt; SELECT * FROM AdvancedFind.Invoice(?, ?, ?, ?)"
	 * @param functionName - the name of the function to be called
	 * @param numOfParameters - the number of parameters defined for this function
	 * @return a string representing the PostgreSQL specific syntax for the query
	 */
	public static String queryProcedureCall(String functionName, int numOfParameters) {
		Assert.assertNotEmpty(functionName, "functionName");
		Assert.assertPositive(numOfParameters, "numOfParameters");
		
		StringBuilder sb = new StringBuilder("SELECT * FROM ");		
		sb.append(functionName);
		
		sb.append("(");
		
		if (numOfParameters > 0) { 
			sb.append("?");
			String placeholder = ", ?";
			for (int i = 1; i < numOfParameters; i ++)
				sb.append(placeholder);
		}
			
		sb.append(")");
		
		return sb.toString();
	}
}
