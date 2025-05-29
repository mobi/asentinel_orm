package com.asentinel.common.orm.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameter;

/**
 * @since 1.71.0
 * @author Razvan Popian
 */
public class SqlParameterTypeDescriptorTestCase {

	private static final Logger log = LoggerFactory.getLogger(SqlParameterTypeDescriptorTestCase.class);
	
	@Test
	public void toStringTest() {
		SqlParameterTypeDescriptor td = new SqlParameterTypeDescriptor(new SqlParameter(0, "json"));
		log.debug("toStringTest - {}", td.toString());
		assertTrue(td.toString().contains("json"));
	}
	
	@Test
	public void toStringNullTest() {
		SqlParameterTypeDescriptor td = new SqlParameterTypeDescriptor(new SqlParameter(0, null));
		log.debug("toStringTest - {}", td.toString());
		assertTrue(td.toString().contains("null"));
	}
	
	@Test
	public void equalsTest() {
		SqlParameterTypeDescriptor td1 = new SqlParameterTypeDescriptor(new SqlParameter(0, "type"));
		SqlParameterTypeDescriptor td2 = new SqlParameterTypeDescriptor(new SqlParameter(0, "type"));
		
		assertEquals(td1, td2);
		assertEquals(td2, td1);
		
		assertEquals(td1.hashCode(), td2.hashCode());
	}
	
	@Test
	public void equalsNullTest() {
		SqlParameterTypeDescriptor td1 = new SqlParameterTypeDescriptor(new SqlParameter(0, null));
		SqlParameterTypeDescriptor td2 = new SqlParameterTypeDescriptor(new SqlParameter(0, null));
		
		assertEquals(td1, td2);
		assertEquals(td2, td1);
		
		assertEquals(td1.hashCode(), td2.hashCode());
	}

	
	@Test
	public void notEqualsTest() {
		SqlParameterTypeDescriptor td1 = new SqlParameterTypeDescriptor(new SqlParameter(0, "type1"));
		SqlParameterTypeDescriptor td2 = new SqlParameterTypeDescriptor(new SqlParameter(0, "type2"));
		
		assertNotEquals(td1, td2);
		assertNotEquals(td2, td1);
		
		assertNotEquals(td1.hashCode(), td2.hashCode());
	}

	@Test
	public void notEqualsNullTest() {
		SqlParameterTypeDescriptor td1 = new SqlParameterTypeDescriptor(new SqlParameter(0, null));
		SqlParameterTypeDescriptor td2 = new SqlParameterTypeDescriptor(new SqlParameter(0, "type"));
		
		assertNotEquals(td1, td2);
		assertNotEquals(td2, td1);
		
		assertNotEquals(td1.hashCode(), td2.hashCode());
	}
}
