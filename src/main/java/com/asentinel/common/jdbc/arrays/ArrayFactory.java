package com.asentinel.common.jdbc.arrays;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.asentinel.common.util.Assert;

/**
 * Factory for {@code java.sql.Array} instances.
 *  
 * @author Razvan Popian
 */
public interface ArrayFactory {

	/**
	 * Method for creating new {@code java.sql.Array} instances. It has a default implementation
	 * that uses strictly non vendor specific JDBC code.
	 */
	public default java.sql.Array newArray(PreparedStatement ps, String typeName, Object... objects) 
			throws SQLException {
		Assert.assertNotNull(ps, "ps");
		return ps.getConnection().createArrayOf(typeName, objects);
	}
}
