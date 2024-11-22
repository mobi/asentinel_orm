package com.asentinel.common.orm.proxy.entity;

import com.asentinel.common.orm.Entity;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.function.Function;

import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@RunWith(Parameterized.class)
public class ProxyTestCase {
	
	static final String LOADER_FIELD_NAME = (String) ReflectionTestUtils.getField(ProxyFactorySupport.class, "LOADER_FIELD_NAME"); 
	
	static final int ID = 17;
	static final String NAME = "Razvan";
	static final int FINAL_FIELD_ORG_VALUE = 10;
	static final int FINAL_FIELD_NEW_VALUE = 1000;

	ProxyFactory pcf = ProxyFactory.getInstance();
	
	static boolean isLoaded(Object proxy) {
		return ReflectionTestUtils.getField(proxy, LOADER_FIELD_NAME) == null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Object createProxy() {
		Object e = pcf.newProxy(targetClass, new Function() {
			@Override
			public Object apply(Object id) {
				try {
					loadCalled = true;
					Object target;
					if (Target_AnnotatedMethods.class == targetClass) {
						target = new Target_AnnotatedMethods(FINAL_FIELD_NEW_VALUE);
					} else if (Target_AnnotatedFields.class == targetClass) {
						target = new Target_AnnotatedFields(FINAL_FIELD_NEW_VALUE);
					} else if (Target_EntityImpl.class == targetClass) {
						target = new Target_EntityImpl(FINAL_FIELD_NEW_VALUE);
					} else {
						target = targetClass.getDeclaredConstructor().newInstance();
					}
					setId(target, id);
					invokeSetterMethod(target, "name", NAME);
					return target;
				} catch (Exception e) {
					fail("Failed to create proxy.");
					return null; // to keep the compiler happy
				}
			}
		});
		assertTrue(EntityUtils.isProxy(e));
		assertFalse(EntityUtils.isLoadedProxy(e));
		return e;
	}
	
	private static Object getId(Object proxy) {
		if (proxy instanceof Entity) {
			return ((Entity) proxy).getEntityId();
		} else {
			return invokeGetterMethod(proxy, "id");
		}
	}
	
	private static void setId(Object proxy, Object id) {
		if (proxy instanceof Entity) {
			((Entity) proxy).setEntityId(id);
		} else {
			invokeSetterMethod(proxy, "id", id);
		}
	}

	private static int getFinalField(Object instance) {
		return (int) ReflectionTestUtils.getField(instance, "finalField");
	}

	private final Class<?> targetClass;
	private boolean loadCalled = false;
	
	public ProxyTestCase(Class<?> targetClass) {
		this.targetClass = targetClass;
	}
	
	@Parameters(name = "class {0}")
	public static Object[] getParameters() {
		return new Class[] {
			Target_AnnotatedFields.class,
			Target_AnnotatedMethods.class,
			Target_EntityImpl.class
		};
	}
	
	@Test
	public void toString_Does_Not_Trigger_Load() {
		Object proxy = createProxy();
		
		// check toString
		String s = proxy.toString();
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));
		assertEquals(String.format(ToStringInterceptor.PROXY, proxy.getClass().getSuperclass().getName(), ID), s);
	}
	
	@Test
	public void toString_Renders_Ok_After_Load() {
		Object proxy = createProxy();
		
		// trigger load
		assertEquals(NAME, invokeGetterMethod(proxy, "name"));
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		
		// check toString
		String s = proxy.toString();
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertNotEquals(String.format(ToStringInterceptor.PROXY, proxy.getClass().getSuperclass().getName(), ID), s);
	}

	
	@Test
	public void entityId_Access_Does_Not_Trigger_Load() {
		Object proxy = createProxy();
		
		// check get/set id
		assertEquals(ID, getId(proxy));
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));

		setId(proxy, 10);
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));

		assertEquals(10, EntityUtils.getEntityId(proxy));
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));

		EntityUtils.setEntityId(proxy, 100);
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));
	}
	
	@Test
	public void field_Get_Does_Trigger_Load() {
		Object proxy = createProxy();
		
		// check get name
		assertEquals(NAME, invokeGetterMethod(proxy, "name"));
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertEquals(ID, getId(proxy));
		assertEquals(NAME, invokeGetterMethod(proxy, "name"));
		assertEquals(FINAL_FIELD_NEW_VALUE, getFinalField(proxy));
	}

	@Test
	public void field_Set_Does_Trigger_Load() {
		Object proxy = createProxy();

		// check get/set name
		invokeSetterMethod(proxy, "name", "Abc");
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertEquals(ID, getId(proxy));
		assertEquals("Abc", invokeGetterMethod(proxy, "name"));
		assertEquals(FINAL_FIELD_NEW_VALUE, getFinalField(proxy));
	}

	@Test
	public void arbitrary_Method_Triggers_Load() {
		Object proxy = createProxy();
		
		invokeMethod(proxy, "aMethod");
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertEquals(ID, getId(proxy));
		assertEquals(NAME, invokeGetterMethod(proxy, "name"));
		assertEquals(FINAL_FIELD_NEW_VALUE, getFinalField(proxy));
	}
	
	// as the name suggests this test demonstrates that final methods
	// are not intercepted and therefore they do not trigger the load
	@Test
	public void final_Method_Does_Not_Trigger_Load() {
		Object proxy = createProxy();
		
		invokeMethod(proxy, "aFinalMethod");
		assertFalse(isLoaded(proxy));
		assertFalse(EntityUtils.isLoadedProxy(proxy));
	}

	@Test
	public void super_Method_Does_Trigger_Load() {
		Object proxy = createProxy();
		
		proxy.hashCode();
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertEquals(ID, getId(proxy));
		assertEquals(NAME, invokeGetterMethod(proxy, "name"));
		assertEquals(FINAL_FIELD_NEW_VALUE, getFinalField(proxy));
	}
	
	@Test
	public void lazy_Load_Happens_Exactly_Once() {
		Object proxy = createProxy();
		
		// first invoke triggers the load
		invokeMethod(proxy, "aMethod");
		assertTrue(isLoaded(proxy));
		assertTrue(EntityUtils.isLoadedProxy(proxy));
		assertTrue(loadCalled);
		assertEquals(FINAL_FIELD_NEW_VALUE, getFinalField(proxy));

		loadCalled = false;
		
		// second invoke should not trigger another load
		invokeMethod(proxy, "aMethod");
		assertTrue(isLoaded(proxy));
		assertFalse(loadCalled);
	}
	
	public static class Target_AnnotatedFields {
		@SuppressWarnings("unused")
		private final int finalField;
		
		@PkColumn("id")
		private int id = ID;
		
		@Column("name")
		private String name;
		
		
		public Target_AnnotatedFields() {
			this.finalField = FINAL_FIELD_ORG_VALUE;
		}

		public Target_AnnotatedFields(int finalField) {
			this.finalField = finalField;
		}
		
		
		public int getId() {
			return id;
		}

		
		public void setId(int id) {
			this.id = id;
		}

		
		public String getName() {
			return name;
		}

		
		public void setName(String name) {
			this.name = name;
		}
		
		public void aMethod() {
			
		}
		
		public final void aFinalMethod() {
			
		}
		
	}
	
	public static class Target_AnnotatedMethods {
		
		@SuppressWarnings("unused")
		private final int finalField;
		
		private int id = ID;

		private String name;
		
		public Target_AnnotatedMethods() {
			this.finalField = FINAL_FIELD_ORG_VALUE;
		}

		public Target_AnnotatedMethods(int finalField) {
			this.finalField = finalField;
		}
		
		
		public int getId() {
			return id;
		}

		@PkColumn("id")
		public void setId(int id) {
			this.id = id;
		}

		
		public String getName() {
			return name;
		}

		@Column("name")
		public void setName(String name) {
			this.name = name;
		}
		
		public void aMethod() {
			
		}
		
		public final void aFinalMethod() {
			
		}
		
	}
	
	public static class Target_EntityImpl implements Entity {
		@SuppressWarnings("unused")
		private final int finalField;

		private Object id = ID;

		private String name;
		
		public Target_EntityImpl() {
			this.finalField = FINAL_FIELD_ORG_VALUE;
		}

		public Target_EntityImpl(int finalField) {
			this.finalField = finalField;
		}
		

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.id = entityId;
			
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void aMethod() {
			
		}

		public final void aFinalMethod() {
			
		}
	}
}
