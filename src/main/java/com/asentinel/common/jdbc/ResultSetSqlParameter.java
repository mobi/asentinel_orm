package com.asentinel.common.jdbc;

import java.sql.Types;

import org.springframework.jdbc.core.ResultSetSupportingSqlParameter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.util.Assert;

/**
 * SqlParameter that supports resultsets. The resultsets can be processed
 * either by a {@link RowMapper} or by a {@link RowCallbackHandler}.
 * 
 * @see InOutCall#call(String, ResultSetSqlParameter[], Object...)
 * 
 * @author Razvan Popian
 */
public class ResultSetSqlParameter extends ResultSetSupportingSqlParameter {
	
	// This is not used if instances of this class are used with
	// InOutCallTemplate or SqlQueryTemplate.
	private final static int cursorType = Types.OTHER;

	/**
	 * Constructor that initializes this sql parameter with
	 * a {@link RowCallbackHandler}. 
	 */
	public ResultSetSqlParameter(RowCallbackHandler rch) {
		super("RS", cursorType, rch);
		Assert.assertNotNull(rch, "rch");
	}

	/**
	 * Constructor that initializes this sql parameter with
	 * a {@link RowMapper}. 
	 */
	public ResultSetSqlParameter(RowMapper<?> mapper) {
		super("RS", cursorType, mapper);
		Assert.assertNotNull(mapper, "mapper");
	}

	/**
	 * Constructor that initializes this sql parameter with
	 * a {@link RowMapper} created using the class clasz
	 * and the {@link RowMapperFactory} parameters. 
	 * @param clasz class for which to create a {@link RowMapper}.
	 * @param rowMapperFactory the factory to use for mapper creation.
	 * 
	 * @see RowMapperFactory
	 */
	public ResultSetSqlParameter(Class<?> clasz, RowMapperFactory rowMapperFactory) {
		super("RS", cursorType, rowMapperFactory.getInstance(clasz));
	}
	
	/**
	 * @see #ResultSetSqlParameter(Class, LobHandler)
	 */
	public ResultSetSqlParameter(Class<?> clasz) {
		this(clasz, (LobHandler) null);
	}

	/**
	 * Constructor that initializes this parameter with
	 * a {@link RowMapper} created using the class clasz.
	 * The mapper is created by calling {@link ResultSetUtils#rowMapperForClass(Class, LobHandler)}
	 * 
	 * @param clasz class for which to create a {@link RowMapper}.
	 * @param lobHandler the {@code LobHandler} to use, can be {@code null} if no BLOB fields are pulled
	 * 			from the database.
	 * 
	 * @see ResultSetUtils#rowMapperForClass(Class, LobHandler)
	 */
	public ResultSetSqlParameter(Class<?> clasz, LobHandler lobHandler) {
		super("RS", cursorType, ResultSetUtils.rowMapperForClass(clasz, lobHandler));
	}
	
	/**
	 * @return true if the resultset will be handled by
	 * 			a {@link RowCallbackHandler}
	 * 			false otherwise
	 */
	public boolean isRowCallbackHandler() {
		return getRowCallbackHandler() != null;
	}

	/**
	 * @return true if the resultset will be handled by
	 * 			a {@link RowMapper}
	 * 			false otherwise
	 */
	public boolean isRowMapper() {
		return getRowMapper() != null;
	}
}
