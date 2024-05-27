package com.asentinel.common.orm.converter.entity;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.converter.entity.IdToEntityConverterTestCase.TestEntityInt;
import com.asentinel.common.orm.converter.entity.IdToEntityConverterTestCase.TestEntityIntHolder;

public class EntityToIdConverterTestCase {

	EntityToIdConverter c = new EntityToIdConverter();
	DefaultConversionService conversionService = new DefaultConversionService();
	{
		conversionService.addConverter(c);
	}

	
	@Test
	public void testExactIdTypeMatchInt() {
		assertEquals(new Integer(10), conversionService.convert(new TestEntityInt(10), int.class));
	}

	@Test
	public void testExactIdTypeMatchIntObject() {
		assertEquals(new Integer(10), conversionService.convert(new TestEntityInt(10), Integer.class));
	}

	@Test
	public void testEntityToString() {
		assertEquals("10", conversionService.convert(new TestEntityInt(10), String.class));
	}
	
	@Test
	public void testNullToString() {
		assertEquals(null, conversionService.convert(null,
				new TypeDescriptor((Field) ReflectionUtils.findField(TestEntityIntHolder.class, "testEntityInt")),
				TypeDescriptor.valueOf(String.class))
		);
	}
	
	@Test
	public void testNullToInt() {
		assertEquals(null, conversionService.convert(null,
				new TypeDescriptor((Field) ReflectionUtils.findField(TestEntityIntHolder.class, "testEntityInt")),
				TypeDescriptor.valueOf(Integer.class))
		);
	}
	
	

	@Test(expected = ConverterNotFoundException.class)
	public void testFailure1() {
		conversionService.convert(new TestEntityInt(10), Long.class);
	}

	@Test(expected = ConverterNotFoundException.class)
	public void testFailure2() {
		conversionService.convert(new TestEntityInt(10), Number.class);
	}
	
}
