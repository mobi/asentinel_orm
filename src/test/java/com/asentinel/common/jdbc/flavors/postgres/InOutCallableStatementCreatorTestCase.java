package com.asentinel.common.jdbc.flavors.postgres;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.Capture;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.lob.LobCreator;

import com.asentinel.common.jdbc.BooleanParameterConverter;
import com.asentinel.common.jdbc.SimpleUser;
import com.asentinel.common.jdbc.ThreadLocalUser;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;

// FIXME: this test is very similar to the Oracle CallableStatement test
// Should try to isolate the common parts in a base class

public class InOutCallableStatementCreatorTestCase {
	private final static Logger log = LoggerFactory.getLogger(InOutCallableStatementCreatorTestCase.class);
	
	JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();

	private static class TestByteArrayInputStream extends ByteArrayInputStream {
		
		private int closeCount = 0;

		public TestByteArrayInputStream(byte[] buf) {
			super(buf);
		}

		@Override
	    public void close() throws IOException {
			closeCount++;
	    }

		public int getCloseCount() {
			return closeCount;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(buf);
		}
	}
	
	private int getExpectedQmCount(Object[] inParams) {
		if (inParams == null) {
			return 0;
		}
		int expectedQmCount = 0;
		for (@SuppressWarnings("unused") Object p:inParams) {
			expectedQmCount++;
		}
		return expectedQmCount;
	}
	
	private boolean hasBlob(Object[] inParams) {
		if (inParams == null) {
			return false;
		}
		for (Object p:inParams) {
			if (p instanceof byte[] 
					|| p instanceof InputStream) {
				return true;
			}
		}
		return false;
	}
	
	private String buildSqlCall(String spName, Object[] inParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("? = call ").append(spName).append("(");
		if (inParams != null) {
			for (@SuppressWarnings("unused") Object p:inParams) {
				sb.append("?");
				sb.append(InOutCallableStatementCreator.SEPARATOR);
			}
		}
		String sql;
		if (sb.toString().endsWith(InOutCallableStatementCreator.SEPARATOR)) {
			sql = sb.substring(0, sb.length() - InOutCallableStatementCreator.SEPARATOR.length()) + ")";
		} else {
			sql = sb + ")";
		}
		return sql;
	}
	
