package com.asentinel.common.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Test;

import com.asentinel.common.text.BetterNumberFormat;

public class BetterNumberFormatTestCase {

	
	@Test(expected=ParseException.class)
	public void testInvalidNumberFormat() throws ParseException {
		BetterNumberFormat nf = new BetterNumberFormat(NumberFormat.getInstance(Locale.US));
		nf.parse("33dd33");
		fail();
	}
	
	@Test
	public void testValidNumberFormat() throws ParseException {
		BetterNumberFormat nf = new BetterNumberFormat(NumberFormat.getInstance(Locale.US));
		assertEquals(33.33, nf.parse("33.33").doubleValue(), 0.01);
	}
	
	@Test
	public void testNullNumberFormat() throws ParseException {
		BetterNumberFormat nf = new BetterNumberFormat(NumberFormat.getInstance(Locale.US));
		assertNull(nf.parse(null));
	}
	
	@Test(expected=ParseException.class)
	public void testEmptyNumberFormat() throws ParseException {
		BetterNumberFormat nf = new BetterNumberFormat(NumberFormat.getInstance(Locale.US));
		assertNull(nf.parse(""));
		fail();
	}
}
