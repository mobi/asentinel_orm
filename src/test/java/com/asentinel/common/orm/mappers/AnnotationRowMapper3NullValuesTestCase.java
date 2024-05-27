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
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests the allowNull feature with NULL values in the resultset.
 * 
 * @see AnnotationRowMapperTestCase#testNormalFieldOperationWithBean3()
 */
public class AnnotationRowMapper3NullValuesTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(AnnotationRowMapper3NullValuesTestCase.class);
	
	
	private ResultSet rs;
	private Map<String, Object> mapValues;
	
	@Before
	public void setup() throws SQLException {
		rs = createMock(ResultSet.class);
		mapValues = new LinkedHashMap<String, Object>();

		mapValues.put(COL_INT_VAL, 0);
		mapValues.put(COL_INT_OBJ, 0);
		mapValues.put(COL_LONG_VAL, 0l);
		mapValues.put(COL_LONG_OBJ, 0l);
		mapValues.put(COL_DOUBLE_VAL, 0d);
		mapValues.put(COL_DOUBLE_OBJ, 0d);
		mapValues.put(COL_BOOL_VAL, "N");
		mapValues.put(COL_BOOL_OBJ, null);
		mapValues.put(COL_STRING, null);
		mapValues.put(COL_DATE, null);
		mapValues.put(COL_BIG_DECIMAL, null);
		mapValues.put(COL_BIG_INTEGER, null);
		mapValues.put(COL_NUMBER, null);
		
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

		expect(rs.wasNull()).andReturn(true).anyTimes();
	}
	
	private void validateBean3(Bean3 bean) {
		log.debug("validateBean3 - bean instance: " + bean);
		assertEquals(mapValues.get(COL_PK), bean.getPk());
		assertEquals(mapValues.get(COL_INT_VAL), bean.getIntVal());
		assertNull(bean.getIntObj());
		assertEquals(mapValues.get(COL_LONG_VAL), bean.getLongVal());
		assertNull(bean.getLongObj());
		assertEquals(mapValues.get(COL_DOUBLE_VAL), bean.getDoubleVal());
		assertNull(bean.getDoubleObj());
		assertEquals(mapValues.get(COL_BOOL_VAL), bean.isBoolVal()?"Y":"N");
		assertNull(bean.getBoolObj());
		assertNull(bean.getString());
		assertNull(bean.getBigDecimal());
		assertNull(bean.getBigInteger());
		assertNull(bean.getNumber());
	}
	
	@Test
	public void testNormalFieldOperation() throws SQLException {
		mapValues.put(COL_PK, 0);
		expect(rs.getInt(COL_PK)).andReturn((Integer) mapValues.get(COL_PK)).anyTimes();
		AnnotationRowMapper<Bean3> mapper = new AnnotationRowMapper<Bean3>(Bean3.class);
		replay(rs);
		Bean3 bean = mapper.mapRow(rs, 1);
		verify(rs);
		validateBean3(bean);
	}
	
	
}
