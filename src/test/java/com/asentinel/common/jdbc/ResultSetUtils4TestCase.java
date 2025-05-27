package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.Test;

import com.asentinel.common.util.Utils;


/**
 * Test the getXxxValue() and getXxxObject() methods/
 */
public class ResultSetUtils4TestCase {
	
	// Integer tests
	
	@Test
	public void testIntegerColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(anyInt())).andReturn(0).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Integer i = ResultSetUtils.getIntObject(rs, 0);
		assertEquals(Integer.valueOf(0), i);
		
		i = ResultSetUtils.getIntObject(rs, 0, true);
		assertNull("The result should be null.", i);

		int i2 = ResultSetUtils.getIntValue(rs, 0);
		assertEquals(0, i2);

		verify(rs);
	}

	@Test
	public void testIntegerColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(anyObject())).andReturn(0).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Integer i = ResultSetUtils.getIntObject(rs, "");
		assertEquals(Integer.valueOf(0), i);
		
		i = ResultSetUtils.getIntObject(rs, "", true);
		assertNull("The result should be null.", i);

		int i2 = ResultSetUtils.getIntValue(rs, "");
		assertEquals(0, i2);

		verify(rs);
	}
	
	@Test
	public void testIntegerColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(anyInt())).andReturn(10).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Integer i = ResultSetUtils.getIntObject(rs, 0);
		assertEquals(Integer.valueOf(10), i);
		
		i = ResultSetUtils.getIntObject(rs, 0, true);
		assertEquals(Integer.valueOf(10), i);
		
		int i2 = ResultSetUtils.getIntValue(rs, 0);
		assertEquals(10, i2);

		verify(rs);
	}

	@Test
	public void testIntegerColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(anyObject())).andReturn(10).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Integer i = ResultSetUtils.getIntObject(rs, "");
		assertEquals(Integer.valueOf(10), i);
		
		i = ResultSetUtils.getIntObject(rs, "", true);
		assertEquals(Integer.valueOf(10), i);
		
		int i2 = ResultSetUtils.getIntValue(rs, "");
		assertEquals(10, i2);

		verify(rs);
	}

	// Long tests
	
	@Test
	public void testLongColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getLong(anyInt())).andReturn(0L).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Long i = ResultSetUtils.getLongObject(rs, 0);
		assertEquals(Long.valueOf(0), i);
		
		i = ResultSetUtils.getLongObject(rs, 0, true);
		assertNull("The result should be null.", i);

		long i2 = ResultSetUtils.getLongValue(rs, 0);
		assertEquals(0, i2);

		verify(rs);
	}

	@Test
	public void testLongColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getLong(anyObject())).andReturn(0L).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Long i = ResultSetUtils.getLongObject(rs, "");
		assertEquals(Long.valueOf(0), i);
		
		i = ResultSetUtils.getLongObject(rs, "", true);
		assertNull("The result should be null.", i);
		
		long i2 = ResultSetUtils.getLongValue(rs, "");
		assertEquals(0, i2);

		verify(rs);
	}
	
	@Test
	public void testLongColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getLong(anyInt())).andReturn(10L).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Long i = ResultSetUtils.getLongObject(rs, 0);
		assertEquals(Long.valueOf(10), i);
		
		i = ResultSetUtils.getLongObject(rs, 0, true);
		assertEquals(Long.valueOf(10), i);

		long i2 = ResultSetUtils.getLongValue(rs, 0);
		assertEquals(10, i2);

		verify(rs);
	}

	@Test
	public void testLongColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getLong(anyObject())).andReturn(10L).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Long i = ResultSetUtils.getLongObject(rs, "");
		assertEquals(Long.valueOf(10), i);
		
		i = ResultSetUtils.getLongObject(rs, "", true);
		assertEquals(Long.valueOf(10), i);

		long i2 = ResultSetUtils.getLongValue(rs, "");
		assertEquals(10, i2);

		verify(rs);
	}

	// Double tests
	
	@Test
	public void testDoubleColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getDouble(anyInt())).andReturn(0d).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Double i = ResultSetUtils.getDoubleObject(rs, 0);
		assertEquals(Double.valueOf(0), i);
		
		i = ResultSetUtils.getDoubleObject(rs, 0, true);
		assertNull("The result should be null.", i);

		double i2 = ResultSetUtils.getDoubleValue(rs, 0);
		assertEquals(0, i2, 0.001);

		verify(rs);
	}

	@Test
	public void testDoubleColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getDouble(anyObject())).andReturn(0d).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Double i = ResultSetUtils.getDoubleObject(rs, "");
		assertEquals(Double.valueOf(0), i);
		
		i = ResultSetUtils.getDoubleObject(rs, "", true);
		assertNull("The result should be null.", i);

		double i2 = ResultSetUtils.getDoubleValue(rs, "");
		assertEquals(0, i2, 0.001);

		verify(rs);
	}
	
	@Test
	public void testDoubleColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getDouble(anyInt())).andReturn(10d).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Double i = ResultSetUtils.getDoubleObject(rs, 0);
		assertEquals(Double.valueOf(10), i);
		
		i = ResultSetUtils.getDoubleObject(rs, 0, true);
		assertEquals(Double.valueOf(10), i);

		double i2 = ResultSetUtils.getDoubleValue(rs, 0);
		assertEquals(10, i2, 0.001);

		verify(rs);
	}

	@Test
	public void testDoubleColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getDouble(anyObject())).andReturn(10d).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Double i = ResultSetUtils.getDoubleObject(rs, "");
		assertEquals(Double.valueOf(10), i);
		
		i = ResultSetUtils.getDoubleObject(rs, "", true);
		assertEquals(Double.valueOf(10), i);

		double i2 = ResultSetUtils.getDoubleValue(rs, "");
		assertEquals(10d, i2, 0.001);

		verify(rs);
	}

	// String tests
	
	@Test
	public void testStringColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		String i = ResultSetUtils.getStringObject(rs, 0);
		assertEquals("", i);
		
		i = ResultSetUtils.getStringObject(rs, 0, true);
		assertNull("The result should be null.", i);

		String i2 = ResultSetUtils.getStringValue(rs, 0);
		assertEquals("", i2);

		verify(rs);
	}

	@Test
	public void testStringColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		String i = ResultSetUtils.getStringObject(rs, "");
		assertEquals("", i);
		
		i = ResultSetUtils.getStringObject(rs, "", true);
		assertNull("The result should be null.", i);

		String i2 = ResultSetUtils.getStringValue(rs, "");
		assertEquals("", i2);

		verify(rs);
	}
	
	@Test
	public void testStringColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyInt())).andReturn("not null").anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		String i = ResultSetUtils.getStringObject(rs, 0);
		assertEquals("not null", i);
		
		i = ResultSetUtils.getStringObject(rs, 0, true);
		assertEquals("not null", i);

		String i2 = ResultSetUtils.getStringValue(rs, 0);
		assertEquals("not null", i2);

		verify(rs);
	}

	@Test
	public void testStringColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyObject())).andReturn("not null").anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		String i = ResultSetUtils.getStringObject(rs, "");
		assertEquals("not null", i);
		
		i = ResultSetUtils.getStringObject(rs, "", true);
		assertEquals("not null", i);

		String i2 = ResultSetUtils.getStringValue(rs, "");
		assertEquals("not null", i2);

		verify(rs);
	}

	// Boolean tests
	
	@Test
	public void testBooleanColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Boolean i = ResultSetUtils.getBooleanObject(rs, 0);
		assertEquals(Boolean.FALSE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, 0, true);
		assertNull("The result should be null.", i);

		boolean i2 = ResultSetUtils.getBooleanValue(rs, 0);
        assertFalse(i2);

		verify(rs);
	}

	@Test
	public void testBooleanColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Boolean i = ResultSetUtils.getBooleanObject(rs, "");
		assertEquals(Boolean.FALSE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, "", true);
		assertNull("The result should be null.", i);

		boolean i2 = ResultSetUtils.getBooleanValue(rs, "");
        assertFalse(i2);

		verify(rs);
	}
	
	@Test
	public void testBooleanColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyInt())).andReturn(" y ").anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Boolean i = ResultSetUtils.getBooleanObject(rs, 0);
		assertEquals(Boolean.TRUE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, 0, true);
		assertEquals(Boolean.TRUE, i);

		boolean i2 = ResultSetUtils.getBooleanValue(rs, 0);
		assertEquals(Boolean.TRUE, i2);

		verify(rs);
	}

	@Test
	public void testBooleanColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyObject())).andReturn(" y ").anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Boolean i = ResultSetUtils.getBooleanObject(rs, "");
		assertEquals(Boolean.TRUE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, "", true);
		assertEquals(Boolean.TRUE, i);
		

		boolean i2 = ResultSetUtils.getBooleanValue(rs, "");
		assertEquals(Boolean.TRUE, i2);

		verify(rs);
	}

	@Test
	public void testRealBooleanColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyInt())).andReturn(Boolean.TRUE).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Boolean i = ResultSetUtils.getBooleanObject(rs, 0);
		assertEquals(Boolean.TRUE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, 0, true);
		assertEquals(Boolean.TRUE, i);
		

		boolean i2 = ResultSetUtils.getBooleanValue(rs, 0);
		assertEquals(Boolean.TRUE, i2);

		verify(rs);
	}
	
	
	@Test
	public void testRealBooleanColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(anyObject())).andReturn(Boolean.TRUE).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Boolean i = ResultSetUtils.getBooleanObject(rs, "");
		assertEquals(Boolean.TRUE, i);
		
		i = ResultSetUtils.getBooleanObject(rs, "", true);
		assertEquals(Boolean.TRUE, i);
		

		boolean i2 = ResultSetUtils.getBooleanValue(rs, "");
		assertEquals(Boolean.TRUE, i2);

		verify(rs);
	}
	
	
	// Date tests
	
	@Test
	public void testDateColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Date d = ResultSetUtils.getDateObject(rs, 0);
		assertNull("The result should be null.", d);
		
		d = ResultSetUtils.getDateValue(rs, 0);
		assertNull("The result should be null.", d);

		verify(rs);
	}

	@Test
	public void testDateColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Date d = ResultSetUtils.getDateObject(rs, "");
		assertNull("The result should be null.", d);
		
		d = ResultSetUtils.getDateValue(rs, "");
		assertNull("The result should be null.", d);

		verify(rs);
	}
	
	@Test
	public void testDateColByIndex() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Date d = ResultSetUtils.getDateObject(rs, 0);
		assertEquals(testDate, d);
		
		d = ResultSetUtils.getDateValue(rs, 0);
		assertEquals(testDate, d);

		verify(rs);
	}

	@Test
	public void testDateColByName() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Date d = ResultSetUtils.getDateObject(rs, "");
		assertEquals(testDate, d);
		
		d = ResultSetUtils.getDateValue(rs, "");
		assertEquals(testDate, d);

		verify(rs);
	}

	// LocalDate tests
	
	@Test
	public void testLocalDateColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalDate d = ResultSetUtils.getLocalDate(rs, 0);
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalDateColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalDate d = ResultSetUtils.getLocalDate(rs, "");
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalDateColByIndex() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalDate d = ResultSetUtils.getLocalDate(rs, 0);
		assertEquals(Utils.toLocalDate(testDate), d);

		verify(rs);
	}
	
	@Test
	public void testLocalDateColByName() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalDate d = ResultSetUtils.getLocalDate(rs, "");
		assertEquals(Utils.toLocalDate(testDate), d);

		verify(rs);
	}

	// LocalTime tests
	
	@Test
	public void testLocalTimeColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalTime d = ResultSetUtils.getLocalTime(rs, 0);
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalTimeColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalTime d = ResultSetUtils.getLocalTime(rs, "");
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalTimeColByIndex() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalTime d = ResultSetUtils.getLocalTime(rs, 0);
		assertEquals(Utils.toLocalTime(testDate), d);

		verify(rs);
	}
	
	@Test
	public void testLocalTimeColByName() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalTime d = ResultSetUtils.getLocalTime(rs, "");
		assertEquals(Utils.toLocalTime(testDate), d);

		verify(rs);
	}
	
	// LocalDateTime tests
	
	@Test
	public void testLocalDateTimeColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalDateTime d = ResultSetUtils.getLocalDateTime(rs, 0);
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalDateTimeColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		LocalDateTime d = ResultSetUtils.getLocalDateTime(rs, "");
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testLocalDateTimeColByIndex() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalDateTime d = ResultSetUtils.getLocalDateTime(rs, 0);
		assertEquals(Utils.toLocalDateTime(testDate), d);

		verify(rs);
	}
	
	@Test
	public void testLocalDateTimeColByName() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		LocalDateTime d = ResultSetUtils.getLocalDateTime(rs, "");
		assertEquals(Utils.toLocalDateTime(testDate), d);

		verify(rs);
	}
	
	
	// Instant tests
	
	@Test
	public void testInstantColByIndex() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Instant d = ResultSetUtils.getInstant(rs, 0);
		assertEquals(testDate.toInstant(), d);

		verify(rs);
	}
	
	@Test
	public void testInstantColByName() throws SQLException {
		Date testDate = new Date(0);
		Timestamp testTimeStamp = new Timestamp(testDate.getTime());
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject(String.class))).andReturn(testTimeStamp).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		Instant d = ResultSetUtils.getInstant(rs, "");
		assertEquals(testDate.toInstant(), d);

		verify(rs);
	}
	
	@Test
	public void testInstantColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Instant d = ResultSetUtils.getInstant(rs, 0);
		assertNull("The result should be null.", d);
		
		verify(rs);
	}
	
	@Test
	public void testInstantColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getTimestamp(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		Instant d = ResultSetUtils.getInstant(rs, "");
		assertNull("The result should be null.", d);
		
		verify(rs);
	}

	// BigDecimal tests
	
	@Test
	public void testBigDecimalColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		BigDecimal i = ResultSetUtils.getBigDecimalObject(rs, 0);
		assertEquals(BigDecimal.ZERO, i);
		
		i = ResultSetUtils.getBigDecimalObject(rs, 0, true);
		assertNull("The result should be null.", i);

		BigDecimal i2 = ResultSetUtils.getBigDecimalValue(rs, 0);
		assertEquals(BigDecimal.ZERO, i2);

		verify(rs);
	}

	@Test
	public void testBigDecimalColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		BigDecimal i = ResultSetUtils.getBigDecimalObject(rs, "");
		assertEquals(BigDecimal.ZERO, i);
		
		i = ResultSetUtils.getBigDecimalObject(rs, "", true);
		assertNull("The result should be null.", i);

		BigDecimal i2 = ResultSetUtils.getBigDecimalValue(rs, "");
		assertEquals(BigDecimal.ZERO, i2);

		verify(rs);
	}
	
	@Test
	public void testBigDecimalColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyInt())).andReturn(BigDecimal.TEN).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		BigDecimal i = ResultSetUtils.getBigDecimalObject(rs, 0);
		assertEquals(BigDecimal.TEN, i);
		
		i = ResultSetUtils.getBigDecimalObject(rs, 0, true);
		assertEquals(BigDecimal.TEN, i);

		BigDecimal i2 = ResultSetUtils.getBigDecimalValue(rs, 0);
		assertEquals(BigDecimal.TEN, i2);

		verify(rs);
	}

	@Test
	public void testBigDecimalColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyObject())).andReturn(BigDecimal.TEN).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		BigDecimal i = ResultSetUtils.getBigDecimalObject(rs, "");
		assertEquals(BigDecimal.TEN, i);
		
		i = ResultSetUtils.getBigDecimalObject(rs, "", true);
		assertEquals(BigDecimal.TEN, i);
		
		BigDecimal i2 = ResultSetUtils.getBigDecimalValue(rs, "");
		assertEquals(BigDecimal.TEN, i2);

		verify(rs);
	}
 
	// BigInteger tests
	
	@Test
	public void testBigIntegerColByIndexWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyInt())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		BigInteger i = ResultSetUtils.getBigIntegerObject(rs, 0);
		assertEquals(BigInteger.ZERO, i);
		
		i = ResultSetUtils.getBigIntegerObject(rs, 0, true);
		assertNull("The result should be null.", i);

		BigInteger i2 = ResultSetUtils.getBigIntegerValue(rs, 0);
		assertEquals(BigInteger.ZERO, i2);

		verify(rs);
	}

	@Test
	public void testBigIntegerColByNameWithNull() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyObject())).andReturn(null).anyTimes();
		expect(rs.wasNull()).andReturn(true).anyTimes();
		
		replay(rs);
		
		BigInteger i = ResultSetUtils.getBigIntegerObject(rs, "");
		assertEquals(BigInteger.ZERO, i);
		
		i = ResultSetUtils.getBigIntegerObject(rs, "", true);
		assertNull("The result should be null.", i);

		BigInteger i2 = ResultSetUtils.getBigIntegerValue(rs, "");
		assertEquals(BigInteger.ZERO, i2);

		verify(rs);
	}
	
	@Test
	public void testBigIntegerColByIndex() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyInt())).andReturn(BigDecimal.TEN).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		BigInteger i = ResultSetUtils.getBigIntegerObject(rs, 0);
		assertEquals(BigInteger.TEN, i);
		
		i = ResultSetUtils.getBigIntegerObject(rs, 0, true);
		assertEquals(BigInteger.TEN, i);

		BigInteger i2 = ResultSetUtils.getBigIntegerValue(rs, 0);
		assertEquals(BigInteger.TEN, i2);

		verify(rs);
	}

	@Test
	public void testBigIntegerColByName() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getBigDecimal(anyObject())).andReturn(BigDecimal.TEN).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		replay(rs);

		BigInteger i = ResultSetUtils.getBigIntegerObject(rs, "");
		assertEquals(BigInteger.TEN, i);
		
		i = ResultSetUtils.getBigIntegerObject(rs, "", true);
		assertEquals(BigInteger.TEN, i);
		
		BigInteger i2 = ResultSetUtils.getBigIntegerValue(rs, "");
		assertEquals(BigInteger.TEN, i2);

		verify(rs);
	}
}
