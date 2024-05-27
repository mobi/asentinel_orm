package com.asentinel.common.jdbc;

import com.asentinel.common.util.Assert;

/**
 * Class for encapsulating column information.
 * 
 * @since 1.61.0
 * @author Razvan Popian
 */
public final class ColumnMetadata {

	private final String prefix;
	private final String mappedName;
	private final boolean allowNull;
	
	private final String key;

	/**
	 * @see #ColumnMetadata(String, String, boolean)
	 */
	public ColumnMetadata(String name) {
		this("", name, false);
	}

	/**
	 * @param prefix     the prefix that will be found in front of each column in
	 *                   the {@code ResultSet} to be processed
	 * @param mappedName the name of the column as defined in the database table
	 * @param allowNull  whether it supports {@code null} or not
	 */
	public ColumnMetadata(String prefix, String mappedName, boolean allowNull) {
		Assert.assertNotEmpty(mappedName, "mappedName");
		this.prefix = prefix == null ? "" : prefix;
		this.mappedName = mappedName;
		this.allowNull = allowNull;
		this.key = (this.prefix + this.mappedName).toLowerCase();
	}

	public String getPrefix() {
		return prefix;
	}

	public String getMappedName() {
		return mappedName;
	}
	
	public String getResultsetName() {
		return prefix + mappedName;
	}

	/**
	 * Selects whether to return <code>null</code> if the corresponding
	 * {@code ResultSet} column is <code>null</code> or to return a default value.
	 * For example, for numeric fields the default value is <code>0</code>.
	 * 
	 * @return {@code true} to allow {@code null} returns, {@code false} otherwise.
	 */
	public boolean isAllowNull() {
		return allowNull;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnMetadata other = (ColumnMetadata) obj;
		return key.equals(other.key);
	}

	@Override
	public String toString() {
		return "ColumnMetadata [" + getResultsetName() + "]";
	}
}
