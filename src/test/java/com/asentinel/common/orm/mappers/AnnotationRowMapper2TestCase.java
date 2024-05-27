package com.asentinel.common.orm.mappers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

/**
 * Tests error/warning situations.
 */
public class AnnotationRowMapper2TestCase {
	private final static Logger log = LoggerFactory.getLogger(AnnotationRowMapper2TestCase.class);
	
	private final static String COL_ID = "id";
	private final static String COL_NAME = "name";

	
	@Test
	public void testMissingColumn() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(COL_NAME)).andThrow(new SQLException("Missing column."));
	
		AnnotationRowMapper<OkBean> mapper = new AnnotationRowMapper<OkBean>(OkBean.class);
		replay(rs);
		try {
			mapper.mapRow(rs, 1);
			fail("Should not get to this line.");
		} catch(SQLException e) {
			log.debug("Expected exception: " + e.getMessage());
		}
		verify(rs);
	}
	
	@Test
	public void testInvalidType() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getObject(COL_NAME)).andThrow(new SQLException("Invalid type."));
	
		AnnotationRowMapper<OkBean> mapper = new AnnotationRowMapper<OkBean>(OkBean.class);
		replay(rs);
		try {
			mapper.mapRow(rs, 1);
			fail("Should not get to this line.");
		} catch(SQLException e) {
			log.debug("testInvalidType - Expected exception: " + e.getMessage());
		}
		verify(rs);
	}
	
	
	@Test
	public void testDoubleColumnAnnotation() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(COL_ID)).andReturn(1);
		expect(rs.getObject(COL_NAME)).andReturn("test");
		expect(rs.wasNull()).andReturn(false).anyTimes();
	
		AnnotationRowMapper<Bean> mapper = new AnnotationRowMapper<Bean>(Bean.class);
		replay(rs);
		Bean b = mapper.mapRow(rs, 1);
		verify(rs);
		
		assertFalse("The set method for the name field should not be called.", b.testFlagName);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidColumnAnnotatedMethod() throws SQLException {
		new AnnotationRowMapper<InvalidColumnAnnBean>(InvalidColumnAnnBean.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPkColumnAnnotatedMethod() throws SQLException {
		new AnnotationRowMapper<InvalidPkColumnAnnBean>(InvalidPkColumnAnnBean.class);
	}
	
	
	@Test
	public void testUnsupportedFieldType() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
	
		AnnotationRowMapper<UnsupportedTypeBean> mapper = new AnnotationRowMapper<UnsupportedTypeBean>(UnsupportedTypeBean.class);
		replay(rs);
		try {
			mapper.mapRow(rs, 1);
			fail("Should not get to this line.");
		} catch(SQLException e) {
			log.debug("testUnsupportedFieldType - Expected exception: " + e.getMessage());
		}
		verify(rs);
	}
	
	
	
	// beans used in tests
	
	public static class OkBean {
		@Column(COL_NAME)
		private String name;
	}
	
	public static class Bean {
		
		boolean testFlagName = false;
		
		@PkColumn(COL_ID)
		private int id;
		
		@Column(COL_NAME)
		private String name;

		public String getName() {
			return name;
		}

		@Column(COL_NAME)
		private void setName(String name) {
			this.testFlagName = true;
			this.name = name;
		}

		public int getId() {
			return id;
		}

	}
	
	
	public static class InvalidColumnAnnBean {
	
		@Column(COL_NAME)
		public void setValue() {
			
		}
	}
	
	public static class InvalidPkColumnAnnBean {
		
		@PkColumn(COL_NAME)
		public void setValue() {
			
		}
		
	}
	
	public static class UnsupportedTypeBean {
		@Column(COL_NAME)
		Object o;
	}
	
}
