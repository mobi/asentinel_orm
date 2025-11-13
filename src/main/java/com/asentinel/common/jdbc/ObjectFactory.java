package com.asentinel.common.jdbc;


/**
 * Simple interface that defines just one method that creates objects.
 * This is primarily used by the custom {@code RowMapper} implementations. 
 * The returned object should not be {@code null}.
 * 
 * @see AbstractReflectionRowMapper	
 * 
 * @author Razvan Popian
 */
public interface ObjectFactory<T> {
	
	// TODO: add support for building objects without a no-args contructor
	// we need this in order to create immutable (all fields final) objects
	// from the ORM
	// An idea would be to extend this interface and have a varargs method newObject(Object ... parameters)
	// that would call the contructor with the right arguments
	
	T newObject() throws IllegalStateException;
}
