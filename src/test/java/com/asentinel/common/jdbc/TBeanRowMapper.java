package com.asentinel.common.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TBeanRowMapper implements RowMapper<TBean> {

	@Override
	public TBean mapRow(ResultSet rs, int rowNum) throws SQLException {
		if (rowNum == 1){
			rs.getMetaData();
		}
		TBean b = new TBean();
		b.setValueInt(ResultSetUtils.getIntValue(rs, 1));
		b.setValueLong(ResultSetUtils.getLongValue(rs, 2));
		b.setValueDouble(ResultSetUtils.getDoubleValue(rs, 3));
		b.setValueString(ResultSetUtils.getStringValue(rs, 4));
		b.setValueDate(ResultSetUtils.getDateValue(rs, 5));
		b.setValueBoolean(ResultSetUtils.getBooleanValue(rs, 6));
		return b;
	}
}