	/**
	 * Method that allows for the creation and validation of various calls.
	 * @param spName
	 * @param outParamCount
	 * @param inParams
	 * @throws SQLException
	 */
	private void testCall(String spName, Object ... inParams) throws SQLException {
		int outParamCount = 1;
		Connection conn = createMock(Connection.class);
		
		ParameterMetaData paramMeta = createMock(ParameterMetaData.class);
		expect(paramMeta.getParameterType(anyInt())).andReturn(Types.NULL).anyTimes();
		
		CallableStatement cs = createMock(CallableStatement.class);
		expect(cs.getConnection()).andReturn(conn).anyTimes();
		expect(cs.getParameterMetaData()).andReturn(paramMeta).anyTimes();
		
		InOutCallableStatementCreator creator = new InOutCallableStatementCreator(jdbcFlavor, spName, inParams);
		assertTrue("MUST implement ParameterDisposer.", creator instanceof ParameterDisposer);
		
		LobCreator lobCreator = createMock(LobCreator.class);
		if (hasBlob(inParams)) {
			creator.setLobCreator(lobCreator);
		}

		Capture<String> capturedSqlCallString = Capture.newInstance();
		
		expect(conn.prepareCall(capture(capturedSqlCallString))).andReturn(cs);
		
		int paramIndex = 1;
		cs.registerOutParameter(paramIndex++, new PostgresJdbcFlavor().getCursorType());
		if (inParams != null) {
			for (Object o:inParams) {
				if (o == null) {
					cs.setNull(paramIndex++, Types.NULL);
				} else if (o instanceof Date) {
					cs.setTimestamp(paramIndex++, new java.sql.Timestamp(((java.util.Date) o).getTime()));
				} else if (o instanceof Calendar) {
					Calendar c = (Calendar) o;
					cs.setTimestamp(paramIndex++, new java.sql.Timestamp(c.getTime().getTime()), c);
				} else if (o instanceof String) {
					cs.setString(paramIndex++, (String)o);
				} else if (o instanceof byte[]) {	
					lobCreator.setBlobAsBytes(cs, paramIndex++, (byte[]) o);
				} else if (o instanceof InputStream) {
					lobCreator.setBlobAsBinaryStream(cs, paramIndex++, (InputStream) o, -1);
				} else if (o instanceof SqlParameterValue) {
					SqlParameterValue pv = (SqlParameterValue) o;
					if (pv.getSqlType() == Types.INTEGER) {
						cs.setObject(paramIndex++, pv.getValue(), pv.getSqlType());
					} else if (pv.getSqlType() == Types.DATE) {
						cs.setDate(paramIndex, new java.sql.Date(((Date)pv.getValue()).getTime()));
					} else {
						throw new RuntimeException("This test method does not support this sql type, please add support.");
					}
				} else {
					cs.setObject(paramIndex++, o);
				}
			}
		}
		
		
		if (hasBlob(inParams)) {
			lobCreator.close();
		}
		
		replay(conn, paramMeta, cs, lobCreator);
		
		// simulate statement creator life cycle
		creator.createCallableStatement(conn);
		creator.cleanupParameters();
		
		verify(conn, paramMeta, cs, lobCreator);

		// validate values
		String sqlCallString = capturedSqlCallString.getValue();
		assertTrue(sqlCallString.indexOf(spName)>=0);
		
		// count question marks
		int qmCount = 0;
		for (int i=0; i<sqlCallString.length(); i++) {
			if (sqlCallString.charAt(i) == '?') {
				qmCount++;
			}
		}
		assertEquals(getExpectedQmCount(inParams) + outParamCount, qmCount);
		
		// validate sql call
		assertTrue("Sql string should start with '{ ? = call '.", sqlCallString.startsWith("{ ? = call"));
		assertTrue("Sql string should end with '}'.", sqlCallString.endsWith("}"));
		assertTrue("Invalid sql call string created.",
				sqlCallString.indexOf(buildSqlCall(spName, inParams)) > 0);
		
		// validate closing count for TestByteArrayInputStreams
		if (inParams != null) {
			for (Object o:inParams) {
				if (o instanceof TestByteArrayInputStream) {
					// close is called 2 times for InputStreams
					// 1) the lobCreator calls close (not the mock but the actual LobCreator in a live situation)
					// 2) cleanupParameters calls close on any Closeables
					assertTrue("Close was not called for an InputStream parameter",
							((TestByteArrayInputStream) o).getCloseCount() > 0);
				}
			}
		}
	}
	
	
	@Test
	public void testCallableStatementCreation() throws SQLException {
		log.info("testCallableStatementCreation - start");
		
		testCall("test");
		testCall("test");
		testCall("test");
		
		testCall("test", (Object[])null);
		
		testCall("test", (Object)null);
		
		testCall("test", null, 2);
		
		testCall("test", true);
		testCall("test", true, false, true);
		
		testCall("test", 1, 2, 3);
		
		testCall("test",  1, 2, 3, new Date(), "aaaa");
		
		testCall("test", 1, 2, 3, new Date(), "aaaa");
		
		testCall("test",  				
				1, 10l, 1.5d, 1.5f,
				new Date(), Calendar.getInstance(),
				"test",
				true, false, null
				);
		
		testCall("test", new byte[]{1});		
		testCall("test", "aaaa", new byte[]{1}, 2, 3.5d);
		
		testCall("test", new TestByteArrayInputStream(new byte[]{1}));
		testCall("test", "aaaa", new TestByteArrayInputStream(new byte[]{1}), 2l, 3.5d);
		
		testCall("test", new TestByteArrayInputStream(new byte[]{1}), new byte[]{2});
		testCall("test", new TestByteArrayInputStream(new byte[]{1}), new TestByteArrayInputStream(new byte[]{2}));
		
		testCall("test", new SqlParameterValue(Types.INTEGER, 2));
		
		testCall("test", new SqlParameterValue(Types.DATE, new Date()));
		
		testCall("test", BigInteger.ONE, BigDecimal.TEN, new BigDecimal("1.5"));
		
		testCall("test", BooleanParameterConverter.NULL_BOOLEAN);
		testCall("test", "test", BooleanParameterConverter.NULL_BOOLEAN, 2);

		log.info("testCallableStatementCreation - stop");
	}
	
	@Test
	public void testUserLogging() throws SQLException {
		log.info("testUserLogging - start");
		
		// test without thread local user
		testCall("test", 1, 2);
		
		// test with null thread local user
		ThreadLocalUser.getThreadLocalUser().set(null);
		testCall("test", 1, 2);
		ThreadLocalUser.getThreadLocalUser().remove();
		
		// test with null username and user id in the thread local user
		ThreadLocalUser.getThreadLocalUser().set(new SimpleUser() {
			@Override
			public String getUsername() {
				return null;
			}
			
			@Override
			public Object getUserId() {
				return null;
			}
		});
		
		testCall("test", 1, 2);
		ThreadLocalUser.getThreadLocalUser().remove();
		
		
		// test with thread local user
		ThreadLocalUser.getThreadLocalUser().set(new SimpleUser() {
			@Override
			public String getUsername() {
				return "Username";
			}
			
			@Override
			public Object getUserId() {
				return "UserId";
			}
		});
		
		testCall("test", 1, 2);
		ThreadLocalUser.getThreadLocalUser().remove();
		
		log.info("testUserLogging - stop");
	}
	
