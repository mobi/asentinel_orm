package com.asentinel.common.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetMetaDataStub implements ResultSetMetaData {
	
	private final String[] columns;
	
	public ResultSetMetaDataStub(String[] columns) {
		this.columns = columns;
	}

	@Override
	public String getCatalogName(int column) {
		return null;
	}

	@Override
	public String getColumnClassName(int column) {
		return null;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columns.length;
	}

	@Override
	public int getColumnDisplaySize(int column) {
		return 0;
	}

	@Override
	public String getColumnLabel(int column) {
		return null;
	}

	@Override
	public String getColumnName(int column) {
		return columns[column - 1];
	}

	@Override
	public int getColumnType(int column) {
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) {
		return null;
	}

	@Override
	public int getPrecision(int column) {
		return 0;
	}

	@Override
	public int getScale(int column) {
		return 0;
	}

	@Override
	public String getSchemaName(int column) {
		return null;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return null;
	}

	@Override
	public boolean isAutoIncrement(int column) {
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) {
		return false;
	}

	@Override
	public boolean isCurrency(int column) {
		return false;
	}

	@Override
	public boolean isDefinitelyWritable(int column) {
		return false;
	}

	@Override
	public int isNullable(int column) {
		return 0;
	}

	@Override
	public boolean isReadOnly(int column) {
		return false;
	}

	@Override
	public boolean isSearchable(int column) {
		return false;
	}

	@Override
	public boolean isSigned(int column) {
		return false;
	}

	@Override
	public boolean isWritable(int column) {
		return false;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) {
		return null;
	}
}
