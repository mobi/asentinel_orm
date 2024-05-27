package com.asentinel.common.jdbc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.asentinel.common.util.Assert;

/**
 * ReflectionRowMapper that handles only those resultset columns that start with the specified prefix.
 * The prefix is stripped from the column name and the remaining string is used to find a setter
 * method in the target object.
 *  
 * <br><br>
 * Just like its super class this has two modes of operation:<br>
 * 		<li> "exception for missing setter mode"
 * 		<li> "best effort mode"
 * <br><br>
 * 
 * Instances of this class should be used as effectively immutable objects.
 * 
 * @see ReflectionRowMapper
 * 
 * @author Razvan Popian
 */
public class PrefixReflectionRowMapper<T> extends ReflectionRowMapper<T> {
	
	private final String prefix;
	
	private Set<String> ignoredColumns;

	public PrefixReflectionRowMapper(Class<T> clasz, String prefix) {
		this(clasz, prefix, false);
	}
	
	public PrefixReflectionRowMapper(ObjectFactory<T> objectFactory, String prefix) {
		this(objectFactory, prefix, false);
	}
	
	
	public PrefixReflectionRowMapper(Class<T> clasz, String prefix, boolean bestEffort) {
		super(clasz, bestEffort);
		Assert.assertNotEmpty(prefix, "prefix");
		this.prefix = prefix.toLowerCase();
	}
	
	public PrefixReflectionRowMapper(ObjectFactory<T> objectFactory, String prefix, boolean bestEffort) {
		super(objectFactory, bestEffort);
		Assert.assertNotEmpty(prefix, "prefix");
		this.prefix = prefix.toLowerCase();
	}

	@Override
	protected ColumnMetadata getColumnMetadata(String rsColumnName) {
		if (!rsColumnName.toLowerCase().startsWith(prefix)) {
			return null;
		}
		if (isIgnored(rsColumnName)) {
			return null;
		}
		String columnName = rsColumnName.substring(prefix.length());
		if (isIgnored(columnName)) {
			return null;
		}

		return new ColumnMetadata(prefix, columnName, false);
	}

	/**
	 * Sets certain columns as ignored. This columns
	 * will not be mapped to the target object.<br>
	 */
	public void ignore(String ... columns) {
		if (columns == null) {
			return;
		}
		ignoredColumns = new HashSet<String>();
		for (String column: columns) {
			if (column != null) {
				ignoredColumns.add(column.toLowerCase());
			}
		}
	}
	
	/**
	 * @return set of ignored columns.
	 * @see #ignore(String...)
	 */
	public Set<String> getIgnoredColumns() {
		if (ignoredColumns == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(ignoredColumns);
	}
	
	/**
	 * Tests if a column is ignored.
	 * @see #ignore(String...)
	 */
	public boolean isIgnored(String column) {
		if (ignoredColumns == null || column == null) {
			return false;
		}
		return ignoredColumns.contains(column.toLowerCase());
	}
	
	@Override
	public String toString() {
		return "PrefixReflectionRowMapper [objectFactory=" + getObjectFactory() + "]";
	}
	

}