	/**
	 * Test that ensures that the InOutCallableStatementCreator works correctly
	 * in combination with the JdbcTemplate when it comes to closing the LobCreator
	 * that it is using for blobs.
	 */
	@Test
	public void testInCombinationWithJdbcTemplate() throws SQLException {
		log.info("testInCombinationWithJdbcTemplate - start");
		
		final List<Integer> testList = new ArrayList<>();
		InputStream in = new ByteArrayInputStream(new byte[]{3, 4}) {
			int i = 1;
			
			@Override
		    public void close() {
		    	testList.add(i++);
		    }
		};
		
		DataSource dataSource = createStrictMock(DataSource.class);
		Connection conn = createStrictMock(Connection.class);
		CallableStatement cs = createStrictMock(CallableStatement.class);
		LobCreator lobCreator = createStrictMock(LobCreator.class);

		cs.registerOutParameter(1, new PostgresJdbcFlavor().getCursorType());
		cs.close();

		expect(dataSource.getConnection()).andReturn(conn);
		expect(conn.prepareCall(anyObject(String.class))).andReturn(cs);
		conn.close();

		// ensure that the lobCreator is closed and implicitly the  InOutCallableStatementCreator#cleanupParameters
		// is called by the JdbcTemplate#execute method
		lobCreator.setBlobAsBytes(anyObject(CallableStatement.class), anyInt(), anyObject(byte[].class));
		lobCreator.setBlobAsBinaryStream(anyObject(CallableStatement.class), anyInt(), anyObject(InputStream.class), anyInt());
		lobCreator.close();
		
		JdbcOperations jdbcOps = new JdbcTemplate(dataSource);
		
		InOutCallableStatementCreator creator = new InOutCallableStatementCreator(jdbcFlavor, "test", new byte[]{0, 1}, in);
		creator.setLobCreator(lobCreator);
		CallableStatementCallback<Object> action = new CallableStatementCallback<>() {
			@Override
			public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				log.debug("doInCallableStatement - made it to the action.");
				return null;
			}
		};
		
		replay(dataSource, conn, cs, lobCreator);
		
		jdbcOps.execute(creator, action);
		
		verify(dataSource, conn, cs, lobCreator);

		assertTrue("The input stream was not closed.", testList.size() > 0 );
		log.debug("testInCombinationWithJdbcTemplate - close was called on InputStream " + testList.size() + " times. That's to be expected.");

		log.info("testInCombinationWithJdbcTemplate - stop");
	}
	

	/**
	 * Test that ensures that the InOutCallableStatementCreator works correctly
	 * in combination with the JdbcTemplate when it comes to closing the LobCreator
	 * that it is using for blobs. Unlike the previous test in this one the JdbcTemplate
	 * throws an exception. In this situation the stream still has to be closed.
	 */
	@Test
	public void testInCombinationWithJdbcTemplateAndException() throws SQLException {
		log.info("testInCombinationWithJdbcTemplateAndException - start");
		
		final List<Integer> testList = new ArrayList<>();
		InputStream in = new ByteArrayInputStream(new byte[]{3, 4}) {
			int i = 1;
			
			@Override
		    public void close() {
		    	testList.add(i++);
		    }
		};
		
		DataSource dataSource = createStrictMock(DataSource.class);
		Connection conn = createNiceMock(Connection.class);
		DatabaseMetaData dbMeta = createStrictMock(DatabaseMetaData.class);
		CallableStatement cs = createStrictMock(CallableStatement.class);
		LobCreator lobCreator = createStrictMock(LobCreator.class);


		expect(dataSource.getConnection()).andReturn(conn).anyTimes();
		expect(conn.prepareCall(anyObject(String.class))).andThrow(new SQLException("Test SQLException"));
		expect(conn.getMetaData()).andReturn(dbMeta);
		conn.close();

		// ensure that the lobCreator is closed and implicitly the  InOutCallableStatementCreator#cleanupParameters
		// is called by the JdbcTemplate#execute method
		lobCreator.close();
		
		JdbcOperations jdbcOps = new JdbcTemplate(dataSource);
		
		InOutCallableStatementCreator creator = new InOutCallableStatementCreator(jdbcFlavor, "test", in);
		creator.setLobCreator(lobCreator);
		CallableStatementCallback<Object> action = new CallableStatementCallback<>() {
			@Override
			public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
				log.debug("doInCallableStatement - made it to the action.");
				return null;
			}
		};
		
		replay(dataSource, conn, cs, lobCreator);
		
		try {
			jdbcOps.execute(creator, action);
			fail("execute should throw exception.");
		} catch (DataAccessException e) {
			log.debug("testInCombinationWithJdbcTemplateAndException - Expected exception: " + e.getMessage());
		}
		
		verify(dataSource, conn, cs, lobCreator);

		assertTrue("The input stream was not closed.", testList.size() > 0 );
		log.debug("testInCombinationWithJdbcTemplateAndException - close was called on InputStream " + testList.size() + " times. That's to be expected.");

		log.info("testInCombinationWithJdbcTemplateAndException - stop");
	}
	

}
