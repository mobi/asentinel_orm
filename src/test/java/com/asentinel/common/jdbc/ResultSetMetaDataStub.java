package com.asentinel.common.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetMetaDataStub implements ResultSetMetaData {
	
	private final String[] columns;
	
	public ResultSetMetaDataStub(String[] columns) {
		this.columns = columns;
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		
		return null;
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		
		return null;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columns.length;
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		
		return 0;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		
		return null;
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return columns[column - 1];
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		
		return null;
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		
		return 0;
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		
		return null;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		
		return null;
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		
		return false;
	}

	@Override
	public int isNullable(int column) throws SQLException {
		
		return 0;
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		
		return false;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		
		return null;
	}

}
