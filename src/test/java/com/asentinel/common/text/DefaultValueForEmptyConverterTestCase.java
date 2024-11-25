package com.asentinel.common.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;

import com.asentinel.common.util.Utils;

public class DefaultValueForEmptyConverterTestCase {
	
	Converter m = mock(Converter.class);
	DefaultValueForEmptyConverter c = new DefaultValueForEmptyConverter(m);
	
	@Test
	public void testDefaultValue() throws ConversionException {
		assertEquals(0, c.convertToInteger("", null).intValue());
		assertEquals(0, c.convertToLong("", null).longValue());
		assertEquals(0, c.convertToDouble("", null), 0.00001);
		assertEquals(false, c.convertToBoolean("", null));
		assertEquals("", c.convertToString("", null));
		assertNull(c.convertToDate("", null));
		assertNull(c.convertToEntity("", null, StringBuilder.class));

		assertEquals(0, c.convertToInteger(null, null).intValue());
		assertEquals(0, c.convertToLong(null, null).longValue());
		assertEquals(0, c.convertToDouble(null, null), 0.00001);
		assertEquals(false, c.convertToBoolean(null, null));
		assertEquals("", c.convertToString(null, null));
		assertNull(c.convertToDate(null, null));
		assertNull(c.convertToEntity(null, null, StringBuilder.class));
		
		verifyNoMoreInteractions(m);
	}
	
	@Test
	public void testNonDefaultValue() throws ConversionException {
		when(m.convertToInteger("10", null)).thenReturn(10);
		when(m.convertToLong("10", null)).thenReturn(10L);
		when(m.convertToDouble("10", null)).thenReturn(10d);
		when(m.convertToBoolean("true", null)).thenReturn(true);
		when(m.convertToString("test", null)).thenReturn("test-converted");
		when(m.convertToDate("1/1/2000", null)).thenReturn(Utils.toDate(LocalDate.of(2000, 1, 1)));
		when(m.convertToEntity("abc", null, String.class)).thenReturn("abc-entity");
		
		assertEquals(10, c.convertToInteger("10", null).intValue());
		assertEquals(10, c.convertToLong("10", null).longValue());
		assertEquals(10, c.convertToDouble("10", null), 0.00001);
		assertEquals(true, c.convertToBoolean("true", null));
		assertEquals("test-converted", c.convertToString("test", null));
		assertEquals(Utils.toDate(LocalDate.of(2000, 1, 1)), c.convertToDate("1/1/2000", null));
		assertEquals("abc-entity", c.convertToEntity("abc", null, String.class));
	}
}
