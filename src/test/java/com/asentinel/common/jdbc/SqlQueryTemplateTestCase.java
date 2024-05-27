package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.Capture;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.util.Utils;

/**
 * Tests the {@link SqlQueryTemplate} methods without the conversion
 * of ResultSet rows to objects.
 */
public class SqlQueryTemplateTestCase {
	private final static Logger log = LoggerFactory.getLogger(SqlQueryTemplateTestCase.class);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testUserLogging() throws SQLException {
		log.info("testUserLogging - start");
		
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		expect(jdbcOperations.query((String) anyObject(), (RowMapper) anyObject(), new Object[0]))
			.andReturn(Collections.emptyList()).anyTimes();
		expect(jdbcOperations.update(anyObject(PreparedStatementCreator.class)))
			.andReturn(0).anyTimes();
		
		SqlQueryTemplate sqlQuery = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		
		replay(jdbcOperations);
		
		// test without thread local user
		sqlQuery.query("dummy sql", Integer.class);
		sqlQuery.update("dummy sql");
		
		// test with null thread local user
		ThreadLocalUser.getThreadLocalUser().set(null);
		sqlQuery.query("dummy sql", Integer.class);
		sqlQuery.update("dummy sql");
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
		
		sqlQuery.query("dummy sql", Integer.class);
		sqlQuery.update("dummy sql");
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
		
		sqlQuery.query("dummy sql", Integer.class);
		sqlQuery.update("dummy sql");
		ThreadLocalUser.getThreadLocalUser().remove();
		
		verify(jdbcOperations);
		
		log.info("testUserLogging - stop");

	}
	
