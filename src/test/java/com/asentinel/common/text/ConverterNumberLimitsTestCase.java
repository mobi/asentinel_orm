package com.asentinel.common.text;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.*;

public class ConverterNumberLimitsTestCase {
	private final Converter defaultConverter = new StandardConverter();
	private final Converter customConverter = new StandardConverter(Locale.US);

	@Test
	public void testIntegerConverter_Ok() throws ConversionException {
		assertEquals(0, defaultConverter.convertToInteger(String.valueOf(0), null), 0);
	}

	@Test
	public void testIntegerConverter_MaxLimit_Ok() throws ConversionException {
		assertEquals(Integer.MAX_VALUE, defaultConverter.convertToInteger(String.valueOf(Integer.MAX_VALUE), null), 0);
	}

	@Test
	public void testIntegerConverter_MinLimit_Ok() throws ConversionException {
		assertEquals(Integer.MIN_VALUE, customConverter.convertToInteger(String.valueOf(Integer.MIN_VALUE), null), 0);
	}

	@Test
	public void testIntegerConverter_MaxLimit_Error() {
		long maxValue = Integer.MAX_VALUE + 1L;

		ConversionException ex = assertThrows(ConversionException.class,
				() -> defaultConverter.convertToInteger(String.valueOf(maxValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}

	@Test
	public void testIntegerConverter_MinLimit_Error() {
		long minValue = Integer.MIN_VALUE - 1L;
		ConversionException ex = assertThrows(ConversionException.class,
				() -> customConverter.convertToInteger(String.valueOf(minValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}

	@Test
	public void testLongConverter_Ok() throws ConversionException {
		assertEquals(0, defaultConverter.convertToLong(String.valueOf(0), null), 0);
	}

	@Test
	public void testLongConverter_MaxLimit_Ok() throws ConversionException {
		assertEquals(Long.MAX_VALUE, defaultConverter.convertToLong(String.valueOf(Long.MAX_VALUE), null), 0);
	}

	@Test
	public void testLongConverter_MinLimit_Ok() throws ConversionException {
		assertEquals(Long.MIN_VALUE, customConverter.convertToLong(String.valueOf(Long.MIN_VALUE), null), 0);

	}

	@Test
	public void testLongConverter_MaxLimit_Error() {
		BigDecimal maxValue = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.valueOf(1));
		ConversionException ex = assertThrows(ConversionException.class,
				() -> defaultConverter.convertToLong(String.valueOf(maxValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}

	@Test
	public void testLongConverter_MinLimit_Error() {
		BigDecimal minValue = BigDecimal.valueOf(Long.MIN_VALUE).add(BigDecimal.valueOf(-1));
		ConversionException ex = assertThrows(ConversionException.class,
				() -> customConverter.convertToLong(String.valueOf(minValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}

	@Test
	public void testDoubleConverter_Ok() throws ConversionException {
		assertEquals(0, defaultConverter.convertToDouble(String.valueOf(0), null), 0);
	}

	@Test
	public void testDoubleConverter_MaxLimit_Ok() throws ConversionException {
		assertEquals(Double.MAX_VALUE, defaultConverter.convertToDouble(String.valueOf(Double.MAX_VALUE), null), 0);
	}

	@Test
	public void testDoubleConverter_MinLimit_Ok() throws ConversionException {
		assertEquals(-Double.MAX_VALUE, customConverter.convertToDouble(String.valueOf(-Double.MAX_VALUE), null), 0);
	}

	@Test
	public void testDoubleConverter_MaxLimit_Error() {
		BigDecimal maxValue = new BigDecimal(Double.MAX_VALUE).add(BigDecimal.valueOf(1.1));
		ConversionException ex = assertThrows(ConversionException.class,
				() -> defaultConverter.convertToDouble(String.valueOf(maxValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}

	@Test
	public void testDoubleConverter_MinLimit_Error() {
		BigDecimal minValue = BigDecimal.valueOf(-Double.MAX_VALUE).add(BigDecimal.valueOf(-1.1));
		ConversionException ex = assertThrows(ConversionException.class,
				() -> customConverter.convertToDouble(String.valueOf(minValue), null));
		assertTrue(ex.isOutOfBoundsError());
	}
}
