package com.asentinel.common.orm.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

/**
 * Strategy interface used by {@link Updater} implementations to detect if an
 * {@code upsert} statement created a new row or updated an existing row in the
 * target database table.
 * 
 * @see Updater
 * @see Updater#upsert(Object, Object...)
 * @see NewRowOnUpsertAware
 * 
 * @author Razvan Popian
 */
public interface NewRowOnUpsertDetector {

	/**
	 * @return array of columns to extract values for from the SQL statement
	 *         generated keys resultset.
	 * @see PreparedStatement#getGeneratedKeys()
	 * @see Connection#prepareStatement(String, String[])
	 */
	String[] getColumns();

	/**
	 * Determines if a new row was created based on the values of the columns
	 * returned by the {@link #getColumns()} method.
	 * 
	 * @param columnValues the values of the columns returned by
	 *                     {@code #getColumns()}
	 * @return {@code true} for a new row, {@code false} otherwise
	 */
	boolean isNewRow(Map<String, Object> columnValues);
}
