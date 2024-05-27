package com.asentinel.common.text;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class ConverterTestCase {

	private void convert(Converter converter, Object[] objects, NumberFormat nf, NumberFormat nfInt, DateFormat df1) throws ConversionException{
		for (Object object:objects){
			Object result;
			if (object instanceof Integer){
				result = converter.convertToInteger(nfInt.format(object), null);
			} else if (object instanceof Long){
				result = converter.convertToLong(nfInt.format(object), null);
			} else if (object instanceof Double){
				result = converter.convertToDouble(nf.format(object), null);
			} else if (object instanceof String){
				result = converter.convertToString((String)object, null);
			} else if (object instanceof Date){
				result = converter.convertToDate(df1.format(object), null);
			} else if (object instanceof Boolean){
				result = converter.convertToBoolean(object.toString(), null);
			} else {
				throw new RuntimeException("Invalid class found."); 
			}
			assertEquals(object, result);
		}
	}
	
	@Test
	public void testStandardConverterEN_US() throws ConversionException, ParseException{
		System.out.println("ConverterTestCase#testStandardConverterEN_US start");
		Converter converter = new StandardConverter();
		
		NumberFormat nf;
		NumberFormat nfInt;
		nf = NumberFormat.getInstance(Locale.US);
		nfInt = NumberFormat.getInstance(Locale.US);
		nfInt.setMaximumFractionDigits(0);
		nfInt.setMinimumFractionDigits(0);
		
		DateFormat df1 = new SimpleDateFormat("MM/dd/yyyy");
		
		Object[] objects = new Object[]{
								1123,
								1123l,
								1123.12d,
								"test",
								df1.parse(df1.format(new Date())),
								Boolean.TRUE
		};
		
		convert(converter, objects, nf, nfInt, df1);
		System.out.println("ConverterTestCase#testStandardConverterEN_US end");		
	}
	
	@Test
	public void testStandardConverterRO() throws ConversionException, ParseException{
		System.out.println("ConverterTestCase#testStandardConverterRO start");
		Locale locale = new Locale("ro");
		Converter converter = new StandardConverter(locale);
		
		NumberFormat nf;
		NumberFormat nfInt;
		nf = NumberFormat.getInstance(locale);
		nfInt = NumberFormat.getInstance(locale);
		nfInt.setMaximumFractionDigits(0);
		nfInt.setMinimumFractionDigits(0);
		
		DateFormat df1 = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		
		Object[] objects = new Object[]{
								1123,
								1123l,
								1123.12d,
								"test",
								df1.parse(df1.format(new Date())),
								Boolean.TRUE
		};
		
		convert(converter, objects, nf, nfInt, df1);
		System.out.println("ConverterTestCase#testStandardConverterRO end");
	}

	@Test
	public void testStandardConverterToDate_WithConversionService() throws ConversionException, ParseException {
		ConversionService conversionService = Mockito.mock(ConversionService.class);
		Date date = new Date();
		when(conversionService.convert(anyString(), eq(Date.class)))
				.thenReturn(date);
		Converter converter = new StandardConverter(conversionService, Locale.ENGLISH);

		assertEquals(date, converter.convertToDate("10/11/2022", 0));
	}

	@Test
	public void testStandardConverterToDate_WithConversionService_Exception() {
		ConversionService conversionService = Mockito.mock(ConversionService.class);
		when(conversionService.convert(anyString(), eq(Date.class)))
				.thenThrow(new org.springframework.core.convert.ConversionException("fail") {
					private static final long serialVersionUID = 1L;
				});
		Converter converter = new StandardConverter(conversionService, Locale.ENGLISH);

		Assert.assertThrows(ConversionException.class, () -> converter.convertToDate("10/11/2022", 0));
	}

	@Test
	public void testConversionException(){
		System.out.println("ConverterTestCase#testConversionException start");
		Locale locale = new Locale("ro");
		Converter converter = new StandardConverter(locale);
		
		String s = "test_error";
		
		try{
			converter.convertToInteger(s, null);
			assertTrue(false);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}

		try{
			converter.convertToLong(s, null);
			assertTrue(false);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}

		try{
			converter.convertToDouble(s, null);
			assertTrue(false);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}

		try{
			converter.convertToDate(s, null);
			assertTrue(false);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}
		
		try{
			converter.convertToString(s, null);
			assertTrue(true);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}
		
		try{
			Boolean b = converter.convertToBoolean(s, null);
			assertEquals(Boolean.FALSE, b);
		} catch (ConversionException e){
			assertTrue(e.isParseError());
		}
		
		System.out.println("ConverterTestCase#testConversionException stop");		
	}
	
	
	private void testNumericConversionErrors(String intValue, String doubleValue, Converter converter){
		try{
			converter.convertToInteger(intValue,  null);
			assertTrue(false);
		} catch (ConversionException e){
		}
		
		try{
			converter.convertToLong(intValue,  null);
			assertTrue(false);
		} catch (ConversionException e){
		}
		
		try{
			converter.convertToDouble(doubleValue,  null);
			assertTrue(false);
		} catch (ConversionException e){
		}
		
	}
	
	@Test
	public void testNumericConversionErrors(){
		System.out.println("ConverterTestCase#testNumericConversionErrors start");
		testNumericConversionErrors("123abc", "123.45abc", new StandardConverter());
		testNumericConversionErrors("123abc", "123,45abc", new StandardConverter(new Locale("ro")));
		System.out.println("ConverterTestCase#testNumericConversionErrors stop");
	}
	
	@Test
	public void testStandardConverterNullValues() {
		System.out.println("ConverterTestCase#testStandardConverterNullValues start");
		Converter converter = new StandardConverter();
		try {
			assertNull(converter.convertToString(null, 0));
			assertNull(converter.convertToDouble(null, 0));
			assertNull(converter.convertToInteger(null, 0));
			assertNull(converter.convertToLong(null, 0));
			assertNull(converter.convertToDate(null, 0));
			assertNull(converter.convertToBoolean(null, 0));
		} catch (Exception e) {
			assertFalse("Should not throw exception.", true);
		}
		System.out.println("ConverterTestCase#testStandardConverterNullValues stop");
	}
	
	@Test(expected = IllegalStateException.class)
	public void testStandardConverterNoConversionService() {
		Converter converter = new StandardConverter();
		try {
			converter.convertToEntity("1", null, TEntity.class);
		} catch (ConversionException ce) {
			
		}
	}
	
	@Test
	public void testStandardConverterNullEntity() {
		ConversionService conversionService = Mockito.mock(ConversionService.class);
		Converter converter = new StandardConverter(conversionService, null);
		try {
			assertNull(converter.convertToEntity(null, null, TEntity.class));
		} catch (ConversionException ce) {
			
		}
	}
	
	@Test
	public void testStandardConverterEntity() {
		ConversionService conversionService = Mockito.mock(ConversionService.class);
		when(conversionService.convert(eq("1"), eq(TypeDescriptor.valueOf(String.class)), eq(TypeDescriptor.valueOf(TEntity.class))))
			.thenReturn(new TEntity());
		Converter converter = new StandardConverter(conversionService, null);
		
		try {
			TEntity entity = converter.convertToEntity("1", null, TEntity.class);
			assertNotNull(entity);
		} catch (ConversionException ce) {
			
		}
	}
	
	@Test(expected = ConversionException.class)
	public void testStandardConverterEntity_throwException() throws ConversionException {
		org.springframework.core.convert.ConversionException ex 
		= new org.springframework.core.convert.ConversionException("failed to convert") {
			private static final long serialVersionUID = 1L; 
		};
		ConversionService conversionService = Mockito.mock(ConversionService.class);
		when(conversionService.convert(eq("1"), eq(TypeDescriptor.valueOf(String.class)), eq(TypeDescriptor.valueOf(TEntity.class))))
			.thenThrow(ex);
		Converter converter = new StandardConverter(conversionService, null);
		converter.convertToEntity("1", null, TEntity.class);
	}
	
	
	class TEntity {
		private int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
	}
}
