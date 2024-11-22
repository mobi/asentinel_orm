package com.asentinel.common.jdbc.flavors.oracle;

import static com.asentinel.common.jdbc.ReusableRowMappers.ROW_MAPPER_BIG_INTEGER;
import static com.asentinel.common.jdbc.ReusableRowMappers.ROW_MAPPER_INTEGER;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.*;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.asentinel.common.jdbc.BooleanParameterConverter;
import com.asentinel.common.jdbc.ResultSetSqlParameter;
import com.asentinel.common.jdbc.ResultSetUtils2TestCase;
import com.asentinel.common.jdbc.ResultSetUtilsTestCase;

/**
 * Tests the execution and ResultSet opening for a {@link CallableStatement}. Ensures that
 * every opened ResultSet is closed. The actual conversion of ResultSet rows to objects 
 * is not tested here, but in the {@link ResultSetUtilsTestCase} and {@link ResultSetUtils2TestCase}. 
 */
public class InOutCallableStatementCallbackTestCase {
	
	private static final Logger log = LoggerFactory.getLogger(InOutCallableStatementCallbackTestCase.class);
	
	private int countBooleans(Object ... inParams) {
		int count = 0;
		for (Object o:inParams) {
			if (o instanceof Boolean
					|| o == BooleanParameterConverter.NULL_BOOLEAN) {
				count++;
			}
		}
		return count;
	}
	
	private void testExec(ResultSetSqlParameter[] rsParams, Object ... inParams) throws SQLException {
		List<Object> mocks = new ArrayList<>();
		CallableStatement cs = createMock(CallableStatement.class);
		mocks.add(cs);
		
		expect(cs.execute()).andReturn(false);
		if (log.isTraceEnabled()) {
			expect(cs.getFetchSize()).andReturn(10);
		}
		if (rsParams != null) {
			for (int i=0; i<rsParams.length; i++) {
				ResultSet rs = createNiceMock(ResultSet.class);
				expect(cs.getObject(inParams.length - countBooleans(inParams) + i + 1)).andReturn(rs);
				rs.close();
				mocks.add(rs);
			}
		}
		
		InOutCallableStatementCallback action = new InOutCallableStatementCallback(rsParams, inParams);
		
		replay(mocks.toArray(new Object[0]));
		
		action.doInCallableStatement(cs);
		
		verify(mocks.toArray(new Object[0]));
	}
	
	
	@Test
	public void testExec() throws SQLException {
		log.info("testExec - start");
		
		Object[] inParams = new Object[4];
		testExec(new ResultSetSqlParameter[]{}, inParams);
		testExec(new ResultSetSqlParameter[]{null, null}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), new ResultSetSqlParameter(ROW_MAPPER_BIG_INTEGER)}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), null, new ResultSetSqlParameter(ROW_MAPPER_INTEGER)}, inParams);
		
		log.info("testExec - passed for inParams WITHOUT booleans.");
		
		inParams[0] = Boolean.TRUE;
		inParams[2] = Boolean.FALSE;
		
		testExec(new ResultSetSqlParameter[]{}, inParams);
		testExec(new ResultSetSqlParameter[]{null, null}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), new ResultSetSqlParameter(ROW_MAPPER_BIG_INTEGER)}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), null, new ResultSetSqlParameter(ROW_MAPPER_INTEGER)}, inParams);

		log.info("testExec - passed for inParams WITH booleans.");
		
		inParams[3] = BooleanParameterConverter.NULL_BOOLEAN;
		testExec(new ResultSetSqlParameter[]{}, inParams);
		testExec(new ResultSetSqlParameter[]{null, null}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), new ResultSetSqlParameter(ROW_MAPPER_BIG_INTEGER)}, inParams);
		testExec(new ResultSetSqlParameter[]{new ResultSetSqlParameter(ROW_MAPPER_INTEGER), null, new ResultSetSqlParameter(ROW_MAPPER_INTEGER)}, inParams);
		
		log.info("testExec - passed for inParams WITH NULL booleans.");


		log.info("testExec - stop");
	}
	
	@Test
	public void countPreparedInputParams() {
		
		assertEquals(0, InOutCallableStatementCallback.countPreparedInputParams((Object[]) null));
		
		assertEquals(3, InOutCallableStatementCallback.countPreparedInputParams(10, "test", null));
		
		assertEquals(3, InOutCallableStatementCallback.countPreparedInputParams(10, "test", null, true, BooleanParameterConverter.NULL_BOOLEAN));
	}
}
