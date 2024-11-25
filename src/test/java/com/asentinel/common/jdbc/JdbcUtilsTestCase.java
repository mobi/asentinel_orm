package com.asentinel.common.jdbc;

import static org.junit.Assert.*;

import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.jdbc.core.SqlParameterValue;

import com.asentinel.common.util.Utils;

public class JdbcUtilsTestCase {
	
	private static final Logger log = LoggerFactory.getLogger(JdbcUtilsTestCase.class);
	
	@Test
	public void testPrepareStringForLogging1() {
		assertTrue("The LOG_MAX_SIZE must be greater than LOG_MAX_SHOW_IF_TOO_LONG.", JdbcUtils.LOG_MAX_SIZE > JdbcUtils.LOG_MAX_SHOW_IF_TOO_LONG);
		String testStr = "";
		for (int i=0; i < JdbcUtils.LOG_MAX_SIZE - 1 ; i++) {
			testStr += "X";
		}
		String result = JdbcUtils.prepareStringForLogging(testStr);
		assertEquals("'" + testStr + "'", result);
	}
	
	@Test
	public void testPrepareStringForLogging2() {
		String testStr = "";
		for (int i=0; i < JdbcUtils.LOG_MAX_SIZE; i++) {
			testStr += "X";
		}
		String result = JdbcUtils.prepareStringForLogging(testStr);
		assertEquals("'" + testStr + "'", result);
	}

	@Test
	public void testPrepareStringForLogging3() {
		String testStr = "";
		for (int i=0; i < JdbcUtils.LOG_MAX_SIZE + 1; i++) {
			testStr += "X";
		}
		String result = JdbcUtils.prepareStringForLogging(testStr);
		assertEquals("String[length=" + (JdbcUtils.LOG_MAX_SIZE + 1) + ", 'XXXXXXXXXX ...']", result);
	}

	@Test
	public void testPrepareStringForLogging4() {
		String testStr = null;
		String result = JdbcUtils.prepareStringForLogging(testStr);
		assertEquals("null", result);
	}
	
	@Test
	public void testPrepareStringForLogging5() {
		String testStr = "";
		String result = JdbcUtils.prepareStringForLogging(testStr);
		assertEquals("''", result);
	}
	
	@Test
	public void testParametersToString() {
		Date d = new Date();
		Object[] params = new Object[] {
			"abc",
			10,
			true,
			BooleanParameterConverter.NULL_BOOLEAN,
			new SqlParameterValue(Types.DATE, d),
			new SqlParameterValue(Types.VARCHAR, "abc2"),
			null
		};
		String expected = String.format(
				"['abc', 10, true, null, SqlParamVal [type=91, value=%s], SqlParamVal [type=12, value='abc2'], null]", 
				String.valueOf(d));
		String result = JdbcUtils.parametersToString(true, params);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals(expected, result);
	}
	
	@Test
	public void testParametersToStringWithLongStrings() {
		StringBuilder sb = new StringBuilder()
				.append("a".repeat(210));
		Object[] params = new Object[] {
			sb.toString(),
			new SqlParameterValue(Types.VARCHAR, sb.toString()),
		};
		String expected = 
				"[String[length=210, 'aaaaaaaaaa ...'], SqlParamVal [type=12, value=String[length=210, 'aaaaaaaaaa ...']]]";
		String result = JdbcUtils.parametersToString(true, params);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals(expected, result);
	}
	

	@Test
	public void testParametersToStringWithNull() {
		String result = JdbcUtils.parametersToString(true, (Object[]) null);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals("null", result);
	}

	@Test
	public void testParametersToStringWithEmpty() {
		String result = JdbcUtils.parametersToString(true);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals("[]", result);
	}
	
	@Test
	public void testParametersToStringWithEmptyNoBrackets() {
		String result = JdbcUtils.parametersToString(false);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals("", result);
	}
	
	@Test
	public void testParametersToStringWithNullSeparator() {
		String result = JdbcUtils.parametersToString(null, true, 1, 2);
		log.debug("testParametersToString - string: {}", result) ;
		assertEquals("[1, 2]", result);
	}
	
	@Test
	public void testParametersToStringWithTemporal() {
		LocalDate d = LocalDate.now();
		String result = JdbcUtils.parametersToString(false, d);
		log.debug("testParametersToStringWithTemporal - string: {}", result) ;
		assertEquals(String.valueOf(Utils.toDate(d)), result);
	}
	
	@Test
	public void testQueryProcedureCall() {
		assertEquals("SELECT * FROM Schema.Function()", 
				JdbcUtils.queryProcedureCall("Schema.Function"));
		assertEquals("SELECT * FROM Schema.Function()", 
				JdbcUtils.queryProcedureCall("Schema.Function", 0));
		
		assertEquals("SELECT * FROM Schema.Function(?)", 
				JdbcUtils.queryProcedureCall("Schema.Function", 1));
		assertEquals("SELECT * FROM Schema.Function(?, ?, ?, ?, ?)", 
				JdbcUtils.queryProcedureCall("Schema.Function", 5));
	}
}
