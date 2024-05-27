package com.asentinel.common.util;

import static com.asentinel.common.util.NestedThrowableUtils.getCause;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

/**
 * @since 1.60.16
 * @author Razvan Popian
 */
public class NestedThrowableUtilsTestCase {
	
	private final Throwable e2 = new ParseException("", 0);
	private final Throwable e1 = new IOException(e2);
	private final Throwable e0 = new RuntimeException(e1);
	
	@Test
	public void nullException() {
		assertTrue(getCause(null, RuntimeException.class).isEmpty());
	}

	@Test
	public void nullTargetType() {
		assertTrue(getCause(e0, null).isEmpty());
	}
	
	@Test
	public void nullExceptionAndTargetType() {
		assertTrue(getCause(null, null).isEmpty());
	}
	
	
	@Test
	public void noCause_NullTargetType() {
		assertTrue(getCause(e2, null).isEmpty());
	}
	
	@Test
	public void noCause_RootCauseMatchesTargetType() {
		assertSame(e2, getCause(e2, ParseException.class).get());
	}

	@Test
	public void noCause_RootCauseDoesNotMatchTargetType() {
		assertTrue(getCause(e2, RuntimeException.class).isEmpty());
	}
	
	
	@Test
	public void targetCauseFirst() {
		assertSame(e0, getCause(e0, RuntimeException.class).get());
	}
	
	@Test
	public void targetCauseSecond() {
		assertSame(e1, getCause(e0, IOException.class).get());
	}

	@Test
	public void targetCauseThird() {		
		assertSame(e2, getCause(e0, ParseException.class).get());
	}
	
	@Test
	public void causeSubclassOfTargetType() {
		Throwable e1 = new NumberFormatException();
		Throwable e0 = new RuntimeException(e1);
		
		assertTrue(getCause(e0, IllegalArgumentException.class).isEmpty());
	}

}
