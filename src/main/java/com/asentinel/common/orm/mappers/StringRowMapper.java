package com.asentinel.common.orm.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class StringRowMapper implements RowMapper<String> {
	private final String colName;
	private final boolean allowNull;
	
	public StringRowMapper(String colName) {
		this(colName, false);
	}
	
	public StringRowMapper(String colName, boolean allowNull) {
		Assert.assertNotEmpty(colName, "colName");
		this.colName = colName;
		this.allowNull = allowNull;
	}
	
	
	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ResultSetUtils.getStringObject(rs, colName, allowNull);
	}

	public String getColumnName() {
		return colName;
	}

	public boolean isAllowNull() {
		return allowNull;
	}
}
