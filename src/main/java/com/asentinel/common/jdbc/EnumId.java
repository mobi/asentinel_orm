package com.asentinel.common.jdbc;

import com.asentinel.common.jdbc.flavors.DefaultPreparedStatementParametersSetter;

/**
 * Interface to be implemented by enums. It is used for converting enums to
 * their database table column representation and viceversa. All the enum
 * constants should return the same type of object and should not return
 * {@code null}. Each enum constant should have a unique id.
 * 
 * @param <T> the type of the enum id
 * 
 * @see DefaultPreparedStatementParametersSetter#setParameter(java.sql.PreparedStatement,
 *      int, int, Object)
 * @see ResultSetUtils#getEnum(java.sql.ResultSet, String, Class)
 * @see ConversionSupport
 * 
 * @author Razvan.Popian
 */
public interface EnumId<T> {
	
	/**
	 * @return the unique id of this enum constant, never {@code null}.
	 */
	T getId();

}
