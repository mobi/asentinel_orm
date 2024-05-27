package com.asentinel.common.jdbc.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Exception thrown when a cycle is detected in a self referencing table (a
 * table that has a column referencing the primary key - a classic example is an
 * employee hierarchy).<br>
 * This class should be used in tandem with a custom
 * {@link SQLExceptionTranslator}.
 * 
 * @see SQLExceptionTranslator
 * @see JdbcTemplate#setExceptionTranslator(SQLExceptionTranslator)
 * 
 * @author Razvan.Popian
 */
@SuppressWarnings("serial")
public class CycleInSelfReferencingTableException extends DataIntegrityViolationException {

	public CycleInSelfReferencingTableException(String msg) {
		super(msg);
	}
	
	public CycleInSelfReferencingTableException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
