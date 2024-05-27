package com.asentinel.common.orm.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class LongRowMapper implements RowMapper<Long> {
	private final String colName;
	private final boolean allowNull;
	
	public LongRowMapper(String colName) {
		this(colName, false);
	}
	
	public LongRowMapper(String colName, boolean allowNull) {
		Assert.assertNotEmpty(colName, "colName");
		this.colName = colName;
		this.allowNull = allowNull;
	}
	
	
	@Override
	public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ResultSetUtils.getLongObject(rs, colName, allowNull);
	}

	public String getColumnName() {
		return colName;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

}
