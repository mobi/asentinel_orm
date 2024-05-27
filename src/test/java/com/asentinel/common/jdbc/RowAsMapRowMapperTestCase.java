package com.asentinel.common.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.asentinel.common.jdbc.AbstractRowMapper.Flag;

public class RowAsMapRowMapperTestCase {
	private final static Logger log = LoggerFactory.getLogger(RowAsMapRowMapperTestCase.class);
	
	@Test
	public void testRowAsMapDefaultOperation() throws SQLException {
		log.info("testRowAsMapDefaultOperation - start");
		
		Statement st = createMock(Statement.class);
		expect(st.getConnection()).andReturn(null).anyTimes();
		
		ResultSet rs = createMock(ResultSet.class);
		ResultSetMetaData md = createMock(ResultSetMetaData.class);
		RowAsMapRowMapper mapper = new RowAsMapRowMapper();
		
		final Date d = new Date();
		final BigDecimal bigDecimal = BigDecimal.TEN;
		final BigInteger bigInteger = BigInteger.TEN;
		
		expect(rs.getMetaData()).andReturn(md).anyTimes();
		
		int i = 1;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.VARCHAR);
		expect(rs.getObject(i)).andReturn("val_" + i);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.CHAR);
		expect(rs.getObject(i)).andReturn("val_" + i);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.CLOB);
		expect(rs.getObject(i)).andReturn("val_" + i);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.TIMESTAMP);
		expect(rs.getTimestamp(i)).andReturn(new Timestamp(d.getTime()));
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.DATE);
		expect(rs.getTimestamp(i)).andReturn(new Timestamp(d.getTime()));
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.NUMERIC);
		expect(md.getScale(i)).andReturn(2);
		expect(rs.getBigDecimal(i)).andReturn(bigDecimal);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.NUMERIC);
		expect(md.getScale(i)).andReturn(0);
		expect(rs.getBigDecimal(i)).andReturn(new BigDecimal(bigInteger));
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.BLOB);
		expect(rs.getBytes(i)).andReturn(new byte[]{1});
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.OTHER);
		expect(rs.getObject(i)).andReturn("test");
		i++;
		
		
		expect(md.getColumnCount()).andReturn(i - 1);
		
		expect(rs.getStatement()).andReturn(st).anyTimes();
		
		replay(rs, md, st);
		Map<String, Object> map = mapper.mapRow(rs, 1);
		verify(rs, md, st);
		
		assertEquals(i - 1, map.size());
		i = 1;
		assertEquals(map.get("COl_" + i), "val_" + i);
		i++;
		assertEquals(map.get("COl_" + i), "val_" + i);
		i++;
		assertEquals(map.get("COl_" + i), "val_" + i);
		i++;
		assertEquals(map.get("COl_" + i), d);
		i++;
		assertEquals(map.get("cOL_" + i), d);
		i++;
		assertEquals(map.get("cOL_" + i), bigDecimal);
		i++;
		assertEquals(map.get("cOL_" + i), bigInteger);
		i++;
		assertEquals(((byte[])map.get("cOL_" + i))[0], 1);
		i++;
		assertEquals(map.get("COl_" + i), "test");
		i++;
				
		log.info("testRowAsMapDefaultOperation - stop");
	}
	
	@Test
	public void testRowAsMapWithNulls() throws SQLException {
		log.info("testRowAsMapWithNulls - start");
		
		RowAsMapRowMapper mapper = new RowAsMapRowMapper();
		commonRowAsMapWithNulls(mapper);
		
		log.info("testRowAsMapWithNulls - end");
	}
	
	@Test
	public void testRowAsMapWithNullsDefaultToNull() throws SQLException {
		log.info("testRowAsMapWithNulls - start");
		
		RowAsMapRowMapper mapper = new RowAsMapRowMapper(EnumSet.of(Flag.DEFAULT_TO_NULL));
		commonRowAsMapWithNulls(mapper);
		
		log.info("testRowAsMapWithNulls - end");
	}
	
	private void commonRowAsMapWithNulls(RowAsMapRowMapper mapper) throws SQLException {		
		Statement st = createMock(Statement.class);
		expect(st.getConnection()).andReturn(null).anyTimes();
		
		ResultSet rs = createMock(ResultSet.class);
		ResultSetMetaData md = createMock(ResultSetMetaData.class);
		
		expect(rs.getMetaData()).andReturn(md).anyTimes();
		
		int i = 1;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.VARCHAR);
		expect(rs.getObject(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.CHAR);
		expect(rs.getObject(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.CLOB);
		expect(rs.getObject(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.TIMESTAMP);
		expect(rs.getTimestamp(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.DATE);
		expect(rs.getTimestamp(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.NUMERIC);
		expect(md.getScale(i)).andReturn(2);
		expect(rs.getBigDecimal(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.NUMERIC);
		expect(md.getScale(i)).andReturn(0);
		expect(rs.getBigDecimal(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.BLOB);
		expect(rs.getBytes(i)).andReturn(null);
		i++;
		expect(md.getColumnName(i)).andReturn("col_" + i);
		expect(md.getColumnType(i)).andReturn(Types.OTHER);
		expect(rs.getObject(i)).andReturn(null);
		i++;
		
		
		expect(md.getColumnCount()).andReturn(i - 1);
		
		expect(rs.getStatement()).andReturn(st).anyTimes();
		
		replay(rs, md, st);
		Map<String, Object> map = mapper.mapRow(rs, 1);
		verify(rs, md, st);
		
		assertEquals(i - 1, map.size());
		i = 1;
		assertEquals(map.get("COl_" + i), mapper.isDefaultToNull() ? null : "");
		i++;
		assertEquals(map.get("COl_" + i), mapper.isDefaultToNull() ? null : "");
		i++;
		assertEquals(map.get("COl_" + i), mapper.isDefaultToNull() ? null : "");
		i++;
		assertEquals(map.get("COl_" + i), null);
		i++;
		assertEquals(map.get("cOL_" + i), null);
		i++;
		assertEquals(map.get("cOL_" + i), mapper.isDefaultToNull() ? null : BigDecimal.ZERO);
		i++;
		assertEquals(map.get("cOL_" + i), mapper.isDefaultToNull() ? null : BigInteger.ZERO);
		i++;
		assertEquals(((byte[])map.get("cOL_" + i)), null);
		i++;
		assertEquals(map.get("COl_" + i), null);
		i++;
	}
	
	@Test
	public void testForceAllNumbersAsBigDecimal() throws SQLException {
		log.info("testForceAllNumbersAsBigDecimal - start");
		
		ResultSet rs = createMock(ResultSet.class);
		ResultSetMetaData md = createMock(ResultSetMetaData.class);
		RowAsMapRowMapper mapper = new RowAsMapRowMapper(EnumSet.of(Flag.ALL_NUMBERS_AS_BIG_DECIMAL));
		
		expect(md.getColumnCount()).andReturn(1).anyTimes();
		
		expect(rs.getMetaData()).andReturn(md).anyTimes();
		
		expect(md.getColumnName(1)).andReturn("col");
		expect(md.getColumnType(1)).andReturn(Types.NUMERIC);
		expect(rs.getBigDecimal(1)).andReturn(new BigDecimal(17));

		replay(rs, md);
		Map<String, Object> map = mapper.mapRow(rs, 1);
		verify(rs, md);
		
		assertEquals(1, map.size());
		assertEquals(new BigDecimal(17), map.get("Col"));

		
		log.info("testForceAllNumbersAsBigDecimal - stop");
	}
	
	
	
}
