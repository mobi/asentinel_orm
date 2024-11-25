package com.asentinel.common.jdbc.arrays;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.asentinel.common.jdbc.JdbcUtils;

public class TestArrays {
	private static final Logger log = LoggerFactory.getLogger(TestArrays.class);
	
	@Test
	public void test_setTypeValue_with_null_array() throws SQLException {
		Array a = new Array("test"); // null array
		
		Connection conn = createMock(Connection.class);
		
		PreparedStatement ps = createMock(PreparedStatement.class);
		ps.setNull(1, Types.ARRAY, "test");
		
		replay(ps, conn);
		a.setTypeValue(ps, 1, 0, null);
		verify(ps, conn);
		
		assertSame(null, a.getArrayToPass());
	}
	
	@Test
	public void test_setTypeValue_with_nonNull_array() throws SQLException {
		Object[] javaArray = new Object[] {"a", "b", "c"};
		Array a = new Array("test", javaArray);
		
		java.sql.Array jdbcArray = createMock(java.sql.Array.class);
		
		Connection conn = createMock(Connection.class);
		expect(conn.createArrayOf("test", javaArray)).andReturn(jdbcArray);
		
		PreparedStatement ps = createMock(PreparedStatement.class);
		expect(ps.getConnection()).andReturn(conn);
		ps.setArray(1, jdbcArray);
		
		replay(ps, conn, jdbcArray);
		a.setTypeValue(ps, 1, 0, null);
		verify(ps, conn, jdbcArray);
		
		assertSame(jdbcArray, a.getArrayToPass());
	}

	@Test
	public void test_setTypeValue_with_ArrayFactory() throws SQLException {
		Object[] javaArray = new Object[] {"a", "b", "c"};
		
		java.sql.Array jdbcArray = createMock(java.sql.Array.class);
		PreparedStatement ps = createMock(PreparedStatement.class);
		ArrayFactory af = createMock(ArrayFactory.class);
		expect(af.newArray(ps, "test", javaArray)).andReturn(jdbcArray);
		
		Array a = new Array("test", javaArray, af);
		ps.setArray(1, jdbcArray);
		
		replay(ps, jdbcArray, af);
		a.setTypeValue(ps, 1, 0, null);
		verify(ps, jdbcArray, af);
		
		assertSame(jdbcArray, a.getArrayToPass());
	}

	@Test
	public void testToString1() {
		Integer[] integers = new Integer[Array.NO_ITEMS_IN_LOG - 1];
		for (int i=0; i<integers.length; i++) {
			integers[i] = i;
		}
		NumberArray array = new NumberArray("x", integers);
		String logTxt = array.toString();
		log.debug("testToString1 - logTxt: {}", logTxt);
		assertTrue(logTxt.contains(Arrays.toString(integers)));
	}

	@Test
	public void testToString0() {
		Integer[] integers = new Integer[Array.NO_ITEMS_IN_LOG];
		for (int i=0; i<integers.length; i++) {
			integers[i] = i;
		}
		NumberArray array = new NumberArray("x", integers);
		String logTxt = array.toString();
		log.debug("testToString1 - logTxt: {}", logTxt);
		assertTrue(logTxt.contains(Arrays.toString(integers)));
	}
	

	@Test
	public void testToString2() {
		Integer[] integers = new Integer[Array.NO_ITEMS_IN_LOG + 1];
		for (int i=0; i<integers.length; i++) {
			integers[i] = i;
		}
		NumberArray array = new NumberArray("x", integers);
		String logTxt = array.toString();
		log.debug("testToString2 - logTxt: {}", logTxt);
		assertTrue(logTxt.contains(Arrays.toString(Arrays.copyOf(integers, Array.NO_ITEMS_IN_LOG))));
	}

	@Test
	public void testToStringWithNull() {
		NumberArray array = new NumberArray("x", (Number[]) null);
		String logTxt = array.toString();
		log.debug("testToStringWithNull - logTxt: {}", logTxt);
		assertTrue(logTxt.contains("null"));
	}
	
	@Test
	public void testNumberArrayWithIntArray() {
		int[] integers = {1,2,3};
		NumberArray a = new NumberArray("test", integers);
		assertEquals(integers.length, a.getObjects().length);
		
		for (int i = 0; i < integers.length; i++) {
			assertEquals(integers[i], a.getObjects()[i]);
		}
	}
	
	@Test
	public void testNumberArrayWithNullIntArray() {
		int[] integers = null;
		NumberArray a = new NumberArray("test", integers);
		assertNull(a.getObjects());
	}
	
	
	@Test
	public void testStringArrayWithLongString() {
		String[] strings = {"aaa", null, "ccc"};
		String testStr = "";
		for (int i=0; i < JdbcUtils.LOG_MAX_SIZE + 1; i++) {
			testStr += "X";
		}
		strings[1] = testStr;
		StringArray a = new StringArray("test", strings);
		String logTxt = a.toString();
		log.debug("testStringArrayWithLongString - logTxt: {}", logTxt);
	}
	
	@Test
	public void testStringArray() {
		String[] strings = {"aaa", "bbb", "ccc"};
		StringArray a = new StringArray("test", strings);
		String logTxt = a.toString();
		log.debug("testStringArray - logTxt: {}", logTxt);
		for (String s: strings) {
			assertTrue(logTxt.contains(s));
		}
	}

	@Test
	public void testStringArrayWithOverflow() {
		String[] strings = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg"};
		StringArray a = new StringArray("test", strings);
		String logTxt = a.toString();
		log.debug("testStringArrayWithOverflow - logTxt: {}", logTxt);
		for (int i=0; i < Array.NO_ITEMS_IN_LOG; i++) {
			assertTrue(logTxt.contains(strings[i]));
		}
		for (int i=Array.NO_ITEMS_IN_LOG; i < strings.length; i++) {
			assertFalse(logTxt.contains(strings[i]));
		}
	}
	
	@Test
	public void testEmptyStringArray() {
		String[] strings = {};
		StringArray a = new StringArray("test", strings);
		String logTxt = a.toString();
		log.debug("testEmptyStringArray - logTxt: {}", logTxt);
	}
	
	@Test
	public void testNullStringArray() {
		String[] strings = null;
		StringArray a = new StringArray("test", strings);
		String logTxt = a.toString();
		log.debug("testNullStringArray - logTxt: {}", logTxt);
	}
}
