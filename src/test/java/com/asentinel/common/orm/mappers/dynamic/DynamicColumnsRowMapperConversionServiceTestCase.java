package com.asentinel.common.orm.mappers.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.jdbc.DefaultObjectFactory;
import com.asentinel.common.orm.mappers.PkColumn;

/**
 * @since 1.71.0
 * @author Razvan Popian
 */
public class DynamicColumnsRowMapperConversionServiceTestCase {
	
	private final ResultSet rs = mock(ResultSet.class);
	private final ConversionService cs = mock(ConversionService.class);

	
	private final DefaultDynamicColumn dc = new DefaultDynamicColumn("SomeTableColumn", String.class, "custom");
	private final DynamicColumnsRowMapper<DefaultDynamicColumn, CustomTypeBean> mapper 
		= new DynamicColumnsRowMapper<>(List.of(dc), new DefaultObjectFactory<>(CustomTypeBean.class), false, "");
	{
		mapper.setConversionService(cs);
	}

	
	@Test
	public void nonNullDynamicFieldValue() throws SQLException {
		Object complexObject = new Object(); // some custom database type like JSON 
		when(rs.getInt("id")).thenReturn(11);
		when(rs.getObject(dc.getDynamicColumnName())).thenReturn(complexObject);
		
		when(cs.canConvert(eq(TypeDescriptor.valueOf(complexObject.getClass())), eq(dc.getTypeDescriptor())))
			.thenReturn(true);
		when(cs.convert(eq(complexObject), eq(TypeDescriptor.valueOf(complexObject.getClass())), eq(dc.getTypeDescriptor())))
			.thenReturn("complexString");
	
		CustomTypeBean b = mapper.mapRow(rs, 0);
		
		assertEquals(11, b.id);
		assertEquals("complexString", b.complexDynamicString);
	}

	@Test
	public void nullDynamicFieldValue() throws SQLException {
		when(rs.getInt("id")).thenReturn(11);
		when(rs.getObject(dc.getDynamicColumnName())).thenReturn(null);
		
		CustomTypeBean b = mapper.mapRow(rs, 0);
		
		assertEquals(11, b.id);
		assertNull(b.complexDynamicString);
	}

	
	private static class CustomTypeBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		
		@PkColumn("id")
		int id;
		
		String complexDynamicString = "some value to be overriden";
		

		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			this.complexDynamicString = (String) value;
			
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			return complexDynamicString;
		}
	}
}
