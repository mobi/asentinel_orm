package com.asentinel.common.text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

public class ConversionExceptionTestCase {
	
	@Test
	public void testIsParseError_True() {
		ConversionException ce1 = new ConversionException("", new ParseException("", 0));
		ConversionException ce2 = new ConversionException("", new NumberFormatException());
		assertTrue(ce1.isParseError());
		assertTrue(ce2.isParseError());
	}

	@Test
	public void testIsParseError_False() {
		ConversionException ce1 = new ConversionException("");
		ConversionException ce2 = new ConversionException("", new OutOfBoundsException());
		assertFalse(ce1.isParseError());
		assertFalse(ce2.isParseError());
	}

	@Test
	public void testIsOutOfBoundsError_True() {
		ConversionException ce = new ConversionException("", new OutOfBoundsException());
		assertTrue(ce.isOutOfBoundsError());
	}

	@Test
	public void testIsOutOfBoundsError_False() {
		ConversionException ce1 = new ConversionException("", null);
		ConversionException ce2 = new ConversionException("", new NumberFormatException());
		assertFalse(ce1.isOutOfBoundsError());
		assertFalse(ce2.isOutOfBoundsError());
	}
}
