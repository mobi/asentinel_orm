package com.asentinel.common.jdbc.flavors.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import com.asentinel.common.jdbc.exceptions.BetterDuplicateKeyException;
import com.asentinel.common.jdbc.exceptions.CycleInSelfReferencingTableException;
import com.asentinel.common.jdbc.exceptions.ForeignKeyViolationException;

public class PgBetterSQLExceptionTranslatorTestCase {

	private static final String CONSTRAINT = "test-constraint";
	
	private final SQLExceptionTranslator defaultTranslator = mock(SQLExceptionTranslator.class);
	private final PgBetterSQLExceptionTranslator pgTranslator = new PgBetterSQLExceptionTranslator(defaultTranslator);
	
	private final ServerErrorMessage sem = mock(ServerErrorMessage.class);
	private final PSQLException sqlEx = mock(PSQLException.class);
	{
		when(sqlEx.getServerErrorMessage()).thenReturn(sem);
	}

	@Test
	public void testBetterDuplicateKeyException() {
		DuplicateKeyException ex = new DuplicateKeyException("test", sqlEx);
		when(defaultTranslator.translate("a", "select", sqlEx)).thenReturn(ex);
		when(sem.getConstraint()).thenReturn(CONSTRAINT);
		Exception tex = pgTranslator.translate("a", "select", sqlEx);
		assertTrue(tex instanceof BetterDuplicateKeyException);
		assertSame(ex, tex.getCause());
		assertEquals(CONSTRAINT, ((BetterDuplicateKeyException) tex).getConstraintName());
		assertTrue(tex.getMessage().contains(CONSTRAINT));
	}
	
	@Test
	public void testCycleInSelfReferencingTableException() {
		when(sem.getHint()).thenReturn(PgBetterSQLExceptionTranslator.CYCLE_HINT);
		DataIntegrityViolationException ex = new DataIntegrityViolationException("test", sqlEx);
		when(defaultTranslator.translate("a", "select", sqlEx)).thenReturn(ex);
		Exception tex = pgTranslator.translate("a", "select", sqlEx);
		assertTrue(tex instanceof CycleInSelfReferencingTableException);
		assertSame(ex, tex.getCause());
	}
	
	@Test
	public void testForeignKeyViolationException() {
		when(sem.getHint()).thenReturn(null);
		when(sqlEx.getSQLState()).thenReturn(PgBetterSQLExceptionTranslator.SQL_STATE_FK_VIOLATION);
		when(sem.getConstraint()).thenReturn(CONSTRAINT);
		DataIntegrityViolationException ex = new DataIntegrityViolationException("test", sqlEx);
		when(defaultTranslator.translate("a", "select", sqlEx)).thenReturn(ex);
		Exception tex = pgTranslator.translate("a", "select", sqlEx);
		assertTrue(tex instanceof ForeignKeyViolationException);
		assertSame(ex, tex.getCause());
		assertEquals(CONSTRAINT, ((ForeignKeyViolationException) tex).getConstraintName());
		assertTrue(tex.getMessage().contains(CONSTRAINT));
	}

	@Test
	public void testDataIntegrityViolationException() {
		DataIntegrityViolationException ex = new DataIntegrityViolationException("test", sqlEx);
		when(defaultTranslator.translate("a", "select", sqlEx)).thenReturn(ex);
		assertSame(ex, pgTranslator.translate("a", "select", sqlEx));
	}
}
