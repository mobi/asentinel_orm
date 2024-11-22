package com.asentinel.common.jdbc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests the following:
 * <p>
 * 	- {@link ResultSetUtils#getResults(CallableStatement, List, int)}
 * 	- {@link ResultSetUtils#toResultSetSqlParameters(RowMapper[])}
 */
public class ResultSetUtils3TestCase {
	private static final Logger log = LoggerFactory.getLogger(ResultSetUtils3TestCase.class);
	
	@Test
	public void testGetResults() throws SQLException {
		log.info("testGetResults - start");
		final int RS_COUNT_MAPPER = 3;
		final int RS_COUNT_HANDLER = 2;
		final int RS_COUNT_UNUSED = 2;
		final int RS_SIZE = 3;

		CallableStatement cs = createStrictMock(CallableStatement.class);
		List<ResultSet> rss = new ArrayList<>();
		for (int i=1; i <= RS_COUNT_MAPPER; i++) {
			ResultSet rs = createStrictMock(ResultSet.class);
			rss.add(rs);
			expect(cs.getObject(i)).andReturn(rs);
			for (int j=1; j <= RS_SIZE; j++) {
				expect(rs.next()).andReturn(true);
				expect(rs.getRow()).andReturn(j);
			}
			expect(rs.next()).andReturn(false);
			if (log.isTraceEnabled()) {
				expect(rs.getFetchSize()).andReturn(10);
			}
			rs.close();		
		}
		
		for (int i=RS_COUNT_MAPPER + 1; 
				i <= RS_COUNT_MAPPER + RS_COUNT_HANDLER; 
				i++) {
			ResultSet rs = createStrictMock(ResultSet.class);
			rss.add(rs);
			expect(cs.getObject(i)).andReturn(rs);
			for (int j=1; j <= RS_SIZE; j++) {
				expect(rs.next()).andReturn(true);
			}
			expect(rs.next()).andReturn(false);
			if (log.isTraceEnabled()) {
				expect(rs.getFetchSize()).andReturn(10);
			}
			rs.close();		
		}
		
		for (int i=RS_COUNT_MAPPER + RS_COUNT_HANDLER + 1; 
				i <= RS_COUNT_MAPPER + RS_COUNT_HANDLER + RS_COUNT_UNUSED; 
				i++) {
			ResultSet rs = createStrictMock(ResultSet.class);
			rss.add(rs);
			expect(cs.getObject(i)).andReturn(rs);
			rs.close();		
		}
		
		List<ResultSetSqlParameter> rsParams = new ArrayList<>();
		for (int i=1; i <= RS_COUNT_MAPPER; i++) {
				rsParams.add(new ResultSetSqlParameter((RowMapper<Integer>) (rs, rowNum) -> {
                    // do nothing, the test scope is not in this method
                    return null;
                }));
		}
		
		for (int i=1; i <= RS_COUNT_HANDLER; i++) {
			rsParams.add(new ResultSetSqlParameter(rs -> {
                // do nothing, the test scope is not in this method
            }));
		}
		
		for (int i=1; i <= RS_COUNT_UNUSED; i++) {
			rsParams.add(null);
		}

		
		replay(cs);
		replay((Object[]) rss.toArray(new ResultSet[0]));
		List<List<?>> results = ResultSetUtils.getResults(cs, rsParams, 0);
		log.debug("testGetResults - Results: {}", results);
		verify(cs);
		verify((Object[]) rss.toArray(new ResultSet[0]));
		
		assertEquals(RS_COUNT_MAPPER + RS_COUNT_HANDLER + RS_COUNT_UNUSED, results.size());
		
		for (int i=1; i <= RS_COUNT_MAPPER; i++) {
			assertEquals(RS_SIZE, results.get(i-1).size());
		}
		
		for (int i=RS_COUNT_MAPPER + 1;
				i <= RS_COUNT_MAPPER + RS_COUNT_HANDLER; 
				i++) {
			assertNull(results.get(i-1));
		}
		
		for (int i=RS_COUNT_MAPPER + RS_COUNT_HANDLER + 1; 
				i <= RS_COUNT_MAPPER + RS_COUNT_HANDLER + RS_COUNT_UNUSED; 
				i++) {
			assertNull(results.get(i-1));
		}

		log.info("testGetResults - stop");
	}
	
	@Test
	public void testToResultSetSqlParameters() {
		RowMapper<?>[] rowMappers = {
				ReusableRowMappers.ROW_MAPPER_INTEGER,
				null,
				ReusableRowMappers.ROW_MAPPER_BIG_INTEGER
		};
		
		ResultSetSqlParameter[] rsParams = ResultSetUtils.toResultSetSqlParameters(rowMappers);
		
		assertEquals(rowMappers.length, rsParams.length);
		for (int i=0; i<rowMappers.length; i++) {
			if (rowMappers[i] == null) {
				assertNull(rsParams[i]);
			} else {
				assertSame(rowMappers[i], rsParams[i].getRowMapper());
			}
		}
	}
}
