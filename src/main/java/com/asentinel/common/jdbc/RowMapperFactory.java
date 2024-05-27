package com.asentinel.common.jdbc;

import org.springframework.jdbc.core.RowMapper;

/**
 * Factory for creating RowMapper instances for a Class object.
 * Implementations should be thread safe, reusable instances. The {@link #getInstance(Class)}
 * method should return either an existing thread safe RowMapper or
 * it creates a new one. 
 *
 * @see DefaultRowMapperFactory
 * 
 * @author Razvan Popian
 */
public interface RowMapperFactory {

	public <T> RowMapper<T> getInstance(Class<T> clasz);
}