	/**
	 * Tests only queryForObject method with class parameters. The class parameter method in
	 * {@link SqlQueryTemplate} delegate to RowMappers method, so there is no
	 * need to write a separate test for the mapper method.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testQueryForObjectWithClass() {
		log.info("testQueryForObjectWithClass - start");
		
		// test correct operation
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		List<Integer> rs = new ArrayList<Integer>();
		final Integer value = 1;
		rs.add(value);
		expect(jdbcOperations.query((String)anyObject(), (RowMapper<Integer>)anyObject(), new Object[0])).andReturn(rs);
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);		
		Integer object = query.queryForObject("dummy sql", Integer.class);
		verify(jdbcOperations);
		assertEquals(value, object);
		
		// test empty result set
		reset(jdbcOperations);
		rs.clear();
		expect(jdbcOperations.query((String)anyObject(), (RowMapper<Integer>)anyObject(), new Object[0])).andReturn(rs);
		
		replay(jdbcOperations);
		query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		try { 
			object = query.queryForObject("dummy sql", Integer.class);
			fail("Should throw exception.");
		} catch (EmptyResultDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);
		
		// test too many rows resultset
		reset(jdbcOperations);
		rs.add(1);
		rs.add(2);
		expect(jdbcOperations.query((String)anyObject(), (RowMapper<Integer>)anyObject(), new Object[0])).andReturn(rs);
		
		replay(jdbcOperations);
		query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		try { 
			object = query.queryForObject("dummy sql", Integer.class);
			fail("Should throw exception.");
		} catch (IncorrectResultSizeDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);
		
		log.info("testQueryForObjectWithClass - stop");
	}

	
	@Test
	public void testQueryWithBooleanParameter() {
		log.info("testQueryWithBooleanParameter - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<String> capturedSql = Capture.newInstance();
		Capture<Object> capturedInParam0 = Capture.newInstance();
		Capture<Object> capturedInParam1 = Capture.newInstance();
		expect(jdbcOperations.query(capture(capturedSql), (RowMapper<?>) anyObject(), capture(capturedInParam0), capture(capturedInParam1)))
			.andReturn(Collections.emptyList());
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.query(sql, Integer.class, true, false);
		log.debug("testQueryWithBooleanParameter - captured inParams: " + capturedInParam0 + ", " + capturedInParam1);
		assertEquals(sql, capturedSql.getValue());
		assertEquals("Y", capturedInParam0.getValue());
		assertEquals("N", capturedInParam1.getValue());
		verify(jdbcOperations);
		log.info("testQueryWithBooleanParameter - stop");
	}
	
	
	@Test
	public void testQueryWithTemporalParameters() {
		log.info("testQueryWithTemporalParameters - start");
		
		final String sql = "dummy sql";
		final LocalDate localDate = LocalDate.now();
		final LocalTime localTime = LocalTime.now();
		final LocalDateTime localDateTime = LocalDateTime.now();
		
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<String> capturedSql = Capture.newInstance();
		Capture<Object> localDateParam0 = Capture.newInstance();
		Capture<Object> localTimeParam1 = Capture.newInstance();
		Capture<Object> localDateTimeParam2 = Capture.newInstance();
		expect(jdbcOperations.query(capture(capturedSql), (RowMapper<?>) anyObject(), 
				capture(localDateParam0), capture(localTimeParam1), capture(localDateTimeParam2)))
			.andReturn(Collections.emptyList());
		
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		
		replay(jdbcOperations);
		query.query(sql, Integer.class, localDate, localTime, localDateTime);
		verify(jdbcOperations);
		
		log.debug("testQueryWithTemporalParameters - captured inParams: " + localDateParam0 + ", " + localTimeParam1 + ", " + localDateTimeParam2);
		assertEquals(sql, capturedSql.getValue());
		assertEquals(Utils.toDate(localDate), localDateParam0.getValue());
		assertEquals(Utils.toDate(localTime), localTimeParam1.getValue());
		assertEquals(Utils.toDate(localDateTime), localDateTimeParam2.getValue());
		
		log.info("testQueryWithTemporalParameters - stop");
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testQueryWithoutBooleanParameter() {
		log.info("testQueryWithoutBooleanParameter - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<String> capturedSql = Capture.newInstance();
		Capture<Object> capturedInParam0 = Capture.newInstance();
		Capture<Object> capturedInParam1 = Capture.newInstance();
		expect(jdbcOperations.query(capture(capturedSql), (RowMapper) anyObject(), 
				capture(capturedInParam0), capture(capturedInParam1)))
				.andReturn(Collections.emptyList());
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.query(sql, Integer.class, "test", 10);
		log.debug("testQueryWithoutBooleanParameter - captured inParams: " + capturedInParam0 + ", " + capturedInParam1);
		assertEquals(sql, capturedSql.getValue());
		assertEquals("test", capturedInParam0.getValue());
		assertEquals(Integer.valueOf(10), capturedInParam1.getValue());
		verify(jdbcOperations);
		log.info("testQueryWithoutBooleanParameter - stop");
	}
	

	@Test
	public void testUpdateWithoutBooleanOrBlobParameter() {
		log.info("testUpdateWithoutBooleanOrBlobParameter - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<CustomPreparedStatementCreator> capturedCreator = Capture.newInstance();
		expect(jdbcOperations.update(capture(capturedCreator))).andReturn(1);
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.update(sql, "test", 10);
		assertEquals(sql, capturedCreator.getValue().getSql());
		assertEquals("test", capturedCreator.getValue().getParams()[0]);
		assertEquals(Integer.valueOf(10), capturedCreator.getValue().getParams()[1]);
		verify(jdbcOperations);
		log.info("testUpdateWithoutBooleanOrBlobParameter - stop");
	}

	@Test
	public void testUpdateWithBooleanParameter() {
		log.info("testUpdateWithBooleanParameter - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<CustomPreparedStatementCreator> capturedCreator = Capture.newInstance();
		expect(jdbcOperations.update(capture(capturedCreator))).andReturn(1);
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.update(sql, true, false);
		assertEquals(sql, capturedCreator.getValue().getSql());
		assertEquals("Y", capturedCreator.getValue().getParams()[0]);
		assertEquals("N", capturedCreator.getValue().getParams()[1]);
		verify(jdbcOperations);
		log.info("testUpdateWithBooleanParameter - stop");
	}

	@Test
	public void testUpdateWithBlobParameter() throws IOException {
		log.info("testUpdateWithBlobParameter - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<CustomPreparedStatementCreator> capturedCreator = Capture.newInstance();
		expect(jdbcOperations.update(capture(capturedCreator))).andReturn(1);
		
		// we don't mock the blob because of the "Illegal reflective operation" warning
		InputStream blob = new ByteArrayInputStream(new byte[0]);
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.update(sql, blob);
		assertEquals(sql, capturedCreator.getValue().getSql());
		assertTrue(capturedCreator.getValue().getParams()[0] instanceof InputStream);
		verify(jdbcOperations);
		log.info("testUpdateWithBlobParameter - stop");
	}
	
	@Test
	public void testUpdateWithTemporalParameters() {
		log.info("testUpdateWithTemporalParameters - start");
		
		final String sql = "dummy sql";
		final LocalDate localDate = LocalDate.now();
		final LocalTime localTime = LocalTime.now();
		final LocalDateTime localDateTime = LocalDateTime.now();
		
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<CustomPreparedStatementCreator> capturedCreator = Capture.newInstance();
		expect(jdbcOperations.update(capture(capturedCreator))).andReturn(1);
		
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);

		replay(jdbcOperations);
		query.update(sql, localDate, localTime, localDateTime);
		verify(jdbcOperations);

		assertEquals(sql, capturedCreator.getValue().getSql());
		assertEquals(Utils.toDate(localDate), capturedCreator.getValue().getParams()[0]);
		assertEquals(Utils.toDate(localTime), capturedCreator.getValue().getParams()[1]);
		assertEquals(Utils.toDate(localDateTime), capturedCreator.getValue().getParams()[2]);
		log.info("testUpdateWithTemporalParameters - stop");
	}
	
	
	
	/**
	 * Tests the query method with a RowCallbackHandler. The test is 
	 * shallow because it is difficult to mock the ResultSet in this case.
	 */
	@Test
	public void testQueryWithRowCallbackHandler() {
		log.info("testQueryWithRowCallbackHandler - start");
		JdbcOperations jdbcOperations = createStrictMock(JdbcOperations.class);
		RowCallbackHandler handler = new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				log.debug("testQueryWithRowCallbackHandler - row: " + rs.getRow());
			}
		};
		jdbcOperations.query(anyObject(String.class), anyObject(Object[].class), anyObject(RowCallbackHandler.class));
		
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		
		replay(jdbcOperations);
		query.query("dummy sql", handler, 1);
		verify(jdbcOperations);
		
		log.info("testQueryWithRowCallbackHandler - stop");
	}

	// this test will ONLY work with PostgresJdbcFlavor
	@Test
	public void testQueryForDoubleWrappingInBigDecimal() {
		log.info("testQueryForDoubleWrappingInBigDecimal - start");
		final String sql = "dummy sql";
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<String> capturedSql = Capture.newInstance();
		expect(jdbcOperations.query(capture(capturedSql), (RowMapper<?>) anyObject(), 
				eq(BigDecimal.valueOf(1.0d)), eq(BigDecimal.valueOf(2.0f))))
				.andReturn(Collections.emptyList());
		
		replay(jdbcOperations);
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);
		assertNotNull(query.getRowMapperFactory());
		assertNotNull(query.getBooleanParameterConverter());
		query.query(sql, Integer.class, 1.0d, 2.0f);
		verify(jdbcOperations);
		
		assertEquals(sql, capturedSql.getValue());
		log.info("testQueryForDoubleWrappingInBigDecimal - stop");
	}

	// this test will ONLY work with PostgresJdbcFlavor	
	@Test
	public void testUpdateForDoubleWrappingInBigDecimal() {
		log.info("testUpdateForDoubleWrappingInBigDecimal - start");
		
		final String sql = "dummy sql";
		
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		Capture<CustomPreparedStatementCreator> capturedCreator = Capture.newInstance();
		expect(jdbcOperations.update(capture(capturedCreator))).andReturn(1);
		
		SqlQueryTemplate query = new SqlQueryTemplate(new PostgresJdbcFlavor(), jdbcOperations);

		replay(jdbcOperations);
		query.update(sql, 1.0d, 2.0f);
		verify(jdbcOperations);

		assertEquals(sql, capturedCreator.getValue().getSql());
		assertEquals(BigDecimal.valueOf(1.0d), capturedCreator.getValue().getParams()[0]);
		assertEquals(BigDecimal.valueOf(2.0f), capturedCreator.getValue().getParams()[1]);
		log.info("testUpdateForDoubleWrappingInBigDecimal - stop");
	}
	
}
