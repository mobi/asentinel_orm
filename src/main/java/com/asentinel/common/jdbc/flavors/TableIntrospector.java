package com.asentinel.common.jdbc.flavors;

import java.util.Set;

/**
 * Simple strategy interface used to determine if a table has a certain column.
 * Used primarily by the {@code TableResourceLoader}.
 * 
 * @since 1.59
 * @author Razvan Popian
 *
 */
public interface TableIntrospector {

	/**
	 * @param table  the table name.
	 * @param column the column name.
	 * @return {@code true} if the table exists and has the column, {@code false}
	 *         otherwise.
	 */
	default boolean supports(String table, String column) {
		return getColumns(table).contains(column.toLowerCase());
	}
	
	/**
	 * @param table the table name.
	 * @return the column names for the specified table, never {@code null}. 
	 */
	Set<String> getColumns(String table);
}
