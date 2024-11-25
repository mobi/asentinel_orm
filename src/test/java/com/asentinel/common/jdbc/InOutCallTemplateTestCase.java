package com.asentinel.common.jdbc;

import com.asentinel.common.jdbc.flavors.CallableStatementCreatorSupport;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import org.easymock.Capture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.transaction.support.TransactionOperations;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test that validates the InOutCallTemplate delegation for call execution to 
 * the JdbcOperations.
 */
public class InOutCallTemplateTestCase {
	private static final Logger log = LoggerFactory.getLogger(InOutCallTemplateTestCase.class);

	JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor(); // should be a mock

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void testCall_ImplicitTransaction() {
		log.info("testCall_ImplicitTransaction - start");
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		InOutCallTemplate call = new InOutCallTemplate(jdbcFlavor, jdbcOperations);

		final Object ret = new Object();
		CallableStatementCallback action = cs -> ret;

		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<Object>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);

		Object object = call.call("test", action, 1, 1, 2);

		verify(jdbcOperations);

		assertEquals(ret, object);
		log.info("testCall_ImplicitTransaction - stop");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCall_ExplicitTransaction() {
		log.info("testCall_ExplicitTransaction - start");
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		TransactionOperations tOps = createMock(TransactionOperations.class);
		InOutCallTemplate call = new InOutCallTemplate(jdbcFlavor, jdbcOperations);
		call.setTransactionOperations(tOps);

		final Object ret = new Object();

		expect(tOps.execute(anyObject()))
				.andReturn(ret);

		replay(jdbcOperations, tOps);

		Object object = call.call("test", (CallableStatementCallback<?>) null, 1, 1, 2);

		verify(jdbcOperations, tOps);

		assertEquals(ret, object);
		log.info("testCall_ExplicitTransaction - stop");
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void testCallForObjectWithClass() {
		log.info("testCallForObject - start");

		// test correct operation
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		InOutCallTemplate call = new InOutCallTemplate(jdbcFlavor, jdbcOperations);
		List<List<?>> ret = new ArrayList<>();
		List<Integer> rs = new ArrayList<>();
		final Integer value = 1;
		rs.add(value);
		ret.add(rs);
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<Object>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		Integer object = call.callForObject("test", Integer.class);
		verify(jdbcOperations);
		assertEquals(value, object);

		// test empty result set
		reset(jdbcOperations);
		rs.clear();
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<Object>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		try {
			call.callForObject("test", Integer.class);
			fail("Should throw exception.");
		} catch (EmptyResultDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);

		// test too many rows result set
		reset(jdbcOperations);
		rs.add(1);
		rs.add(2);
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<Object>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		try {
			call.callForObject("test", Integer.class);
			fail("Should throw exception.");
		} catch (IncorrectResultSizeDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);

		log.info("testCallForObject - stop");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void testCallForObjectWithRowMapper() {
		log.info("testCallForObject - start");

		// test correct operation
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		InOutCallTemplate call = new InOutCallTemplate(jdbcFlavor, jdbcOperations);
		List<List<?>> ret = new ArrayList<>();
		List<Integer> rs = new ArrayList<>();
		final Integer value = 1;
		rs.add(value);
		ret.add(rs);
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<List>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		Integer object = call.callForObject("test", ReusableRowMappers.ROW_MAPPER_INTEGER);
		verify(jdbcOperations);
		assertEquals(value, object);

		// test empty result set
		reset(jdbcOperations);
		rs.clear();
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<List>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		try {
			call.callForObject("test", ReusableRowMappers.ROW_MAPPER_INTEGER);
			fail("Should throw exception.");
		} catch (EmptyResultDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);

		// test too many rows resultset
		reset(jdbcOperations);
		rs.add(1);
		rs.add(2);
		expect(jdbcOperations.execute((CallableStatementCreator) anyObject(),
				(CallableStatementCallback<List>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);
		try {
			call.callForObject("test", ReusableRowMappers.ROW_MAPPER_INTEGER);
			fail("Should throw exception.");
		} catch (IncorrectResultSizeDataAccessException e) {
			log.info("testCallForObject - Expected exception: ", e);
		}
		verify(jdbcOperations);

		log.info("testCallForObject - stop");
	}

	// This test will work ONLY with PostgresJdbcFlavor
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void testCallWithWrappedInBigDecimalDouble() {
		log.info("testCallWithWrappedInBigDecimalDouble - start");
		JdbcOperations jdbcOperations = createMock(JdbcOperations.class);
		InOutCallTemplate call = new InOutCallTemplate(jdbcFlavor, jdbcOperations);

		final Object ret = new Object();
		CallableStatementCallback action = new CallableStatementCallback() {
			@Override
			public Object doInCallableStatement(CallableStatement cs) {
				return ret;
			}
		};

		Capture<CallableStatementCreatorSupport> csc = Capture.newInstance();

		expect(jdbcOperations.execute(capture(csc),
				(CallableStatementCallback<Object>) anyObject(CallableStatementCallback.class)))
				.andReturn(ret);

		replay(jdbcOperations);

		Object object = call.call("test", action, 1, 1.0d, 2.0f);

		verify(jdbcOperations);

		assertEquals(ret, object);
		assertEquals(BigDecimal.valueOf(1.0d), csc.getValue().getInParams()[0]);
		assertEquals(BigDecimal.valueOf(2.0d), csc.getValue().getInParams()[1]);

		log.info("testCallWithWrappedInBigDecimalDouble - stop");
	}
}
