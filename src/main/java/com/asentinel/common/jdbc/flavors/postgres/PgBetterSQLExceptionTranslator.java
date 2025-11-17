package com.asentinel.common.jdbc.flavors.postgres;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import com.asentinel.common.jdbc.exceptions.BetterDuplicateKeyException;
import com.asentinel.common.jdbc.exceptions.CycleInSelfReferencingTableException;
import com.asentinel.common.jdbc.exceptions.ForeignKeyViolationException;
import com.asentinel.common.util.Assert;

/**
 * Postgres specific exception translator that does the following:
 * <li>wraps {@code DuplicateKeyException}s into a
 * {@link BetterDuplicateKeyException} that provides additional information
 * about the constraint that triggered the error
 * <li>wraps a {@code DataIntegrityViolationException} into a
 * {@link CycleInSelfReferencingTableException} if the root cause postgres sql
 * exception hint is "cycle"
 * <li>wraps a {@code DataIntegrityViolationException} into a
 * {@link ForeignKeyViolationException} if a foreign key violation is the cause,
 * allowing access to the name of the constraint that was violated.
 * 
 * @see BetterDuplicateKeyException
 * @see CycleInSelfReferencingTableException
 * @see ForeignKeyViolationException
 * 
 * @author Razvan.Popian
 */
public class PgBetterSQLExceptionTranslator implements SQLExceptionTranslator {
	private final static Logger log = LoggerFactory.getLogger(PgBetterSQLExceptionTranslator.class);

	static final String CYCLE_HINT = "cycle";
	static final String SQL_STATE_FK_VIOLATION = "23503";

	private final SQLExceptionTranslator exTranslator;

	public PgBetterSQLExceptionTranslator(SQLExceptionTranslator exTranslator) {
		Assert.assertNotNull(exTranslator, "exTranslator");
		this.exTranslator = exTranslator;
	}

	@Override
	public DataAccessException translate(String task, String sql, SQLException ex) {
		DataAccessException tex = exTranslator.translate(task, sql, ex);
		if (tex instanceof DuplicateKeyException) {
			return new BetterDuplicateKeyException(getExceptionAttribute(ex, ServerErrorMessage::getConstraint), tex);
		} else if (tex instanceof DataIntegrityViolationException) {
			if (SQL_STATE_FK_VIOLATION.equals(ex.getSQLState())) {
				return new ForeignKeyViolationException(getExceptionAttribute(ex, ServerErrorMessage::getConstraint), tex);
			} else {
				String hint = getExceptionAttribute(ex, ServerErrorMessage::getHint);
				if (CYCLE_HINT.equalsIgnoreCase(hint)) {
					return new CycleInSelfReferencingTableException("Cycle detected.", tex);
				}
			}
		}
		return tex;
	}
	
	private static String getExceptionAttribute(Throwable orgEx, Function<ServerErrorMessage, String> getter) {
		Throwable ex = NestedExceptionUtils.getRootCause(orgEx);
		Throwable cause = ex != null ? ex : orgEx;
		
		if (cause instanceof PSQLException) {
			PSQLException pSqlCause = (PSQLException) cause;
			return Optional.of(pSqlCause)
					.map(PSQLException::getServerErrorMessage)
					.map(getter)
					.orElse(null);
		}
		log.warn("getExceptionAttribute - The most specific cause is not a " + PSQLException.class.getSimpleName() + ". Returning null.");
		return null;
	}
}
