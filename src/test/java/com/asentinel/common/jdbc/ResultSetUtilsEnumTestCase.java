package com.asentinel.common.jdbc;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class ResultSetUtilsEnumTestCase {
	
	private final ResultSet rs = mock(ResultSet.class);
	
	// PlainEnum
	
	@Test
	public void plainEnumValidValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("b");
		
		PlainEnum e = (PlainEnum) ResultSetUtils.getEnum(rs, "enum", PlainEnum.class);
		
		assertSame(PlainEnum.B, e);
	}

	@Test
	public void plainEnumNullValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn(null);
		
		PlainEnum e = (PlainEnum) ResultSetUtils.getEnum(rs, "enum", PlainEnum.class);
		
		assertNull(e);
	}
	
	@Test
	public void plainEnumEmptyValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("");
		
		PlainEnum e = (PlainEnum) ResultSetUtils.getEnum(rs, "enum", PlainEnum.class);
		
		assertNull(e);
	}	

	@Test(expected = SQLException.class)
	public void plainEnumInvalidValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("d");
		
		ResultSetUtils.getEnum(rs, "enum", PlainEnum.class);
	}


	// EnumIdEnum
	
	@Test
	public void enumIdValidValue() throws SQLException {
		when(rs.getInt("enum")).thenReturn(2);
		
		EnumIdEnum e = (EnumIdEnum) ResultSetUtils.getEnum(rs, "enum", EnumIdEnum.class);
		
		assertSame(EnumIdEnum.B, e);
	}

	@Test
	public void enumIdNullValue() throws SQLException {
		when(rs.getInt("enum")).thenReturn(0);
		when(rs.wasNull()).thenReturn(true);
		
		EnumIdEnum e = (EnumIdEnum) ResultSetUtils.getEnum(rs, "enum", EnumIdEnum.class);
		
		assertNull(e);
	}

	@Test(expected = SQLException.class)
	public void enumIdInvalidValue() throws SQLException {
		when(rs.getInt("enum")).thenReturn(4);
		
		ResultSetUtils.getEnum(rs, "enum", EnumIdEnum.class);
	}
	
	
	// StringEnumIdEnum
	
	@Test
	public void stringEnumIdEnumValidValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("b");
		
		StringEnumIdEnum e = (StringEnumIdEnum) ResultSetUtils.getEnum(rs, "enum", StringEnumIdEnum.class);
		
		assertSame(StringEnumIdEnum.B, e);
	}

	@Test
	public void stringEnumIdEnumNullValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn(null);
		
		StringEnumIdEnum e = (StringEnumIdEnum) ResultSetUtils.getEnum(rs, "enum", StringEnumIdEnum.class);
		
		assertNull(e);
	}
	
	@Test
	public void stringEnumIdEnumEmptyValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("");
		
		StringEnumIdEnum e = (StringEnumIdEnum) ResultSetUtils.getEnum(rs, "enum", StringEnumIdEnum.class);
		
		assertNull(e);
	}	

	@Test(expected = SQLException.class)
	public void stringEnumIdEnumInvalidValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("d");
		
		ResultSetUtils.getEnum(rs, "enum", StringEnumIdEnum.class);
	}

	@Test(expected = SQLException.class)
	public void stringEnumIdEnumInvalidUpperCaseValue() throws SQLException {
		when(rs.getObject("enum")).thenReturn("B");
		
		ResultSetUtils.getEnum(rs, "enum", StringEnumIdEnum.class);
	}
	
	private enum PlainEnum {
		A, B, C
	}
	
	private enum EnumIdEnum implements EnumId<Integer> {
		A(1), B(2), C(3);
		
		private final int id;
		
		EnumIdEnum(int id) {
			this.id = id;
		}

		@Override
		public Integer getId() {
			return id;
		}
	}

	private enum StringEnumIdEnum implements EnumId<String> {
		A("a"), B("b"), C("c");
		
		private final String id;
		
		StringEnumIdEnum(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}
	}

}
