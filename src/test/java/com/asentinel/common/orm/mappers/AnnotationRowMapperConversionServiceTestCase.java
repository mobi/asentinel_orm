package com.asentinel.common.orm.mappers;

import static com.asentinel.common.orm.mappers.Const.COL_CUSTOM_OBJECT;
import static com.asentinel.common.orm.mappers.Const.COL_PK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.orm.TargetMembersHolder;

/**
 * @since 1.71.0
 * @author Razvan Popian
 */
public class AnnotationRowMapperConversionServiceTestCase {

	private final ResultSet rs = mock(ResultSet.class);
	private final ConversionService cs = mock(ConversionService.class);

	private final TypeDescriptor targetDesc = TargetMembersHolder.getInstance().getTargetMembers(CustomTypeBean.class)
			.getColumnMembers().get(0).getTypeDescriptor();

	private final AnnotationRowMapper<CustomTypeBean> mapper = new AnnotationRowMapper<>(CustomTypeBean.class);
	{
		mapper.setConversionService(cs);
	}
	
	@Test
	public void nonNullValue() throws SQLException {
		Object complexObject = new Object(); // some custom database type like JSON 
		when(rs.getInt(COL_PK)).thenReturn(11);
		when(rs.getObject(COL_CUSTOM_OBJECT)).thenReturn(complexObject);
		
		when(cs.canConvert(eq(TypeDescriptor.valueOf(complexObject.getClass())), eq(targetDesc)))
			.thenReturn(true);
		when(cs.convert(eq(complexObject), eq(TypeDescriptor.valueOf(complexObject.getClass())), eq(targetDesc)))
			.thenReturn("complexString");
		
		CustomTypeBean b = mapper.mapRow(rs, 0);
		
		assertEquals(11, b.id);
		assertEquals("complexString", b.complexString);
	}
	
	@Test
	public void nullValue() throws SQLException {
		when(rs.getInt(COL_PK)).thenReturn(11);
		when(rs.getObject(COL_CUSTOM_OBJECT)).thenReturn(null);
				
		CustomTypeBean b = mapper.mapRow(rs, 0);
		
		assertEquals(11, b.id);
		assertNull(b.complexString);
		
		verifyNoInteractions(cs);
	}

	
	
	private static class CustomTypeBean {
		
		@PkColumn(COL_PK)
		int id;
		
		@Column(value = COL_CUSTOM_OBJECT, sqlParam = @SqlParam("custom"))
		String complexString;
	}
}
