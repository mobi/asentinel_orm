package com.asentinel.common.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

/**
 * This class contains static, reusable, thread safe RowMapper implementations for
 * basic types.
 * 
 * @author Razvan Popian
 */
public final class ReusableRowMappers {
	
	// ---- Recordsets with one column RowMappers for basic types. They are stateless, so they are thread safe.  ---- // 
	
	public static final RowMapper<String> ROW_MAPPER_STRING = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getStringObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_STRING";
		}
	};
	
	public static final RowMapper<Integer> ROW_MAPPER_INTEGER = new RowMapper<Integer>() {
		@Override
		public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getIntObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_INTEGER";
		}
	};
	
	public static final RowMapper<Long> ROW_MAPPER_LONG = new RowMapper<Long>() {
		@Override
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getLongObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_LONG";
		}
	};
	
	
	public static final RowMapper<Double> ROW_MAPPER_DOUBLE = new RowMapper<Double>() {
		@Override
		public Double mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getDoubleObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_DOUBLE";
		}
	};
	
	// TODO: Currently supports 'Y'/'N' char columns for boolean, add some strategy for supporting other types like 1/0 ints. 
	public static final RowMapper<Boolean> ROW_MAPPER_BOOLEAN = new RowMapper<Boolean>() {
		@Override
		public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getBooleanObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_BOOLEAN";
		}
	};
	
	public static final RowMapper<Date> ROW_MAPPER_DATE = new RowMapper<Date>() {
		@Override
		public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getDateObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_DATE";
		}
	};

	public static final RowMapper<BigInteger> ROW_MAPPER_BIG_INTEGER = new RowMapper<BigInteger>() {
		@Override
		public BigInteger mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getBigIntegerObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_BIG_INTEGER";
		}
	};
	
	public static final RowMapper<BigDecimal> ROW_MAPPER_BIG_DECIMAL = new RowMapper<BigDecimal>() {
		@Override
		public BigDecimal mapRow(ResultSet rs, int rowNum) throws SQLException {
			return ResultSetUtils.getBigDecimalObject(rs, 1);
		}
		
		@Override
		public String toString() {
			return "ROW_MAPPER_BIG_DECIMAL";
		}
	};
	
	
	private ReusableRowMappers() {}
}
