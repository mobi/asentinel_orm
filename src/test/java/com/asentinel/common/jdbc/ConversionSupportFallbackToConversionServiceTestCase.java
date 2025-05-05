package com.asentinel.common.jdbc;

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

/**
 * @since 1.71.0
 * @author Razvan Popian
 */
public class ConversionSupportFallbackToConversionServiceTestCase {
	private final ResultSet rs = mock(ResultSet.class);
	private final ConversionService cs = mock(ConversionService.class);
	
	private final ConversionSupport support = new ConversionSupport() {};
	
	
	@Test
	public void conversionServiceSetNonNullValue() throws SQLException {
		support.setConversionService(cs);
		
		Object sourceCustomObject = new Object();
		TestTargetClass targetCustomObject = new TestTargetClass();
		when(rs.getObject("testColumn")).thenReturn(sourceCustomObject);
		
		when(cs.canConvert(eq(TypeDescriptor.valueOf(sourceCustomObject.getClass())), eq(TypeDescriptor.valueOf(TestTargetClass.class))))
			.thenReturn(true);
		when(cs.convert(eq(sourceCustomObject), eq(TypeDescriptor.valueOf(sourceCustomObject.getClass())), eq(TypeDescriptor.valueOf(TestTargetClass.class))))
			.thenReturn(targetCustomObject);
		
		TestTargetClass value = (TestTargetClass) support.getValue(new Object(), TypeDescriptor.valueOf(TestTargetClass.class), rs, new ColumnMetadata("testColumn"));
		assertEquals(targetCustomObject, value);
	}

	@Test
	public void conversionServiceSetNullValue() throws SQLException {
		support.setConversionService(cs);
		
		when(rs.getObject("testColumn")).thenReturn(null);
		
		TestTargetClass value = (TestTargetClass) support.getValue(new Object(), TypeDescriptor.valueOf(TestTargetClass.class), rs, new ColumnMetadata("testColumn"));
		assertNull(value);
		
		verifyNoInteractions(cs);
	}

	
	@Test(expected = SQLException.class)
	public void conversionServiceNotSet() throws SQLException {
		Object sourceCustomObject = new Object();
		when(rs.getObject("testColumn")).thenReturn(sourceCustomObject);
		
		support.getValue(new Object(), TypeDescriptor.valueOf(TestTargetClass.class), rs, new ColumnMetadata("testColumn"));
	}

	
	private static class TestTargetClass {
		
	}
}
