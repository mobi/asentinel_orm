package com.asentinel.common.jdbc;

import org.springframework.jdbc.core.RowMapper;

/**
 * Default implementation for the {@link RowMapperFactory}. This class
 * is stateless so it is reusable and thread safe.
 * 
 * @author Razvan Popian
 */
public class DefaultRowMapperFactory extends LobHandlerSupport implements RowMapperFactory {

	/**
	 * Delegates mapper creation to {@link ResultSetUtils#rowMapperForClass(Class, org.springframework.jdbc.support.lob.LobHandler)}.
	 * Consequently this method can return {@code null} if the {@code clasz} argument is null.
	 */
	@Override
	public <T> RowMapper<T> getInstance(Class<T> clasz) {
		return ResultSetUtils.rowMapperForClass(clasz, getLobHandler());
	}

}
