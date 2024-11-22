package com.asentinel.common.text;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConverterTimeConversionTestCase {
	
	private void convert(Converter converter, Object[] objects, DateTimeFormatter dtf) throws ConversionException{
		for (Object object:objects){
			Object result;
			if (object instanceof LocalTime){
				result = converter.convertToTime(((LocalTime) object).format(dtf), null);
			} else {
				throw new RuntimeException("Invalid class found."); 
			}
			assertEquals(object, result);
		}
	}
	
	@Test
	public void testStandardConverterEN_US() throws ConversionException {
		System.out.println("ConverterTestCase#testStandardConverterEN_US start");
		Converter converter = new StandardConverter();
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm a");
		
		Object[] objects = new Object[]{
								LocalTime.of(13, 30),
								LocalTime.of(10, 30),
								LocalTime.of(00, 00),
								LocalTime.of(12,00)
		};
		
		convert(converter, objects, dtf);
		System.out.println("ConverterTestCase#testStandardConverterEN_US end");		
	}
	
	@Test
	public void testStandardConverterRO() throws ConversionException {
		System.out.println("ConverterTestCase#testStandardConverterRO start");
		Locale locale = new Locale("ro");
		Converter converter = new StandardConverter(locale);
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm a");
		
		Object[] objects = new Object[]{
								LocalTime.of(13, 30),
								LocalTime.of(10, 30),
								LocalTime.of(00, 00),
								LocalTime.of(12,00)
		};
		
		convert(converter, objects, dtf);
		System.out.println("ConverterTestCase#testStandardConverterRO end");
	}
	
	@Test
	public void testConversionException(){
		System.out.println("ConverterTestCase#testConversionException start");
		Locale locale = new Locale("ro");
		Converter converter = new StandardConverter(locale);
		
		String value = "Not a TIME value !";
		try{
			converter.convertToTime(value, null);
            fail();
		} catch (ConversionException e){
			assertTrue(e.getCause() instanceof DateTimeParseException);
		}
		
		System.out.println("ConverterTestCase#testConversionException stop");		
	}
}
