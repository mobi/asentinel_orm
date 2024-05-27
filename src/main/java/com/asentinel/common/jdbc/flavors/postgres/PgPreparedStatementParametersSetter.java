package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.StatementCreatorUtils;

import com.asentinel.common.jdbc.flavors.DefaultPreparedStatementParametersSetter;

/**
 * The
 * {@link DefaultPreparedStatementParametersSetter#setParameter(PreparedStatement, int, int, Object)}
 * delegates parameter setting to the
 * {@link StatementCreatorUtils#setParameterValue(PreparedStatement, int, int, Object)}
 * method which uses a slow approach for setting {@code null} values. It is slow
 * because it involves network calls to extract parameter metadata. This class
 * overrides this behavior by setting {@code null}s with one simple instruction
 * because the Postgres driver supports this.
 * 
 * @author Razvan.Popian
 */
public class PgPreparedStatementParametersSetter extends DefaultPreparedStatementParametersSetter {

	@Override
	public void setParameter(PreparedStatement ps, int paramIndex, int sqlType, Object inValue) throws SQLException {
		// the next if is a CRITICAL performance optimization
		if (inValue == null) {
			ps.setNull(paramIndex, java.sql.Types.NULL);
			// alternative for the above line: ps.setObject(paramIndex, null);
		} else {
			super.setParameter(ps, paramIndex, sqlType, inValue);
		}
	}
}
