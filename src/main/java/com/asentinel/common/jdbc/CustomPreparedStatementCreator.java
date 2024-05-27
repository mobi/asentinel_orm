package com.asentinel.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobCreator;

import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.util.Assert;

/**
 * {@link PreparedStatementCreator} implementation that is able to create statements that can
 * return any database generated ids. It also sets the parameters of the statement and can handle
 * <code>byte[]</code> and <code>InputStream</code> parameters.
 */
class CustomPreparedStatementCreator implements PreparedStatementCreator, ParameterDisposer {
	private final static Logger log = LoggerFactory.getLogger(CustomPreparedStatementCreator.class);
	
	private final JdbcFlavor jdbcFlavor;
	LobCreator lobCreator;
	
	private final String sql;
	private final String[] keyColumnNames;
	private final Object[] params;
	
	
	
	public CustomPreparedStatementCreator(JdbcFlavor jdbcFlavor, String sql, Object ... params) {
		this(jdbcFlavor, sql, null, params);
	}

	public CustomPreparedStatementCreator(JdbcFlavor jdbcFlavor, String sql, String[] keyColumnNames, Object ... params) {
		Assert.assertNotNull(sql, "sql");
		this.jdbcFlavor = jdbcFlavor;
		this.sql = sql;
		this.keyColumnNames = keyColumnNames;
		this.params = params;
	}

	@Override
	public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
		PreparedStatement ps;
		if (keyColumnNames != null) {
			ps = con.prepareStatement(this.sql, keyColumnNames);
		} else {
			ps = con.prepareStatement(this.sql);
		}
		lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().setParameters(ps, lobCreator, params);
		return ps;
	}

	@Override
	public void cleanupParameters() {
		if (lobCreator != null) {
			lobCreator.close();
			if (log.isTraceEnabled()){
				log.trace("cleanupParameters - LobCreator closed.");
			}
		}
		jdbcFlavor.getPreparedStatementParametersSetter().cleanupParameters(params);
	}

	public String getSql() {
		return sql;
	}

	public String[] getKeyColumnNames() {
		return keyColumnNames;
	}

	public Object[] getParams() {
		return params;
	}
}
