package com.asentinel.common.orm.mappers;

import static com.asentinel.common.orm.mappers.Const.COL_BIG_DECIMAL;
import static com.asentinel.common.orm.mappers.Const.COL_BIG_INTEGER;
import static com.asentinel.common.orm.mappers.Const.COL_BOOL_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_BOOL_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_DATE;
import static com.asentinel.common.orm.mappers.Const.COL_DOUBLE_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_DOUBLE_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_INT_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_INT_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_LONG_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_LONG_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_NUMBER;
import static com.asentinel.common.orm.mappers.Const.COL_PK;
import static com.asentinel.common.orm.mappers.Const.COL_STRING;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the normal operation of the {@link AnnotationRowMapper} both
 * with fields and methods annotations. It does not test blob fields.
 */
public class AnnotationRowMapperTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(AnnotationRowMapperTestCase.class);
	
	private ResultSet rs;
	private Map<String, Object> mapValues;
	
	@Before
	public void setup() throws SQLException {
		rs = createMock(ResultSet.class);
		mapValues = new LinkedHashMap<String, Object>();

		mapValues.put(COL_INT_VAL, 20);
		mapValues.put(COL_INT_OBJ, 30);
		mapValues.put(COL_LONG_VAL, 100l);
		mapValues.put(COL_LONG_OBJ, 200l);
		mapValues.put(COL_DOUBLE_VAL, 1.1d);
		mapValues.put(COL_DOUBLE_OBJ, 2.2d);
		mapValues.put(COL_BOOL_VAL, "Y");
		mapValues.put(COL_BOOL_OBJ, "N");
		mapValues.put(COL_STRING, "test");
		mapValues.put(COL_DATE, new Timestamp(new Date().getTime()));
		mapValues.put(COL_BIG_DECIMAL, BigDecimal.TEN);
		mapValues.put(COL_BIG_INTEGER, BigDecimal.ONE);
		mapValues.put(COL_NUMBER, BigDecimal.valueOf(100));
		
		expect(rs.getInt(COL_INT_VAL)).andReturn((Integer) mapValues.get(COL_INT_VAL));
		expect(rs.getInt(COL_INT_OBJ)).andReturn((Integer) mapValues.get(COL_INT_OBJ));
		expect(rs.getLong(COL_LONG_VAL)).andReturn((Long) mapValues.get(COL_LONG_VAL));
		expect(rs.getLong(COL_LONG_OBJ)).andReturn((Long) mapValues.get(COL_LONG_OBJ));
		expect(rs.getDouble(COL_DOUBLE_VAL)).andReturn((Double) mapValues.get(COL_DOUBLE_VAL));
		expect(rs.getDouble(COL_DOUBLE_OBJ)).andReturn((Double) mapValues.get(COL_DOUBLE_OBJ));
		expect(rs.getObject(COL_BOOL_VAL)).andReturn((String) mapValues.get(COL_BOOL_VAL));
		expect(rs.getObject(COL_BOOL_OBJ)).andReturn((String) mapValues.get(COL_BOOL_OBJ));
		expect(rs.getObject(COL_STRING)).andReturn((String) mapValues.get(COL_STRING));
		expect(rs.getTimestamp(COL_DATE)).andReturn((Timestamp) mapValues.get(COL_DATE));
		expect(rs.getBigDecimal(COL_BIG_DECIMAL)).andReturn((BigDecimal) mapValues.get(COL_BIG_DECIMAL));
		expect(rs.getBigDecimal(COL_BIG_INTEGER)).andReturn((BigDecimal) mapValues.get(COL_BIG_INTEGER));
		expect(rs.getBigDecimal(COL_NUMBER)).andReturn((BigDecimal) mapValues.get(COL_NUMBER));

		expect(rs.wasNull()).andReturn(false).anyTimes();
	}
	
	private void validateBean1(Bean1 bean1) {
		log.debug("validateBean1 - Bean1 instance: " + bean1);
		assertEquals(mapValues.get(COL_PK), bean1.getPk());
		assertEquals(mapValues.get(COL_INT_VAL), bean1.getIntVal());
		assertEquals(mapValues.get(COL_INT_OBJ), bean1.getIntObj());
		assertEquals(mapValues.get(COL_LONG_VAL), bean1.getLongVal());
		assertEquals(mapValues.get(COL_LONG_OBJ), bean1.getLongObj());
		assertEquals(mapValues.get(COL_DOUBLE_VAL), bean1.getDoubleVal());
		assertEquals(mapValues.get(COL_DOUBLE_OBJ), bean1.getDoubleObj());
		assertEquals(mapValues.get(COL_BOOL_VAL), bean1.isBoolVal()?"Y":"N");
		assertEquals(mapValues.get(COL_BOOL_OBJ), bean1.getBoolObj()?"Y":"N");
		assertEquals(mapValues.get(COL_STRING), bean1.getString());
		assertEquals(mapValues.get(COL_BIG_DECIMAL), bean1.getBigDecimal());
		assertEquals(((BigDecimal) mapValues.get(COL_BIG_INTEGER)).toBigInteger(), bean1.getBigInteger());
		assertEquals(mapValues.get(COL_NUMBER), bean1.getNumber());
	}
	
	@Test
	public void testNormalFieldOperationWithBean1() throws SQLException {
		mapValues.put(COL_PK, 10);
		expect(rs.getInt(COL_PK)).andReturn((Integer) mapValues.get(COL_PK));
		AnnotationRowMapper<Bean1> mapper = new AnnotationRowMapper<Bean1>(Bean1.class);
		replay(rs);
		Bean1 bean1 = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean1(bean1);
	}

	private void validateBean3(Bean3 bean3) {
		log.debug("validateBean3 - Bean1 instance: " + bean3);
		assertEquals(mapValues.get(COL_PK), bean3.getPk());
		assertEquals(mapValues.get(COL_INT_VAL), bean3.getIntVal());
		assertEquals(mapValues.get(COL_INT_OBJ), bean3.getIntObj());
		assertEquals(mapValues.get(COL_LONG_VAL), bean3.getLongVal());
		assertEquals(mapValues.get(COL_LONG_OBJ), bean3.getLongObj());
		assertEquals(mapValues.get(COL_DOUBLE_VAL), bean3.getDoubleVal());
		assertEquals(mapValues.get(COL_DOUBLE_OBJ), bean3.getDoubleObj());
		assertEquals(mapValues.get(COL_BOOL_VAL), bean3.isBoolVal()?"Y":"N");
		assertEquals(mapValues.get(COL_BOOL_OBJ), bean3.getBoolObj()?"Y":"N");
		assertEquals(mapValues.get(COL_STRING), bean3.getString());
		assertEquals(mapValues.get(COL_BIG_DECIMAL), bean3.getBigDecimal());
		assertEquals(((BigDecimal) mapValues.get(COL_BIG_INTEGER)).toBigInteger(), bean3.getBigInteger());
		assertEquals(mapValues.get(COL_NUMBER), bean3.getNumber());
	}
	
	/**
	 * Tests that the {@link AnnotationRowMapper} works properly if the 
	 * target class @Column annotations have the allowNull flag set to true
	 * and the resultset column values are not null.<br>
	 * The other scenario where the resultset column values are null is tested
	 * by the {@link AnnotationRowMapper3NullValuesTestCase} test case.
	 */
	@Test
	public void testNormalFieldOperationWithBean3() throws SQLException {
		mapValues.put(COL_PK, 10);
		expect(rs.getInt(COL_PK)).andReturn((Integer) mapValues.get(COL_PK));
		AnnotationRowMapper<Bean3> mapper = new AnnotationRowMapper<Bean3>(Bean3.class);
		replay(rs);
		Bean3 bean3 = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean3(bean3);
	}
	

	@Test
	public void testNormalFieldOperationIgnorePk() throws SQLException {
		mapValues.put(COL_PK, Integer.MAX_VALUE);
		AnnotationRowMapper<Bean1> mapper = new AnnotationRowMapper<Bean1>(Bean1.class, true);
		replay(rs);
		Bean1 bean1 = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean1(bean1);
	}

	
	private void validateBean2(Bean2 bean2) {
		log.debug("validateBean2 - Bean2 instance: " + bean2);
		assertEquals(mapValues.get(COL_PK), bean2.getPk());
		assertEquals(mapValues.get(COL_INT_VAL), bean2.getIntVal());
		assertEquals(mapValues.get(COL_INT_OBJ), bean2.getIntObj());
		assertEquals(mapValues.get(COL_LONG_VAL), bean2.getLongVal());
		assertEquals(mapValues.get(COL_LONG_OBJ), bean2.getLongObj());
		assertEquals(mapValues.get(COL_DOUBLE_VAL), bean2.getDoubleVal());
		assertEquals(mapValues.get(COL_DOUBLE_OBJ), bean2.getDoubleObj());
		assertEquals(mapValues.get(COL_BOOL_VAL), bean2.isBoolVal()?"Y":"N");
		assertEquals(mapValues.get(COL_BOOL_OBJ), bean2.getBoolObj()?"Y":"N");
		assertEquals(mapValues.get(COL_STRING), bean2.getString());
		assertEquals(mapValues.get(COL_BIG_DECIMAL), bean2.getBigDecimal());
		assertEquals(((BigDecimal) mapValues.get(COL_BIG_INTEGER)).toBigInteger(), bean2.getBigInteger());
		assertEquals(mapValues.get(COL_NUMBER), bean2.getNumber());
	}
	
	
	@Test
	public void testNormalMethodOperation() throws SQLException {
		mapValues.put(COL_PK, 10);
		expect(rs.getInt(COL_PK)).andReturn((Integer) mapValues.get(COL_PK));
		AnnotationRowMapper<Bean2> mapper = new AnnotationRowMapper<Bean2>(Bean2.class);
		replay(rs);
		Bean2 bean2 = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean2(bean2);
	}

	@Test
	public void testNormalMethodOperationIgnorePk() throws SQLException {
		mapValues.put(COL_PK, Integer.MAX_VALUE);
		AnnotationRowMapper<Bean2> mapper = new AnnotationRowMapper<Bean2>(Bean2.class, true);
		replay(rs);
		Bean2 bean2 = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean2(bean2);
	}
	
	
}
