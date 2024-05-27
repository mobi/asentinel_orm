package com.asentinel.common.orm.mappers.dynamic;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;

/**
 * Interface to be implemented by entities that support dynamic columns in
 * addition to static columns. By static columns we mean annotated columns with
 * {@link PkColumn} or {@link Column}.
 * 
 * @see DynamicColumn
 * 
 * @author Razvan.Popian
 */
public interface DynamicColumnsEntity<T extends DynamicColumn> {

	/**
	 * Sets the value of a {@code DynamicColumn}.
	 * @param column the target column
	 * @param value the actual value to set
	 */
	void setValue(T column, Object value);
	
	/**
	 * Gets the value for the {@code column} parameter.
	 * @param column the target column
	 * @return the value of the column
	 */
	Object getValue(T column);
}
