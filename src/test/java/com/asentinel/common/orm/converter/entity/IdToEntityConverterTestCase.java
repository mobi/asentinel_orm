package com.asentinel.common.orm.converter.entity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.Test;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.OrmOperations;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class IdToEntityConverterTestCase {

	OrmOperations ormOps = mock(OrmOperations.class);
	IdToEntityConverter c = new IdToEntityConverter(ormOps);
	DefaultConversionService conversionService = new DefaultConversionService();
	{
		conversionService.addConverter(c);
	}
	
	// int id tests
	@Test
	public void testExactIdTypeMatchInt() {
		when(ormOps.getProxy(TestEntityInt.class, 10)).thenReturn(new TestEntityInt(10));
		TestEntityInt e = conversionService.convert(10, TestEntityInt.class);
		assertNotNull(e);
		assertEquals(10, e.id);
	}
	
	@Test
	public void testExactIdTypeMatchIntObject() {
		when(ormOps.getProxy(TestEntityIntObject.class, 10)).thenReturn(new TestEntityIntObject(10));
		TestEntityIntObject e = conversionService.convert(10, TestEntityIntObject.class);
		assertNotNull(e);
		assertEquals(10l, e.id.longValue());
	}


	@Test
	public void testStringIdInt() {
		when(ormOps.getProxy(TestEntityInt.class, 10)).thenReturn(new TestEntityInt(10));
		TestEntityInt e = conversionService.convert("10", TestEntityInt.class);
		assertNotNull(e);
		assertEquals(10, e.id);
	}
	
	@Test
	public void testEmptyStringIdInt() {
		TestEntityInt e = conversionService.convert("", TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}

	@Test
	public void testNullIdInt() {
		// in this case the converter is not even called
		TestEntityInt e = conversionService.convert(null, TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testNullIdIntToProperty() {
		// in this case the converter is called
		Object e = conversionService.convert(null,
				TypeDescriptor.valueOf(Integer.class),
				new TypeDescriptor((Field) ReflectionUtils.findField(TestEntityIntHolder.class, "testEntityInt")));
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	
	@Test
	public void testZeroIdInt() {
		TestEntityInt e = conversionService.convert(0, TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testNegativeIdInt() {
		TestEntityInt e = conversionService.convert(-1, TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	
	@Test
	public void testStringZeroIdInt() {
		TestEntityInt e = conversionService.convert("0", TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}

	@Test
	public void testStringNegativeIdInt() {
		TestEntityInt e = conversionService.convert("-1", TestEntityInt.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testValidZeroIdInt() {
		when(ormOps.getProxy(TestEntityInt.class, 0)).thenReturn(new TestEntityInt(0));
		conversionService = new DefaultConversionService();
		conversionService.addConverter(new IdToEntityConverter(ormOps, false));
		TestEntityInt e = conversionService.convert(0, TestEntityInt.class);
		assertNotNull(e);
	}

	
	
	// long id 
	@Test
	public void testExactIdTypeMatchLong() {
		when(ormOps.getProxy(TestEntityLong.class, 10l)).thenReturn(new TestEntityLong(10l));
		TestEntityLong e = conversionService.convert(10l, TestEntityLong.class);
		assertNotNull(e);
		assertEquals(10, e.id);
	}
	
	@Test
	public void testExactIdTypeMatchLongObject() {
		when(ormOps.getProxy(TestEntityLongObject.class, 10l)).thenReturn(new TestEntityLongObject(10l));
		TestEntityLongObject e = conversionService.convert(10l, TestEntityLongObject.class);
		assertNotNull(e);
		assertEquals(10l, e.id.longValue());
	}
	

	@Test
	public void testStringIdLong() {
		when(ormOps.getProxy(TestEntityLong.class, 10l)).thenReturn(new TestEntityLong(10));
		TestEntityLong e = conversionService.convert("10", TestEntityLong.class);
		assertNotNull(e);
		assertEquals(10, e.id);
	}
	
	@Test
	public void testEmptyStringIdLong() {
		TestEntityLong e = conversionService.convert("", TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}

	@Test
	public void testNullIdLong() {
		// in this case the converter is not even called
		TestEntityLong e = conversionService.convert(null, TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testZeroIdLong() {
		TestEntityLong e = conversionService.convert(0l, TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testNegativeIdLong() {
		TestEntityLong e = conversionService.convert(-1l, TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	
	@Test
	public void testStringZeroIdLong() {
		TestEntityLong e = conversionService.convert("0", TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testStringNegativeIdLong() {
		TestEntityLong e = conversionService.convert("-1", TestEntityLong.class);
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	@Test
	public void testValidZeroIdLong() {
		when(ormOps.getProxy(TestEntityLong.class, 0l)).thenReturn(new TestEntityLong(0l));
		conversionService = new DefaultConversionService();
		conversionService.addConverter(new IdToEntityConverter(ormOps, false));
		TestEntityLong e = conversionService.convert(0l, TestEntityLong.class);
		assertNotNull(e);
	}
	
	
	

	// String id 
	@Test
	public void testExactIdTypeMatchString() {
		when(ormOps.getProxy(TestEntityString.class, "10")).thenReturn(new TestEntityString("10"));
		TestEntityString e = conversionService.convert("10", TestEntityString.class);
		assertNotNull(e);
		assertEquals("10", e.id);
	}

	@Test
	public void testExactIdTypeMatchStringEmptyString() {
		when(ormOps.getProxy(TestEntityString.class, "")).thenReturn(new TestEntityString(""));
		TestEntityString e = conversionService.convert("", TestEntityString.class);
		assertNotNull(e);
		assertEquals("", e.id);
	}
	
	@Test
	public void testNullIdStringToProperty() {
		// in this case the converter is called
		Object e = conversionService.convert(null,
				TypeDescriptor.valueOf(String.class),
				new TypeDescriptor((Field) ReflectionUtils.findField(TestEntityIntHolder.class, "testEntityInt")));
		verifyZeroInteractions(ormOps);
		assertNull(e);
	}
	
	
	
	// converter not found test
	
	@Test(expected = ConverterNotFoundException.class)
	public void testLongId() {
		conversionService.convert(new Long(10), TestEntityInt.class);
	}

	@Test(expected = ConverterNotFoundException.class)
	public void testSomeUnsupportedClass() {
		conversionService.convert(10, this.getClass());
	}
	
	@Test(expected = ConverterNotFoundException.class)
	public void testNoConverterForNumber() {
		conversionService.convert(10, TestEntityNumber.class);
	}
	
	
	// helper entity classes
	
	class TestEntityIntHolder {
		TestEntityInt testEntityInt;

		public TestEntityInt getTestEntityInt() {
			return testEntityInt;
		}

		public void setTestEntityInt(TestEntityInt testEntityInt) {
			this.testEntityInt = testEntityInt;
		}
	}
	
	@Table("Test")
	static class TestEntityInt {
		@PkColumn("id")
		int id;
		
		public TestEntityInt(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityInt [id=" + id + "]";
		}
	}
	
	@Table("Test")
	static class TestEntityIntObject {
		@PkColumn("id")
		Integer id;
		
		public TestEntityIntObject(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityIntObject [id=" + id + "]";
		}
	}

	
	@Table("Test")
	static class TestEntityLong {
		@PkColumn("id")
		long id;
		
		public TestEntityLong(long id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityLong [id=" + id + "]";
		}
	}
	
	@Table("Test")
	static class TestEntityLongObject {
		@PkColumn("id")
		Long id;
		
		public TestEntityLongObject(Long id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityLongObject [id=" + id + "]";
		}
	}
	

	@Table("Test")
	static class TestEntityString {
		@PkColumn("id")
		String id;
		
		public TestEntityString(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityString [id=" + id + "]";
		}
	}

	@Table("Test")
	static class TestEntityNumber {
		@PkColumn("id")
		Number id;
		
		public TestEntityNumber(Number id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "TestEntityNumber [id=" + id + "]";
		}
	}
	
}
