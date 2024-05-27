package com.asentinel.common.orm;

import static com.asentinel.common.orm.TargetMembersHolder.findGetterAndSetterMethods;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class TargetMembersHolderFindGetterSetterTestCase {

	@Test
	public void testSimple() {
		Field f = ReflectionUtils.findField(Simple.class, "id");
		Method[] ms = findGetterAndSetterMethods(Simple.class, f);
		assertEquals("getId", ms[0].getName());
		assertEquals("setId", ms[1].getName());
	}

	@Test
	public void testExtSimple() {
		Field f = ReflectionUtils.findField(ExtSimple.class, "id");
		Method[] ms = findGetterAndSetterMethods(ExtSimple.class, f);
		assertEquals("getId", ms[0].getName());
		assertEquals("setId", ms[1].getName());
	}

	@Test
	public void testExtSimpleWithOverride() {
		Field f = ReflectionUtils.findField(ExtSimpleWithOverride.class, "id");
		Method[] ms = findGetterAndSetterMethods(ExtSimpleWithOverride.class, f);
		assertEquals("getId", ms[0].getName());
		assertEquals("setId", ms[1].getName());
	}

	@Test
	public void testBooleanClass() {
		Field f = ReflectionUtils.findField(BooleanClass.class, "found");
		Method[] ms = findGetterAndSetterMethods(BooleanClass.class, f);
		assertEquals("isFound", ms[0].getName());
		assertEquals("setFound", ms[1].getName());
	}
	
	@Test
	public void testBooleanGetClass() {
		Field f = ReflectionUtils.findField(BooleanGetClass.class, "found");
		Method[] ms = findGetterAndSetterMethods(BooleanGetClass.class, f);
		assertEquals("getFound", ms[0].getName());
		assertEquals("setFound", ms[1].getName());
	}	

	@Test
	public void testBooleanIsAndGetClass() {
		Field f = ReflectionUtils.findField(BooleanIsAndGetClass.class, "found");
		Method[] ms = findGetterAndSetterMethods(BooleanIsAndGetClass.class, f);
		// we make no guarantees if both the IS and the GET methods are present
		assertTrue("isFound".equals(ms[0].getName())
				|| "getFound".equals(ms[0].getName()));
		assertEquals("setFound", ms[1].getName());
	}
	
	
	@Test
	public void testBooleanObjectClass() {
		Field f = ReflectionUtils.findField(BooleanObjectClass.class, "found");
		Method[] ms = findGetterAndSetterMethods(BooleanObjectClass.class, f);
		assertEquals("isFound", ms[0].getName());
		assertEquals("setFound", ms[1].getName());
	}	
	
	
	@Test
	public void testGetterNotFound() {
		Field f = ReflectionUtils.findField(GetterNotFound.class, "id");
		Method[] ms = findGetterAndSetterMethods(GetterNotFound.class, f);
		assertEquals(null, ms[0]);
		assertEquals("setId", ms[1].getName());
	}

	@Test
	public void testSetterNotFound() {
		Field f = ReflectionUtils.findField(SetterNotFound.class, "id");
		Method[] ms = findGetterAndSetterMethods(SetterNotFound.class, f);
		assertEquals("getId", ms[0].getName());
		assertEquals(null, ms[1]);
	}
	
	@Test
	public void testMostSpecific() {
		Field f = ReflectionUtils.findField(MostSpecific.class, "number");
		Method[] ms = findGetterAndSetterMethods(MostSpecific.class, f);
		assertEquals("getNumber", ms[0].getName());
		assertEquals(Integer.class, ms[0].getReturnType());
		assertEquals("setNumber", ms[1].getName());
		assertEquals(Integer.class, ms[1].getParameters()[0].getType());
	}
	
	
	
	private static class Simple {
		int id;

		@SuppressWarnings("unused")
		int getId() {
			return id;
		}

		@SuppressWarnings("unused")
		void setId(int id) {
			this.id = id;
		}
	}
	
	private static class ExtSimple extends Simple {
		@Override
		void setId(int id) {
			this.id = id;
		}
		
	}

	private static class ExtSimpleWithOverride extends ExtSimple {

	}
	
	private static class BooleanClass {
		boolean found;

		@SuppressWarnings("unused")
		boolean isFound() {
			return found;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			this.found = found;
		}
	}
	
	private static class BooleanGetClass {
		boolean found;

		@SuppressWarnings("unused")
		boolean getFound() {
			return found;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			this.found = found;
		}
	}	

	private static class BooleanIsAndGetClass {
		boolean found;

		@SuppressWarnings("unused")
		boolean getFound() {
			return found;
		}
		
		@SuppressWarnings("unused")
		boolean isFound() {
			return found;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			this.found = found;
		}
	}
	
	
	private static class BooleanObjectClass {
		Boolean found;

		@SuppressWarnings("unused")
		boolean isFound() {
			return found;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			this.found = found;
		}
	}	
	
	
	private static class GetterNotFound {
		@SuppressWarnings("unused")
		int id;

		@SuppressWarnings("unused")
		void setId(int id) {
			this.id = id;
		}
		
	}

	private static class SetterNotFound {
		int id;

		@SuppressWarnings("unused")
		int getId() {
			return id;
		}
		
	}
	
	private static class MostSpecificSuper {
		Integer number;

		@SuppressWarnings("unused")
		Number getNumber() {
			return number;
		}
		
		@SuppressWarnings("unused")
		void setNumber(Number number) {
			this.number = (Integer) number;
		}
	}

	private static class MostSpecific extends MostSpecificSuper {

		@SuppressWarnings("unused")
		@Override
		Integer getNumber() {
			return number;
		}
		
		@SuppressWarnings("unused")
		void setNumber(Integer number) {
			this.number = number;
		}		
	}

}
