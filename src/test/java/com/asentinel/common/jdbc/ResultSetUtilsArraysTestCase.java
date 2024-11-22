package com.asentinel.common.jdbc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResultSetUtilsArraysTestCase {
	
	private Array array; 
	private ResultSet rs;
	
	@Before
	public void setup() {
		array = createMock(Array.class);

		rs = createMock(ResultSet.class);
	}

	@Test
	public void testGetStringArray() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(array).anyTimes();
		expect(array.getArray()).andReturn(new String[]{"a", "b"});
		array.free();
		
		replay(rs, array);
		String[] strings = ResultSetUtils.getStringArray(rs, 1);
		verify(rs, array);
		
		assertEquals(2, strings.length);
		assertEquals("a", strings[0]);
		assertEquals("b", strings[1]);
	}
	
	@Test
	public void testGetStringArrayNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		String[] strings = ResultSetUtils.getStringArray(rs, 1);
		verify(rs, array);
		
		assertEquals(0, strings.length);
	}

	@Test
	public void testGetStringArrayNull_AllowNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		String[] strings = ResultSetUtils.getStringArray(rs, 1, true);
		verify(rs, array);
		
		assertNull(strings);
	}

	@Test
	public void testGetIntArray() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(array).anyTimes();
		expect(array.getArray()).andReturn(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
		array.free();

		replay(rs, array);
		int[] numbers = ResultSetUtils.getIntArray(rs, 1);
		verify(rs, array);
		
		assertEquals(2, numbers.length);
		assertEquals(1, numbers[0]);
		assertEquals(2, numbers[1]);
	}
	
	@Test
	public void testGetIntArrayNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		int[] integers = ResultSetUtils.getIntArray(rs, 1);
		verify(rs, array);
		
		assertEquals(0, integers.length);
	}

	@Test
	public void testGetIntArrayNull_Allow() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		int[] integers = ResultSetUtils.getIntArray(rs, 1, true);
		verify(rs, array);
		
		assertNull(integers);
	}
	

	@Test
	public void testGetLongArray() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(array).anyTimes();
		expect(array.getArray()).andReturn(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
		array.free();
		
		replay(rs, array);
		long[] numbers = ResultSetUtils.getLongArray(rs, 1);
		verify(rs, array);
		
		assertEquals(2, numbers.length);
		assertEquals(1, numbers[0]);
		assertEquals(2, numbers[1]);
	}

	@Test
	public void testGetLongArrayNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		long[] longs = ResultSetUtils.getLongArray(rs, 1);
		verify(rs, array);
		
		assertEquals(0, longs.length);
	}

	@Test
	public void testGetLongArrayNull_Allow() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		long[] longs = ResultSetUtils.getLongArray(rs, 1, true);
		verify(rs, array);
		
		assertNull(longs);
	}

	@Test
	public void testGetDoubleArray() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(array).anyTimes();
		expect(array.getArray()).andReturn(new BigDecimal[]{new BigDecimal("1.1"), new BigDecimal("2.2")});
		array.free();
		
		replay(rs, array);
		double[] numbers = ResultSetUtils.getDoubleArray(rs, 1);
		verify(rs, array);
		
		assertEquals(2, numbers.length);
		assertEquals(1.1, numbers[0], 0.01);
		assertEquals(2.2, numbers[1], 0.01);
	}

	@Test
	public void testGetDoubleArrayNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		double[] numbers = ResultSetUtils.getDoubleArray(rs, 1);
		verify(rs, array);
		
		assertEquals(0, numbers.length);
	}

	@Test
	public void testGetDoubleArrayNull_Allow() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		double[] numbers = ResultSetUtils.getDoubleArray(rs, 1, true);
		verify(rs, array);
		
		assertNull(numbers);
	}

	
	@Test
	public void testGetBigIntegerArray() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(array).anyTimes();
		expect(array.getArray()).andReturn(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
		array.free();
		
		replay(rs, array);
		BigInteger[] numbers = ResultSetUtils.getBigIntegerArray(rs, 1);
		verify(rs, array);
		
		assertEquals(2, numbers.length);
		assertEquals(1, numbers[0].intValue());
		assertEquals(2, numbers[1].intValue());
	}

	@Test
	public void testGetBigIntegerArrayNull() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		BigInteger[] numbers = ResultSetUtils.getBigIntegerArray(rs, 1);
		verify(rs, array);
		
		assertEquals(0, numbers.length);
	}

	@Test
	public void testGetBigIntegerArrayNull_Allow() throws SQLException {
		expect(rs.getArray(anyInt())).andReturn(null).anyTimes();
		
		replay(rs, array);
		BigInteger[] numbers = ResultSetUtils.getBigIntegerArray(rs, 1, true);
		verify(rs, array);
		
		assertNull(numbers);
	}
}
