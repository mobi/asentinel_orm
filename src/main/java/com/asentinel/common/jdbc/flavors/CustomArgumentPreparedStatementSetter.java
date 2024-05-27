package com.asentinel.common.jdbc.flavors;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.lob.LobCreator;

/**
 * Sets the parameters for a {@code PreparedStatement} using the logic defined
 * in the {@code JdbcFlavor} encapsulated
 * {@code PreparedStatementParametersSetter}. Adds support for {@code byte[]},
 * {@code InputStream} and {@code Enum} parameters. Others may be added in the
 * future.<br>
 * 
 * A new instance of this class should be returned by the
 * {@code JdbcTemplate#newArgPreparedStatementSetter(Object[])} overridden
 * method.
 * 
 * 
 * @see JdbcFlavor#getPreparedStatementParametersSetter()
 * @see JdbcTemplate#newArgPreparedStatementSetter(Object[])
 * 
 * @since 1.70.0
 * @author Razvan Popian
 */
public class CustomArgumentPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {
	private final static Logger log = LoggerFactory.getLogger(CustomArgumentPreparedStatementSetter.class);

	private final JdbcFlavor jdbcFlavor;
	private final Object[] args;
	private LobCreator lobCreator;

	public CustomArgumentPreparedStatementSetter(JdbcFlavor jdbcFlavor, Object[] args) {
		this.jdbcFlavor = jdbcFlavor;
		this.args = args;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		lobCreator = jdbcFlavor.getPreparedStatementParametersSetter().setParameters(ps, lobCreator, args);
	}
	
	@Override
	public void cleanupParameters() {
		if (lobCreator != null) {
			lobCreator.close();
			lobCreator = null;
			if (log.isTraceEnabled()) {
				log.trace("cleanupParameters - LobCreator closed.");
			}
		}

		jdbcFlavor.getPreparedStatementParametersSetter().cleanupParameters(args);
	}

}