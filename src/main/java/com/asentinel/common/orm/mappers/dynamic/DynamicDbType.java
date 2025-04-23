package com.asentinel.common.orm.mappers.dynamic;

import com.asentinel.common.orm.mappers.DbType;
import com.asentinel.common.util.Assert;

/**
 * Equivalent for the {@link DbType} annotation for dynamic columns. It holds
 * information about the type of the mapped database column if the dynamic
 * column is not a standard SQL type. It should be used together with a
 * {@code ConversionService} when custom conversion from the java type to the
 * SQL type is needed.
 * 
 * @see DbType
 * @see DynamicColumn#getDynamicDbType()
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
public class DynamicDbType {

	private final String value;

	public DynamicDbType(String value) {
		Assert.assertNotEmpty(value, "value");
		this.value = value;
	}
	
	/**
	 * @return information to be used by the {@code ConversionService} to perform
	 *         the conversion from the java type to the database type. 
	 */
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "DynamicDbType [value=" + value + "]";
	}
	
}
