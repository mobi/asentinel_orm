package com.asentinel.common.orm.proxy.entity;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.mappers.PkColumn;

public class ProxyFactoryTestCase {
	
	ProxyFactory factory = ProxyFactory.getInstance();
	
	public static class Target1 implements Serializable {
		
		static final long serialVersionUID = 1L;
		
		@PkColumn("id")
		private int id;
	}
	
	public static class Target2 {
		
		private int id;

		int getId() {
			return id;
		}
		
		@PkColumn("id")
		void setId(int id) {
			this.id = id;
		}
		
	}
	
	@Test
	public void testCache() {
		Class<?> clazz1 = factory.getProxyObjectFactory(Target1.class).getType();
		Class<?> clazz2 = factory.getProxyObjectFactory(Target1.class).getType();
		Class<?> clazz3 = factory.getProxyObjectFactory(Target2.class).getType();
		Class<?> clazz4 = factory.getProxyObjectFactory(Target2.class).getType();

		assertSame(clazz1, clazz2);
		assertSame(clazz3, clazz4);
		assertNotSame(clazz1, clazz3);
	}

	@Test(expected = IllegalStateException.class)
	public void testProxyingNotSupported () {
		factory.getProxyObjectFactory(Object.class);
	}
	
	@Test
	public void testObjectFactory() {
		Target1 t11 = factory.newProxy(Target1.class, (id) -> null);
		Target1 t12 = factory.newProxy(Target1.class, (id) -> null);
		Target2 t21 = factory.newProxy(Target2.class, (id) -> null);
		Target2 t22 = factory.newProxy(Target2.class, (id) -> null);
		
		assertNotSame(t11, t12);
		assertNotSame(t21, t22);
		assertNotSame(t11, t21);


		assertSame(t11.getClass(), t12.getClass());
		assertSame(t21.getClass(), t22.getClass());
		assertNotSame(t11.getClass(), t21.getClass());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testProxyForProxy() {
		Target1 t1 = factory.newProxy(Target1.class, (id) -> null);
		factory.newProxy(t1.getClass(), (id) -> null);
	}


	@Test
	public void testTarget1HasSerialVersionUid() {
		Class<?> clazz = factory.getProxyObjectFactory(Target1.class).getType();
		Field field = ReflectionUtils.findField(clazz, "serialVersionUID");
		assertNotNull(field);
		assertEquals(clazz, field.getDeclaringClass());
		Object uid = ReflectionTestUtils.getField(clazz, "serialVersionUID");
		assertEquals(Target1.serialVersionUID, uid);
	}

	@Test
	public void testTarget2HasNoSerialVersionUid() {
		Class<?> clazz = factory.getProxyObjectFactory(Target2.class).getType();
		Field field = ReflectionUtils.findField(clazz, "serialVersionUID");
		assertNull(field);
	}
	
}
