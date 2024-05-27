package com.asentinel.common.orm.proxy.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

public class CollectionProxyFactoryTestCase {
	
	CollectionProxyFactory factory = CollectionProxyFactory.getInstance();
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidClass() {
		factory.getProxyObjectFactory(Object.class).getType();
	}


	@Test
	public void testCache() {
		Class<?> clazz1 = factory.getProxyObjectFactory(ArrayList.class).getType();
		Class<?> clazz2 = factory.getProxyObjectFactory(ArrayList.class).getType();
		Class<?> clazz3 = factory.getProxyObjectFactory(HashMap.class).getType();
		Class<?> clazz4 = factory.getProxyObjectFactory(HashMap.class).getType();

		assertSame(clazz1, clazz2);
		assertSame(clazz3, clazz4);
		assertNotSame(clazz1, clazz3);
	}

	@Test
	public void testObjectFactory() {
		List<?> t11 = factory.newProxy(ArrayList.class, (id) -> null, 1);
		List<?> t12 = factory.newProxy(ArrayList.class, (id) -> null, 2);
		Map<?, ?> t21 = factory.newProxy(HashMap.class, (id) -> null, 3);
		Map<?, ?> t22 = factory.newProxy(HashMap.class, (id) -> null, 4);
		
		assertNotSame(t11, t12);
		assertNotSame(t21, t22);
		assertNotSame(t11, t21);


		assertSame(t11.getClass(), t12.getClass());
		assertSame(t21.getClass(), t22.getClass());
		assertNotSame(t11.getClass(), t21.getClass());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testProxyForProxy() {
		ArrayList<?> l1 = factory.newProxy(ArrayList.class, (id) -> null, 1);
		factory.newProxy(l1.getClass(), (id) -> null, 1);
	}


	@Test
	public void testTarget1HasSerialVersionUid() {
		Class<?> clazz = factory.getProxyObjectFactory(ArrayList.class).getType();
		Field field = ReflectionUtils.findField(clazz, "serialVersionUID");
		assertNotNull(field);
		assertEquals(clazz, field.getDeclaringClass());
		Object uid = ReflectionTestUtils.getField(clazz, "serialVersionUID");
		assertEquals(8683452581122892189L, uid);
	}

}
