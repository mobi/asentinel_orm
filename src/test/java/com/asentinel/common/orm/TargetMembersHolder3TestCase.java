package com.asentinel.common.orm;

import static com.asentinel.common.orm.TargetMembersHolder.findGetterMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

/**
 * Tests the static utility method {@link TargetMembersHolder#findGetterMethod(Class, Method)}
 */
public class TargetMembersHolder3TestCase {
	
	@Test
	public void testOk() {
		Method s = ReflectionUtils.findMethod(Ok.class, "setVar", int.class);
		Method g = findGetterMethod(Ok.class, s);
		assertNotNull(g);
		assertEquals("getVar", g.getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongMissing() {
		Method s = ReflectionUtils.findMethod(WrongMissing.class, "setVar", int.class);
		findGetterMethod(WrongMissing.class, s);
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testWrongHasArgs() {
		Method s = ReflectionUtils.findMethod(WrongHasArgs.class, "setVar", int.class);
		findGetterMethod(WrongHasArgs.class, s);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongNoReturn() {
		Method s = ReflectionUtils.findMethod(WrongNoReturn.class, "setVar", int.class);
		findGetterMethod(WrongNoReturn.class, s);
	}
	
	@Test
	public void testExtOk() {
		Method s = ReflectionUtils.findMethod(ExtOk.class, "setVar", int.class);
		Method g = findGetterMethod(ExtOk.class, s);
		assertNotNull(g);
		assertEquals("getVar", g.getName());
		assertEquals(ExtOk.class, g.getDeclaringClass());
	}

	@Test
	public void testExtOk2() {
		Method s = ReflectionUtils.findMethod(ExtOk2.class, "setVar", int.class);
		Method g = findGetterMethod(ExtOk2.class, s);
		assertNotNull(g);
		assertEquals("getVar", g.getName());
		assertEquals(Ok.class, g.getDeclaringClass());
	}
	
	
	@Test
	public void testBooleanOk() {
		Method s = ReflectionUtils.findMethod(BooleanClass.class, "setFound", boolean.class);
		Method g = findGetterMethod(BooleanClass.class, s);
		assertNotNull(g);
		assertEquals("isFound", g.getName());
	}	
	
	@Test
	public void testBooleanGetClassOk() {
		Method s = ReflectionUtils.findMethod(BooleanGetClass.class, "setFound", boolean.class);
		Method g = findGetterMethod(BooleanGetClass.class, s);
		assertNotNull(g);
		assertEquals("getFound", g.getName());
	}
	
	@Test
	public void testBooleanGetObjectClassOk() {
		Method s = ReflectionUtils.findMethod(BooleanGetObjectClass.class, "setFound", boolean.class);
		Method g = findGetterMethod(BooleanGetObjectClass.class, s);
		assertNotNull(g);
		assertEquals("isFound", g.getName());
	}	
	
	@Test
	public void testBooleanIsAndGetClassOk() {
		Method s = ReflectionUtils.findMethod(BooleanIsAndGetClass.class, "setFound", boolean.class);
		Method g = findGetterMethod(BooleanIsAndGetClass.class, s);
		assertNotNull(g);
		// we make no guarantees if both the IS and the GET methods are present
		assertTrue("isFound".equals(g.getName())
				|| "getFound".equals(g.getName()));
	}		

	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSetter() {
		Method s = ReflectionUtils.findMethod(InvalidSetter.class, "setNumber");
		findGetterMethod(InvalidSetter.class, s);
	}
	
	
	static class Ok {
		
		public void setVar(int i) {
			
		}
		
		public int getVar() {
			return 0;
		}
	}
	
	static class WrongMissing {
		
		public void setVar(int i) {
			
		}

	}
	
	
	static class WrongHasArgs {
		
		public void setVar(int i) {
			
		}
		
		public int getVar(String s) {
			return 0;
		}
	}

	static class WrongMultiple {
		
		public void setVar(int i) {
			
		}
		
		public int getVar() {
			return 0;
		}
		
		public int getVar(String s) {
			return 0;
		}
		
	}

	static class WrongNoReturn {
		
		public void setVar(int i) {
			
		}
		
		public void getVar() {
			System.out.println(this.getClass());
		}
		
	}

	static class ExtOk extends Ok {

		@Override
		public int getVar() {
			System.out.println(this.getClass());
			return 0;
		}
	}
	
	static class ExtOk2 extends Ok {

	}
	
	private static class BooleanClass {

		@SuppressWarnings("unused")
		boolean isFound() {
			return true;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			
		}
	}
	
	private static class BooleanGetClass {

		@SuppressWarnings("unused")
		boolean getFound() {
			return true;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			
		}
	}	
	
	private static class BooleanIsAndGetClass {
	
		@SuppressWarnings("unused")
		boolean getFound() {
			return true;
		}
		
		@SuppressWarnings("unused")
		boolean isFound() {
			return true;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {

		}
	}	
	
	private static class BooleanGetObjectClass {

		@SuppressWarnings("unused")
		Boolean isFound() {
			return true;
		}

		@SuppressWarnings("unused")
		void setFound(boolean found) {
			
		}
	}		
	
	
	private static class InvalidSetter {
		
		@SuppressWarnings("unused")
		int getNumber() {
			return 0;
		}
		
		@SuppressWarnings("unused")
		void setNumber() {
			
		}
	}

}
