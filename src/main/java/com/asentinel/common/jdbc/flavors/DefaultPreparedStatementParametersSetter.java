package com.asentinel.common.jdbc.flavors;

import java.io.Closeable;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.temporal.Temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.jdbc.EnumId;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.Utils;

/**
 * The default implementation of the {@link PreparedStatementParametersSetter},
 * can be used as a base class for more specific implementations.
 * 
 * @author Razvan.Popian
 */
public class DefaultPreparedStatementParametersSetter implements PreparedStatementParametersSetter {
	private final static Logger log = LoggerFactory.getLogger(DefaultPreparedStatementParametersSetter.class);	
	
	// the DefaultLobHandler is thread safe and reusable once configured (unlike the implementations of LobCreator 
	// which are not).
	private LobHandler lobHandler = new DefaultLobHandler();
	
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@Override
	public LobHandler getLobHandler() {
		return lobHandler;
	}
	
	@Override
	public LobCreator setParameters(PreparedStatement ps, Object ... inParams) throws SQLException {
		return setParameters(ps, null, null, inParams);
	}
	
	@Override
	public LobCreator setParameters(PreparedStatement ps, LobCreator lobCreator, Object ... inParams) throws SQLException {
		return setParametersWithIndex(ps, 1, lobCreator, inParams);
	}

	@Override
	public LobCreator setParametersWithIndex(PreparedStatement ps, int index, 
			LobCreator lobCreator, Object ... inParams) throws SQLException {
		Assert.assertNotNull(ps, "ps");
		if (inParams == null) {
			return lobCreator;
		}
		for (int i=0; i < inParams.length; i++) {
			if (inParams[i] instanceof SqlParameterValue) {
				SqlParameterValue paramValue = (SqlParameterValue) inParams[i];
				StatementCreatorUtils.setParameterValue(ps, index++, paramValue, paramValue.getValue());
			} else if (inParams[i] instanceof byte[]) {
				lobCreator = getLobCreator(lobCreator);
				lobCreator.setBlobAsBytes(ps, index++, (byte[]) inParams[i]);
			} else if (inParams[i] instanceof InputStream) {
				InputStream in = (InputStream) inParams[i];
				lobCreator = getLobCreator(lobCreator);
				// there is no way to determine the length of the content so we pass -1,
				// the LobCreator should use the setBinaryStream method without content length
				lobCreator.setBlobAsBinaryStream(ps, index++, in, -1);
			} else {
				setParameter(ps, index++, SqlTypeValue.TYPE_UNKNOWN, inParams[i]);
			}
		}
		return lobCreator;
	}
	
	/**
	 * Delegates to
	 * {@link StatementCreatorUtils#setParameterValue(PreparedStatement, int, int, Object)},
	 * but it converts any java 8 date/time objects to <code>java.util.Date</code>.
	 */
	@Override
	public void setParameter(PreparedStatement ps, int paramIndex, int sqlType, Object inValue) throws SQLException {
		// When Spring JDBC supports java 8 dates we will remove this if.
		if (inValue instanceof Temporal) {
			inValue = Utils.toDate((Temporal) inValue);
		} else if (inValue instanceof Enum) {
			if (inValue instanceof EnumId) {
				Enum<?> tempEnum = (Enum<?>) inValue;
				inValue = ((EnumId<?>) inValue).getId();
				if (inValue == null) {
					throw new IllegalStateException("Enum constant" + tempEnum + " implements " + EnumId.class.getSimpleName() 
							+ " incorrectly because it has a null id.");
				}
			} else {
				inValue = inValue.toString();
			}
		}
		StatementCreatorUtils.setParameterValue(ps, paramIndex, sqlType, inValue);
	}
	
	/**
	 * Performs SQL statement parameters cleanup, by closing any {@link Closeable} parameters
	 * and by calling Spring's {@link StatementCreatorUtils#cleanupParameters(Object...)} 
	 */
	@Override
	public void cleanupParameters(Object ... inParams) {
		if (inParams == null || inParams.length == 0) {
			return;
		}
		// Cleanup for Closeable parameters
		boolean hasCloseable = false;
		for (Object param:inParams){
			if (param instanceof Closeable) {
				hasCloseable = true;
				try {
					((Closeable) param).close();
				} catch (Exception e) {
					if (log.isTraceEnabled()) {
						log.trace("cleanupParameters - Failed to close parameter " + param + ".", e);
					}
				}
			}
		}
		if (hasCloseable) {
			if (log.isTraceEnabled()) {
				log.trace("cleanupParameters - Some parameters were Closeable, so they were closed.");
			}
		}
		
		// Cleanup for other parameters
		StatementCreatorUtils.cleanupParameters(inParams);		
	}	
}
