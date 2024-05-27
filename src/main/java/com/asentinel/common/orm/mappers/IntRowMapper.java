package com.asentinel.common.orm.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class IntRowMapper implements RowMapper<Integer> {
	private final String colName;
	private final boolean allowNull;
	
	public IntRowMapper(String colName) {
		this(colName, false);
	}
	
	public IntRowMapper(String colName, boolean allowNull) {
		Assert.assertNotEmpty(colName, "colName");
		this.colName = colName;
		this.allowNull = allowNull;
	}

	@Override
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ResultSetUtils.getIntObject(rs, colName, allowNull);
	}

	public String getColumnName() {
		return colName;
	}

	public boolean isAllowNull() {
		return allowNull;
	}
}