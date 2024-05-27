package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.jdbc.core.RowCallbackHandler;

public class ResultSetRowMapperTestCase {
	
	ResultSet rs = createMock(ResultSet.class);
	ResultSet rs1 = createMock(ResultSet.class);
	ResultSet rs2 = createMock(ResultSet.class);

	@Test
	public void testNormalOperation_RowMapper() throws SQLException {
		ResultSetRowMapper m = new ResultSetRowMapper(ReusableRowMappers.ROW_MAPPER_INTEGER, ReusableRowMappers.ROW_MAPPER_STRING);
		expect(rs.getObject(1)).andReturn(rs1);
		expect(rs1.getRow()).andReturn(-1).anyTimes(); // not used, the return value does not matter
		expect(rs1.wasNull()).andReturn(false).anyTimes();
		expect(rs1.getFetchSize()).andReturn(10).anyTimes();
		expect(rs1.next()).andReturn(true);
		expect(rs1.getInt(1)).andReturn(10);
		expect(rs1.next()).andReturn(true);
		expect(rs1.getInt(1)).andReturn(20);
		expect(rs1.next()).andReturn(false);
		rs1.close();
		
		replay(rs, rs1);
		List<?> list = m.mapRow(rs, 0);
		verify(rs, rs1);
		
		assertEquals(Arrays.asList(10, 20), list);
	}
	
	@Test
	public void testNormalOperation_RowCallbackHandler() throws SQLException {
		RowCallbackHandler rch = createMock(RowCallbackHandler.class);
		rch.processRow(rs1);
		expectLastCall().times(2);
		ResultSetRowMapper m = new ResultSetRowMapper(
			new ResultSetSqlParameter[] {
				new ResultSetSqlParameter(rch),
				null
			}
		);
		expect(rs.getObject(1)).andReturn(rs1);
		expect(rs1.getFetchSize()).andReturn(10).anyTimes();
		expect(rs1.next()).andReturn(true);
		expect(rs1.next()).andReturn(true);
		expect(rs1.next()).andReturn(false);
		rs1.close();
		
		replay(rs, rs1, rch);
		List<?> list = m.mapRow(rs, 0);
		verify(rs, rs1, rch);
		
		assertNull(list);
	}

	@Test
	public void testNormalOperation_WithNull() throws SQLException {
		ResultSetRowMapper m = new ResultSetRowMapper(
			new ResultSetSqlParameter[] {
				null
			}
		);
		expect(rs.getObject(1)).andReturn(rs1);
		rs1.close();
		
		replay(rs, rs1);
		List<?> list = m.mapRow(rs, 0);
		verify(rs, rs1);
		
		assertNull(list);
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidRowIndex1() throws SQLException {
		ResultSetRowMapper m = new ResultSetRowMapper(
			new ResultSetSqlParameter[] {
				null
			}
		);
		
		m.mapRow(rs, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidRowIndex2() throws SQLException {
		ResultSetRowMapper m = new ResultSetRowMapper(
			new ResultSetSqlParameter[] {
				null
			}
		);
		
		m.mapRow(rs, -10);
	}
	
}
