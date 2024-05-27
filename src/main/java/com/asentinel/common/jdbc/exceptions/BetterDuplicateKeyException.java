package com.asentinel.common.jdbc.exceptions;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.util.StringUtils;

/**
 * {@code DuplicateKeyException} subclass that can provide additional
 * information about the constraint that triggered the exception. It is useful
 * in scenarios where the client needs to determine which one of the unique
 * constraints on a table triggered the exception.<br>
 * This class should be used in tandem with a custom
 * {@link SQLExceptionTranslator}.
 * 
 * @see SQLExceptionTranslator
 * @see JdbcTemplate#setExceptionTranslator(SQLExceptionTranslator)
 * 
 * @author Razvan.Popian
 */
@SuppressWarnings("serial")
public class BetterDuplicateKeyException extends DuplicateKeyException {
	
	private final String constraintName;

	public BetterDuplicateKeyException(String constraintName, Throwable cause) {
		super("Unique key constraint violation." 
				+ (StringUtils.hasText(constraintName) ? " Constraint name: '" + constraintName + "'." : ""),
				cause);
		this.constraintName = constraintName;
	}

	/**
	 * @return the name of the constraint that triggered this exception. Can return
	 *         {@code null} if for some reason the constraint name can not be
	 *         determined.
	 */
	public String getConstraintName() {
		return constraintName;
	}
}

