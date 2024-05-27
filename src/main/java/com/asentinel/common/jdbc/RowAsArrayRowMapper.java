package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

/**
 * Row mapper that converts each resultset row to an object array.
 * <br><br>
 * 
 * Instances of this class should be used as effectively immutable objects. 
 * 
 * @see AbstractRowMapper
 * @see AbstractRowMapper#valueForColumn(ResultSet, int) for details about conversion.
 * @see AbstractRowMapper.Flag for details about the available flags
 * 
 * @author Razvan Popian
 */
public class RowAsArrayRowMapper extends AbstractRowMapper<Object[]> {

	
	public RowAsArrayRowMapper() {
	}

	public RowAsArrayRowMapper(EnumSet<Flag> flags) {
		super(flags);
	}

	@Override
	public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
		Object[] row = new Object[rs.getMetaData().getColumnCount()];
		for (int i = 0; i < row.length; i++) {
			row[i] = valueForColumn(rs, i + 1);
		}
		return row;
	}
	
	@Override
	public String toString() {
		return "RowAsArrayRowMapper []";
	}
}
