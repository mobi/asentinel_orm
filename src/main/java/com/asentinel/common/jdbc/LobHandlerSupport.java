package com.asentinel.common.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Abstract class intended primarily as base class for classes that pull
 * BLOB fields from the database ({@code RowMapper}s for example). It still supports the legacy lazy init 
 * for the {@code lobHandler} instance, but it is highly recommended to inject a proper {@code LobHandler} when needed.
 * <br><br>
 * Instances of this class should be used as effectively immutable objects.
 *   
 * @author Razvan Popian
 */
abstract class LobHandlerSupport {
	private final static Logger log = LoggerFactory.getLogger(LobHandlerSupport.class);
	
	private SqlQuery queryEx;
	
	private LobHandler lobHandler;
	
	/*
	 * Synchronization should not be needed for the SqlQuery getter/setter if the 
	 * class is used in an effectively immutable way as the doc advises. Added synchronized
	 * just for extra safety
	 */
	
	/**
	 * @see #setQueryExecutor(SqlQuery)
	 */
	public synchronized final SqlQuery getQueryExecutor() {
		return queryEx;
	}

	/**
	 * Sets the {@code SqlQuery} instance used for lazy loading {@code InputStream}
	 * proxies. If this is {@code null} the input streams will be eagerly loaded.
	 */
	public synchronized final void setQueryExecutor(SqlQuery queryEx) {
		this.queryEx = queryEx;
	}
	
	
	/*
	 * Synchronization IS NEEDED for the LobHandler getter/setter so that
	 * we keep supporting the legacy lazy initialization
	 */
	
	/**
	 * @return the {@link LobHandler} associated with this instance. If not set
	 * 		it creates a new {@code LobHandler} instance and assigns it to the member variable.<br>
	 * 		<b>It is highly recommended NOT to rely on the lazy init behavior of this method. Inject a {@code LobHandler} instead.</b>
	 * 
	 * @see JdbcUtils#buildLobHandler()
	 */
	public final synchronized LobHandler getLobHandler() {
		if (lobHandler == null) {
			// lazy init the LobHandler
			log.warn("getLobHandler - LobHandler lazy initialization is not recommended, because it will not be supported in future versions. Please inject a LobHandler.");
			lobHandler = JdbcUtils.buildLobHandler();
		}
		return lobHandler;
	}

	/**
	 * Set the {@link LobHandler}. If not set it will be lazy initialized by
	 * the {@link #getLobHandler()} method.
	 */
	public final synchronized void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

}
 