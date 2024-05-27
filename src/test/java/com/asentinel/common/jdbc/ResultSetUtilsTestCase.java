package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Tests the following:
 * 
 * 	- ResultSetUtils#asXXX methods in combination with ReflectionRowMapper
 * 	and also the exception situations for the ReflectionRowMapper.
 * 
 * 	- {@link ResultSetUtils#processResultSet(ResultSet, RowCallbackHandler)}
 * 
 * 	- the {@link ReflectionRowMapper} with best effort. 
 * 
 * 	- type conversions 
 */
public class ResultSetUtilsTestCase {
	private final static Logger log = LoggerFactory.getLogger(ResultSetUtilsTestCase.class);
	
	final static String VALUE_INT_COL = "ValueInt";
	final static String VALUE_LONG_COL = "ValueLong";
	final static String VALUE_DOUBLE_COL = "ValueDouble";
	final static String VALUE_STRING_COL = "ValueString";
	final static String VALUE_DATE_COL = "ValueDate";
	final static String VALUE_BOOLEAN_COL = "ValueBoolean";
	final static String VALUE_BIG_DECIMAL_COL = "ValueBigDecimal";	
	final static String VALUE_BIG_INTEGER_COL = "ValueBigInteger";
	final static String VALUE_BYTES_COL = "ValueBytes";
	final static String VALUE_INPUT_STREAM_COL = "ValueInputStream";
	final static String VALUE_NUMBER_COL = "ValueNumber";
	final static String VALUE_STRING_ARRAY_COL = "ValueStringArray";
	final static String VALUE_NUMBER_ARRAY_COL = "ValueNumberArray";
	final static String VALUE_BIG_DECIMAL_ARRAY_COL = "ValueBigDecimalArray";
	final static String VALUE_INT_ARRAY_COL = "ValueIntArray";
	final static String VALUE_LONG_ARRAY_COL = "ValueLongArray";
	final static String VALUE_DOUBLE_ARRAY_COL = "ValueDoubleArray";
	final static String VALUE_BIGINTEGER_ARRAY_COL = "ValueBigIntegerArray";
	final static String VALUE_LOCAL_DATE_COL = "ValueLocalDate";
	final static String VALUE_LOCAL_TIME_COL = "ValueLocalTime";
	final static String VALUE_LOCAL_DATE_TIME_COL = "ValueLocalDateTime";
	final static String VALUE_ENUM_COL = "ValueEnum";
	
	
	private final static String STRING_PREFIX = "string_";
	
	private ResultSet rs = createMock(ResultSet.class);
	private ResultSetMetaData rsmd = createMock(ResultSetMetaData.class);
	
	private Array stringArray;
	private String[] stringArrayValue = new String[] {"s1", "s2"};
	
	
	private Array numberArray;
	private Number[] numberArrayValue = new Number[] {new BigDecimal(1), new BigDecimal(2)};

	private Array bigDecimalArray;
	private BigDecimal[] bigDecimalArrayValue = new BigDecimal[] {new BigDecimal(1), new BigDecimal(2)};
	
	private Array intArray;
	private int[] intTestArrayValue = new int[] {1, 2};
	private BigDecimal[] intArrayValue = new BigDecimal[] {new BigDecimal(intTestArrayValue[0]), new BigDecimal(intTestArrayValue[1])};

	private Array longArray;
	private long[] longTestArrayValue = new long[] {1, 2};
	private BigDecimal[] longArrayValue = new BigDecimal[] {new BigDecimal(longTestArrayValue[0]), new BigDecimal(longTestArrayValue[1])};

	private Array doubleArray;
	private double[] doubleTestArrayValue = new double[] {1.1, 2.2};
	private BigDecimal[] doubleArrayValue = new BigDecimal[] {new BigDecimal(doubleTestArrayValue[0]), new BigDecimal(doubleTestArrayValue[1])};

	private Array bigIntegerArray;
	private BigInteger[] bigIntegerTestArrayValue = new BigInteger[] {new BigInteger(String.valueOf(1)), new BigInteger(String.valueOf(2))};
	private BigDecimal[] bigIntegerArrayValue = new BigDecimal[] {new BigDecimal(intTestArrayValue[0]), new BigDecimal(intTestArrayValue[1])};
	
	
	
	@Before
	public void setup() throws SQLException {
		
		// for simplicity we test with one array for all rows
		stringArray = createMock(Array.class);
		numberArray = createMock(Array.class);
		bigDecimalArray = createMock(Array.class);
		intArray = createMock(Array.class);
		longArray = createMock(Array.class);
		doubleArray = createMock(Array.class);
		bigIntegerArray = createMock(Array.class);
		
	}
	
	private void prepareArrayMock(Array array, Object ret) throws SQLException {
		expect(array.getArray()).andReturn(ret).anyTimes();
		array.free();
		expectLastCall().anyTimes();
	}
	
	
	
	@Test
	public void testReflectionRowMapper() throws SQLException {
		log.info("testReflectionRowMapper - start");
		
		Statement st = createMock(Statement.class);
		expect(st.getConnection()).andReturn(null).anyTimes();
		
		String[] cols = new String[]{
				VALUE_INT_COL,
				VALUE_LONG_COL,
				VALUE_DOUBLE_COL,
				VALUE_STRING_COL,
				VALUE_DATE_COL,
				VALUE_BOOLEAN_COL,
				VALUE_BIG_DECIMAL_COL,
				VALUE_BIG_INTEGER_COL,
				VALUE_BYTES_COL,
				VALUE_INPUT_STREAM_COL,
				VALUE_NUMBER_COL,
				VALUE_STRING_ARRAY_COL,
				VALUE_NUMBER_ARRAY_COL,
				VALUE_BIG_DECIMAL_ARRAY_COL,
				VALUE_INT_ARRAY_COL,
				VALUE_LONG_ARRAY_COL,
				VALUE_DOUBLE_ARRAY_COL,
				VALUE_BIGINTEGER_ARRAY_COL,
				VALUE_LOCAL_DATE_COL,
				VALUE_LOCAL_TIME_COL,
				VALUE_LOCAL_DATE_TIME_COL,
				VALUE_ENUM_COL
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int ii = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(ii)).andReturn(col).anyTimes();
			ii++;
		}
		
		// we need anyTimes() because we use the same array for all rows
		prepareArrayMock(stringArray, stringArrayValue);
		prepareArrayMock(numberArray, numberArrayValue);
		prepareArrayMock(bigDecimalArray, bigDecimalArrayValue);
		prepareArrayMock(intArray, intArrayValue);
		prepareArrayMock(longArray, longArrayValue);
		prepareArrayMock(doubleArray, doubleArrayValue);
		prepareArrayMock(bigIntegerArray, bigIntegerArrayValue);
		
		
		final int SIZE = 10;
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(new Date().getTime());
		try {
			expect(rs.getMetaData()).andReturn(rsmd).anyTimes();
			for (int i=1; i<=SIZE; i++) {
				int cc = 0;
				expect(rs.next()).andReturn(true);
				expect(rs.getRow()).andReturn(i);
				expect(rs.getInt(VALUE_INT_COL)).andReturn(i); cc++;
				expect(rs.wasNull()).andReturn(false);
				expect(rs.getLong(VALUE_LONG_COL)).andReturn((long)i); cc++;
				expect(rs.wasNull()).andReturn(false);
				expect(rs.getDouble(VALUE_DOUBLE_COL)).andReturn(i + .5); cc++;
				expect(rs.wasNull()).andReturn(false);
				expect(rs.getObject(VALUE_STRING_COL)).andReturn(STRING_PREFIX + i); cc++;
				expect(rs.getTimestamp(VALUE_DATE_COL)).andReturn(sqlTimestamp); cc++;
				expect(rs.getObject(VALUE_BOOLEAN_COL)).andReturn(new StringBuilder().append("Y").append((char)0).append((char)0).append((char)0).toString()); cc++;
				expect(rs.getBigDecimal(VALUE_BIG_DECIMAL_COL)).andReturn(new BigDecimal(i + .5)); cc++;
				expect(rs.getBigDecimal(VALUE_BIG_INTEGER_COL)).andReturn(new BigDecimal(i)); cc++;
				expect(rs.findColumn(VALUE_BYTES_COL)).andReturn(cc);
				expect(rs.getBytes(cc++)).andReturn(new byte[]{(byte)i});
				expect(rs.findColumn(VALUE_INPUT_STREAM_COL)).andReturn(cc);
				expect(rs.getBinaryStream(cc++)).andReturn(new ByteArrayInputStream( new byte[]{(byte)(i * 2)}));
				expect(rs.getBigDecimal(VALUE_NUMBER_COL)).andReturn(new BigDecimal(i));
				expect(rs.getArray(VALUE_STRING_ARRAY_COL)).andReturn(stringArray);
				expect(rs.getArray(VALUE_NUMBER_ARRAY_COL)).andReturn(numberArray);
				expect(rs.getArray(VALUE_BIG_DECIMAL_ARRAY_COL)).andReturn(bigDecimalArray);
				expect(rs.getArray(VALUE_INT_ARRAY_COL)).andReturn(intArray);
				expect(rs.getArray(VALUE_LONG_ARRAY_COL)).andReturn(longArray);
				expect(rs.getArray(VALUE_DOUBLE_ARRAY_COL)).andReturn(doubleArray);
				expect(rs.getArray(VALUE_BIGINTEGER_ARRAY_COL)).andReturn(bigIntegerArray);
				expect(rs.getTimestamp(VALUE_LOCAL_DATE_COL)).andReturn(sqlTimestamp);
				expect(rs.getTimestamp(VALUE_LOCAL_TIME_COL)).andReturn(sqlTimestamp);
				expect(rs.getTimestamp(VALUE_LOCAL_DATE_TIME_COL)).andReturn(sqlTimestamp);
				expect(rs.getObject(VALUE_ENUM_COL)).andReturn("AAA");
			}
			expect(rs.next()).andReturn(false);
			if (log.isTraceEnabled()) {
				expect(rs.getFetchSize()).andReturn(10);
			}
			rs.close();
			
			replay(rs, rsmd, st, 
					stringArray, numberArray, bigDecimalArray,
					intArray, longArray, doubleArray, bigIntegerArray
			);
			
			long t0, t1;
			t0 = System.nanoTime();
			TBean[] list = ResultSetUtils.asArray(rs, TBean.class);
			t1 = System.nanoTime();
			log.info("testReflectionRowMapper - conversion took: " + (t1 - t0)/1000000d + " ms");
			verify(rs, rsmd, st, 
					stringArray, numberArray, bigDecimalArray, 
					intArray, longArray, doubleArray, bigIntegerArray
			);
			
			assertEquals(SIZE, list.length);
			for (int i=1; i<=list.length; i++) {
				TBean b = list[i-1];
				assertNotNull(b);
				assertEquals(i, b.getValueInt());
				assertEquals(i, b.getValueLong());
				assertEquals(i + .5, b.getValueDouble(), 0.0001);
				assertEquals(STRING_PREFIX + i, b.getValueString());
				assertEquals(sqlTimestamp.getTime(), b.getValueDate().getTime());
				assertEquals(true, b.isValueBoolean());
				assertEquals(new BigDecimal(i + .5), b.getValueBigDecimal());
				assertEquals(new BigInteger(String.valueOf(i)), b.getValueBigInteger());
				assertEquals(1, b.getValueBytes().length);
				assertEquals((byte)i, b.getValueBytes()[0]);
				assertEquals(new BigDecimal(i), b.getValueNumber());
				
				InputStream in = b.getValueInputStream();
				List<Byte> bytes = new ArrayList<Byte>();
				int ib;
				try {
					while( (ib = in.read()) > 0) {
						bytes.add((byte)ib);
					}
					in.close();
				} catch (IOException e) {
					assertTrue("Should not throw IOException.", false);
				}
				assertEquals(1, bytes.size());
				assertEquals((byte)(i * 2), (byte)bytes.get(0));
				
				assertTrue(Arrays.equals(stringArrayValue, b.getValueStringArray()));
				assertTrue(Arrays.equals(numberArrayValue, b.getValueNumberArray()));
				assertTrue(Arrays.equals(bigDecimalArrayValue, b.getValueBigDecimalArray()));
				assertTrue(Arrays.equals(intTestArrayValue, b.getValueIntArray()));
				assertTrue(Arrays.equals(longTestArrayValue, b.getValueLongArray()));
				assertTrue(Arrays.equals(doubleTestArrayValue, b.getValueDoubleArray()));
				assertTrue(Arrays.equals(bigIntegerTestArrayValue, b.getValueBigIntegerArray()));
				assertEquals(sqlTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), b.getValueLocalDate());
				assertEquals(sqlTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalTime(), b.getValueLocalTime());
				assertEquals(sqlTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), b.getValueLocalDateTime());
				assertEquals(TBean.TBeanEnum.AAA, b.getValueEnum());
			}
		} catch (SQLException e) {
			log.error("Exception:", e);
			assertTrue("No exception should be thrown.",false);
		}
		log.info("testReflectionRowMapper - stop");		
	}
	

	@Test
	public void testReflectionRowMapperMissingSetMethod() throws SQLException {
		log.info("testReflectionRowMapperMissingSetMethod - start");	
		String[] cols = new String[]{
				VALUE_INT_COL,
				VALUE_LONG_COL,
				VALUE_DOUBLE_COL,
				VALUE_STRING_COL,
				VALUE_DATE_COL,
				VALUE_BOOLEAN_COL,
				VALUE_BIG_DECIMAL_COL,
				VALUE_BIG_INTEGER_COL,
				VALUE_BYTES_COL,
				VALUE_INPUT_STREAM_COL,
				"NoSetterForColumn"
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int i = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(i)).andReturn(col).anyTimes();
			i++;
		}
		
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(new Date().getTime());
		int cc = 1;
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();
		expect(rs.next()).andReturn(true);
		expect(rs.getRow()).andReturn(1);
		expect(rs.getInt(VALUE_INT_COL)).andReturn(i); cc++;
		expect(rs.wasNull()).andReturn(false);
		expect(rs.getLong(VALUE_LONG_COL)).andReturn((long)i); cc++;
		expect(rs.wasNull()).andReturn(false);
		expect(rs.getDouble(VALUE_DOUBLE_COL)).andReturn(i + .5); cc++;
		expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject(VALUE_STRING_COL)).andReturn(STRING_PREFIX + i); cc++;
		expect(rs.getTimestamp(VALUE_DATE_COL)).andReturn(sqlTimestamp); cc++;
		expect(rs.getObject(VALUE_BOOLEAN_COL)).andReturn(new StringBuilder().append("Y").append((char)0).append((char)0).append((char)0).toString()); cc++;
		expect(rs.getBigDecimal(VALUE_BIG_DECIMAL_COL)).andReturn(new BigDecimal(i + .5)); cc++;
		expect(rs.getBigDecimal(VALUE_BIG_INTEGER_COL)).andReturn(new BigDecimal(i)); cc++;
		expect(rs.findColumn(VALUE_BYTES_COL)).andReturn(cc);
		expect(rs.getBytes(cc++)).andReturn(new byte[]{(byte)i});
		expect(rs.findColumn(VALUE_INPUT_STREAM_COL)).andReturn(cc);
		expect(rs.getBinaryStream(cc++)).andReturn(new ByteArrayInputStream( new byte[]{(byte)(i * 2)}));
		rs.close();
		
		
		try {
			
			replay(rs, rsmd);

			ResultSetUtils.asArray(rs, TBean.class);
			assertTrue("Should not get to this point.", false);			
		} catch (SQLException e) {
			log.error("Expected exception:", e);
		}
		verify(rs, rsmd);
		log.info("testReflectionRowMapperMissingSetMethod - stop");			
	}

	
	@Test
	public void testReflectionRowMapperEmptyResultSet() {
		log.info("testReflectionRowMapperEmptyResultSet - start");
		try {
			expect(rs.next()).andReturn(false);
			if (log.isTraceEnabled()) {
				expect(rs.getFetchSize()).andReturn(10);
			}
			rs.close();
			expectLastCall();
			replay(rs);
			
			ResultSetUtils.asArray(rs, TBean.class);
			verify(rs);
		} catch (SQLException e) {
			assertTrue("No exception should be thrown.",false);
		}		
		log.info("testReflectionRowMapperEmptyResultSet - stop");
	}
	
	@Test
	public void testReflectionRowMapperWithConversionFailure() throws SQLException {
		log.info("testReflectionRowMapperWithConversionFailure - start");
		expect(rsmd.getColumnCount()).andReturn(1).anyTimes();
		expect(rsmd.getColumnName(1)).andReturn("intValue");
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();
		Exception expectedException = new SQLException("Test SqlException");
		expect(rs.getInt("intValue")).andThrow(expectedException);
		
		ReflectionRowMapper<TIntFieldBean> mapper = new ReflectionRowMapper<TIntFieldBean>(TIntFieldBean.class);
		
		replay(rs, rsmd);
		try {
			mapper.mapRow(rs, 1);
			fail("Expected SQLException not thrown.");
		} catch(SQLException e) {
			log.debug("Expected exception: " + e.getMessage());
			assertSame(expectedException, e.getCause());
		}
		verify(rs, rsmd);
		log.info("testReflectionRowMapperWithConversionFailure - stop");
	}
	
	@Test
	public void testReflectionRowMapperWithInvalidSetMethod() throws SQLException {
		log.info("testReflectionRowMapperWithInvalidSetMethod - start");
		expect(rsmd.getColumnCount()).andReturn(1).anyTimes();
		expect(rsmd.getColumnName(1)).andReturn("intValue").anyTimes();
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();		
		
		ReflectionRowMapper<TInvalidSetterBean> mapper = new ReflectionRowMapper<TInvalidSetterBean>(TInvalidSetterBean.class);
		
		replay(rs, rsmd);
		try {
			mapper.mapRow(rs, 1);
			fail("Expected SQLException not thrown.");
		} catch(SQLException e) {
			log.debug("Expected exception: " + e.getMessage());
		}
		verify(rs, rsmd);
		log.info("testReflectionRowMapperWithInvalidSetMethod - stop");
	}
	
	@Test
	public void testReflectionRowMapperWithFailingSetMethod() throws SQLException {
		log.info("testReflectionRowMapperWithInvalidSetMethod - start");
		expect(rsmd.getColumnCount()).andReturn(1).anyTimes();
		expect(rsmd.getColumnName(1)).andReturn("intValue").anyTimes();
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();		
		expect(rs.getInt("intValue")).andReturn(10);
		expect(rs.wasNull()).andReturn(false);
		
		ReflectionRowMapper<TFaillingSetterBean> mapper = new ReflectionRowMapper<TFaillingSetterBean>(TFaillingSetterBean.class);
		replay(rs, rsmd);
		try {
			mapper.mapRow(rs, 1);
			fail("Expected SQLException not thrown.");
		} catch(SQLException e) {
			log.debug("Expected exception: " + e.getMessage());
			assertTrue("Wrong cause exception type.", e.getCause().getCause() instanceof UnsupportedOperationException);
		}
		verify(rs, rsmd);
		log.info("testReflectionRowMapperWithInvalidSetMethod - stop");
	}
	
	/**
	 * Tests the {@link ResultSetUtils#processResultSet(ResultSet, RowCallbackHandler)}
	 */
	@Test
	public void testProcessResultSet() throws SQLException {
		log.info("testProcessResultSet - start");
		ResultSet rs = createStrictMock(ResultSet.class);
		int SIZE = 10;
		for (int i=1; i<=SIZE; i++) {
			expect(rs.next()).andReturn(true);
		}
		expect(rs.next()).andReturn(false);
		if (log.isTraceEnabled()) {
			expect(rs.getFetchSize()).andReturn(10);
		}
		rs.close();		
		
		replay(rs);
		final List<Integer> list = new ArrayList<Integer>();
		ResultSetUtils.processResultSet(rs, new RowCallbackHandler() {
			int count = 0;
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				list.clear();
				list.add(++count);
			}
		});
		verify(rs);
		
		assertEquals(SIZE, list.get(0).intValue());
		log.info("testProcessResultSet - stop");
	}
	
	
	@Test
	public void testReflectionRowMapperMissingSetMethodWithBestEffort() throws SQLException {
		log.info("testBestEffortReflectionRowMapperMissingSetMethod - start");	
		String[] cols = new String[]{
				VALUE_INT_COL,
				VALUE_LONG_COL,
				VALUE_DOUBLE_COL,
				VALUE_STRING_COL,
				VALUE_DATE_COL,
				VALUE_BOOLEAN_COL,
				VALUE_BIG_DECIMAL_COL,
				VALUE_BIG_INTEGER_COL,
				VALUE_BYTES_COL,
				VALUE_INPUT_STREAM_COL,
				"NoSetterForColumn"
		};
		expect(rsmd.getColumnCount()).andReturn(cols.length).anyTimes();
		int ii = 1;
		for (String col: cols) {
			expect(rsmd.getColumnName(ii)).andReturn(col).anyTimes();
			ii++;
		}
		
		Statement st = createMock(Statement.class);
		expect(st.getConnection()).andReturn(null).anyTimes();
		
		final int SIZE = 1;
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(new Date().getTime());
		expect(rs.getMetaData()).andReturn(rsmd).anyTimes();
		for (int i=1; i<=SIZE; i++) {
			int ci = 1;
			expect(rs.next()).andReturn(true);
			expect(rs.getRow()).andReturn(i);
			expect(rs.getInt(VALUE_INT_COL)).andReturn(i); ci++;
			expect(rs.wasNull()).andReturn(false);
			expect(rs.getLong(VALUE_LONG_COL)).andReturn((long)i); ci++;
			expect(rs.wasNull()).andReturn(false);
			expect(rs.getDouble(VALUE_DOUBLE_COL)).andReturn(i + .5); ci++;
			expect(rs.wasNull()).andReturn(false);
			expect(rs.getObject(VALUE_STRING_COL)).andReturn(STRING_PREFIX + i); ci++;
			expect(rs.getTimestamp(VALUE_DATE_COL)).andReturn(sqlTimestamp); ci++;
			expect(rs.getObject(VALUE_BOOLEAN_COL)).andReturn(new StringBuilder().append("Y").append((char)0).append((char)0).append((char)0).toString()); ci++;
			expect(rs.getBigDecimal(VALUE_BIG_DECIMAL_COL)).andReturn(new BigDecimal(i + .5)); ci++;
			expect(rs.getBigDecimal(VALUE_BIG_INTEGER_COL)).andReturn(new BigDecimal(i)); ci++;
			expect(rs.findColumn(VALUE_BYTES_COL)).andReturn(ci);
			expect(rs.getBytes(ci)).andReturn(new byte[]{(byte)i}); ci++;
			expect(rs.findColumn(VALUE_INPUT_STREAM_COL)).andReturn(ci);
			expect(rs.getBinaryStream(ci)).andReturn(new ByteArrayInputStream(new byte[]{(byte)(i * 2)})); ci++;
		}
		expect(rs.next()).andReturn(false);
		if (log.isTraceEnabled()) {
			expect(rs.getFetchSize()).andReturn(10);
		}
		rs.close();
		
		replay(rs, rsmd, st);
		try {
			ResultSetUtils.asList(rs, new ReflectionRowMapper<TBean>(TBean.class, true));
		} catch (SQLException e) {
			fail("Should not throw exception.");
		}
		verify(rs, rsmd, st);
		log.info("testBestEffortReflectionRowMapperMissingSetMethod - stop");			
	}
	
}
