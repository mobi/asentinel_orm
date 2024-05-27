package com.asentinel.common.orm.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class ColumnRowMapper implements RowMapper<Object> {
	private final String colName;
	
	public ColumnRowMapper(String colName) {
		Assert.assertNotEmpty(colName, "colName");
		this.colName = colName;
	}
	
	
	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		return rs.getObject(colName);
	}


	public String getColumnName() {
		return colName;
	}
	
}