package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 * Test the ResultSetUtils#asXXX methods in combination with the single column
 * mappers.
 */
public class ResultSetUtils2TestCase {
	private static final Logger log = LoggerFactory.getLogger(ResultSetUtils2TestCase.class);

	private static final int SIZE = 10;
	
	private interface ExpectationCallback {
		
			void expectCall(ResultSet rs, int row) throws SQLException;
			
			void validate(Object[] objects);
	}
	
	private void genericRowMapperTest(Class<?> clasz, ExpectationCallback expectation) throws SQLException {
		ResultSet rs = createNiceMock(ResultSet.class);
		
		for (int i=1; i<=SIZE; i++) {
			expect(rs.next()).andReturn(true);
			expect(rs.getRow()).andReturn(i);
			expectation.expectCall(rs, i);
		}
		if (log.isTraceEnabled()) {
			expect(rs.getFetchSize()).andReturn(10);
		}
		expect(rs.next()).andReturn(false);
		rs.close();
		expectLastCall();
		replay(rs);
		
		Object[] array = ResultSetUtils.asArray(rs, clasz);
		verify(rs);
		expectation.validate(array);
	}

	@Test
	public void testSingleColumnResultSet() throws SQLException {
		log.info("testSingleColumnResultSet - start");
		
		genericRowMapperTest(String.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getObject(1)).andReturn("string" + row);
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals("string" + (i + 1), objects[i]);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for String.");

		genericRowMapperTest(Integer.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getInt(1)).andReturn(row);
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals(i + 1, ((Integer)objects[i]).intValue());
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for int.");

		genericRowMapperTest(Long.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getLong(1)).andReturn((long)row);
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals(i + 1, ((Long)objects[i]).longValue());
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for long.");
		
		
		genericRowMapperTest(Double.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getDouble(1)).andReturn((double)row);
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals((double)i + 1, (Double) objects[i], 0.01);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for double.");

		genericRowMapperTest(Boolean.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getObject(1)).andReturn("y");
			}

			@Override
			public void validate(Object[] objects) {
                for (Object object : objects) {
                    assertEquals(true, object);
                }
			}
		});
		log.info("testSingleColumnResultSet - passed for boolean.");
		

		final Date d = new Date();
		final Timestamp ts = new Timestamp(d.getTime());
		
		genericRowMapperTest(Date.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getTimestamp(1)).andReturn(ts);
			}

			@Override
			public void validate(Object[] objects) {
                for (Object object : objects) {
                    assertEquals(d, object);
                }
			}
		});
		log.info("testSingleColumnResultSet - passed for date.");

		genericRowMapperTest(BigInteger.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getBigDecimal(1)).andReturn(new BigDecimal(row));
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals(new BigInteger(""+ (i + 1)), objects[i]);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for BigInteger.");
		
		
		genericRowMapperTest(BigDecimal.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getBigDecimal(1)).andReturn(new BigDecimal(row));
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals(new BigDecimal(""+ (i + 1)), objects[i]);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for BigDecimal.");
		

		final Statement st = createMock(Statement.class);
		expect(st.getConnection()).andReturn(null).anyTimes();
		replay(st);
		genericRowMapperTest(byte[].class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getBytes(1)).andReturn(new byte[]{(byte)row});
			}

			@Override
			public void validate(Object[] objects) {
				for (int i=0; i<objects.length; i++) {
					assertEquals(i + 1, ((byte[])objects[i])[0]);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for byte[].");

		final Statement st2 = createMock(Statement.class);
		expect(st2.getConnection()).andReturn(null).anyTimes();
		replay(st2);
		genericRowMapperTest(InputStream.class, new ExpectationCallback(){

			@Override
			public void expectCall(ResultSet rs, int row) throws SQLException {
				expect(rs.getBinaryStream(1)).andReturn(new ByteArrayInputStream(new byte[]{(byte)row}));
			}

			@Override
			public void validate(Object[] objects) {
				try {
					for (int i=0; i<objects.length; i++) {
						assertEquals(i + 1, ((InputStream)objects[i]).read());
					}
				} catch (IOException e) {
					throw new RuntimeException("Failed to read blob.", e);
				}
			}
		});
		log.info("testSingleColumnResultSet - passed for InputStream.");
		
		
		log.info("testSingleColumnResultSet - stop");
	}

}
