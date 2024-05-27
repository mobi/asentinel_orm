package com.asentinel.common.jdbc.flavors;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.lob.LobCreator;

import com.asentinel.common.jdbc.JdbcUtils;
import com.asentinel.common.jdbc.SimpleUser;
import com.asentinel.common.jdbc.ThreadLocalUser;
import com.asentinel.common.util.Assert;

/**
 * Base class for {@link CallableStatementCreator} implementations. It encapsulates the parameters, a {@link JdbcFlavor}, a {@link LobCreator}
 * and a {@link Logger}. It also implements {@link ParameterDisposer} to make sure that IN parameters are properly disposed
 * after the call is completed. It is not intended to be used directly in user code.
 * <br> 
 * The constructors log the call to the stored procedure in a standard format, regardless of the 
 * selected JDBC flavor.
 * <br>
 * Not reusable, not thread safe.
 * 
 * @author Razvan Popian
 *
 */
public abstract class CallableStatementCreatorSupport implements CallableStatementCreator, ParameterDisposer {
	private final static Logger DEFAULT_LOG = LoggerFactory.getLogger(CallableStatementCreatorSupport.class);
	
	public static final String SEPARATOR = ", ";
	
	protected final JdbcFlavor jdbcFlavor;
	
	protected final String spName;
	protected final int resultCount;
	protected final Object[] inParams;
	
	protected LobCreator lobCreator = null;
	
	protected final Logger log;	
	
	/**
	 * @see #CallableStatementCreatorSupport(JdbcFlavor, String, int, Logger, Object...)
	 */
	public CallableStatementCreatorSupport(JdbcFlavor jdbcFlavor,
			String spName, int resultCount, Object ... inParams) {
		this(jdbcFlavor, spName, resultCount, null, inParams);
	}

	/** 
	 * Constructor.
	 * Logs the creation of the sql string. If a user is present in the static ThreadLocalUser class for the current
	 * thread, this user will be used for logging.
	 * @param jdbcFlavor the {@code JdbcFlavor} implementation to use.
	 * @param spName the name of the stored procedure including package name if necessary.
	 * @param resultCount the number of resultsets expected
	 * @param logger the logger to use. If <code>null</code> the default class logger will be used.
	 * @param inParams the input parameters for the stored procedure. Can be any type that matches the actual stored
	 * 					procedure argument. Objects with class {@link SqlParameterValue} are also accepted if more
	 * 					exact parameter matching is necessary. Also byte arrays or InputStreams can be used where 
	 * 					the stored procedure expects blobs.
	 * 
	 */
	public CallableStatementCreatorSupport(JdbcFlavor jdbcFlavor,
			String spName, int resultCount, Logger logger, Object ... inParams) {
		Assert.assertNotNull(jdbcFlavor, "jdbcFlavor");
		Assert.assertNotEmpty(spName, "spName");
		Assert.assertPositive(resultCount, "resultCount");
		this.jdbcFlavor = jdbcFlavor;
		if (inParams == null) {
			inParams = new Object[0];
		}
        this.spName = spName;
        this.resultCount = resultCount;
        this.log = logger != null ? logger : DEFAULT_LOG;
        this.inParams = inParams;
        if (log.isDebugEnabled()){
        	final String separator = ", ";
        	String sql;
        	StringBuilder sb = new StringBuilder(100);
        	Object userId = -1;
        	String userName = "UNKNOWN";
        	SimpleUser user = ThreadLocalUser.getThreadLocalUser().get();
        	if (user != null) {
    			userId = user.getUserId();
    			userName = user.getUsername();
        	}
        	sb.append(spName).append("(");
        	sb.append(JdbcUtils.parametersToString(separator, false, inParams));
        	if (inParams.length > 0) {
        		sb.append(separator);
        	}
        	for (int i=0; i<resultCount; i++) {
        		sb.append("?").append(separator);
        	}
            if (inParams.length > 0 || resultCount > 0) {
            	sql = sb.substring(0, sb.length() - separator.length()) + ")";
            } else {
            	sql = sb + ")";
            }
        	if (user != null) {
        		log.debug("<init> - user: " + userName + " (" + userId + "); Call: " + sql);
        	} else {
        		log.debug("<init> - Call: " + sql);
        	}
        }
        
	}
	
	
	@Override
	public void cleanupParameters() {
		// Cleanup the lobCreator
		if (lobCreator != null) {
			lobCreator.close();
			lobCreator = null;
			if (log.isTraceEnabled()){
				log.trace("cleanupParameters - LobCreator closed.");
			}
		}

		// cleanup Closeable and spring specific parameters
		jdbcFlavor.getPreparedStatementParametersSetter().cleanupParameters(inParams);
	}
	
	protected void initLobCreator() {
		this.lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().getLobCreator(lobCreator);
	}
	
	/**
	 * Used for testing.<br>
	 * Should not be called in user code.
	 * 
	 * @return the LobCreator or null if not yet created.
	 */
	public LobCreator getLobCreator() {
		return lobCreator;
	}


	/**
	 * Used for testing. Sets the LobCreator.<br>
	 * Should not be called in user code.
	 * 
	 * @param lobCreator
	 */
	public void setLobCreator(LobCreator lobCreator) {
		this.lobCreator = lobCreator;
	}

	/**
	 * Used for testing. <br>
	 * Should not be called in user code.
	 */
	public Object[] getInParams() {
		return inParams;
	}



	@Override
	public String toString() {
		return "CallableStatementCreatorSupport [spName=" + spName
				+ ", resultCount=" + resultCount 
				+ ", inParams=" + Arrays.toString(inParams) + "]";
	}

}
