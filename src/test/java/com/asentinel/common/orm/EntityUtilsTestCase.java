package com.asentinel.common.orm;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.proxy.Proxy;
import com.asentinel.common.orm.proxy.ProxyFactorySupport;
import com.asentinel.common.orm.proxy.collection.CollectionProxyFactory;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

public class EntityUtilsTestCase {
	
	private static final Integer ENTITY_ID = Integer.valueOf(10);
	
	@Test
	public void testIsEntityClass() {
		assertTrue(EntityUtils.isEntityClass(TestEntity.class));
		assertTrue(EntityUtils.isEntityClass(TestEntityWithFieldReflection.class));
		assertTrue(EntityUtils.isEntityClass(TestEntityWithMethodReflection.class));
		assertFalse(EntityUtils.isEntityClass(Object.class));
		assertFalse(EntityUtils.isEntityClass(null));
	}

	@Test
	public void testGetNoEntity() {
		Object entity = new Object();
		try {
			EntityUtils.getEntityId(entity);
			fail("The entity is not a real entity, the test should fail.");
		} catch (IllegalArgumentException e) {
			
		}
	}

	@Test
	public void testSetNoEntity() {
		Object entity = new Object();
		try {
			EntityUtils.setEntityId(entity, ENTITY_ID);
			fail("The entity is not a real entity, the test should fail.");
		} catch (IllegalArgumentException e) {
			
		}
	}

	@Test
	public void testGetSetEntityIdWithEntityInterface() {
		TestEntity entity = new TestEntity();
		EntityUtils.setEntityId(entity, ENTITY_ID);
		assertEquals(ENTITY_ID, entity.getEntityId());
		assertEquals(ENTITY_ID, EntityUtils.getEntityId(entity));
	}

	@Test
	public void testGetSetEntityIdWithFieldReflection() {
		TestEntityWithFieldReflection entity = new TestEntityWithFieldReflection();
		EntityUtils.setEntityId(entity, ENTITY_ID);
		assertEquals(ENTITY_ID, entity.getEntityId());
		assertEquals(ENTITY_ID, EntityUtils.getEntityId(entity));
	}

	@Test
	public void testGetSetEntityIdWithMethodReflection() {
		TestEntityWithMethodReflection entity = new TestEntityWithMethodReflection();
		EntityUtils.setEntityId(entity, ENTITY_ID);
		assertEquals(ENTITY_ID, entity.getEntityId());
		assertEquals(ENTITY_ID, EntityUtils.getEntityId(entity));
	}
	
	@Test
	public void testEntityIdClassInt() {
		assertEquals(int.class, EntityUtils.getEntityIdClass(IntIdEntity.class));
	}

	@Test
	public void testEntityIdClassInteger() {
		assertEquals(Integer.class, EntityUtils.getEntityIdClass(IntegerIdEntity.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEntityIdClassNotEntity() {
		EntityUtils.getEntityIdClass(Object.class);
	}

	@Test
	public void testEntityIdClassLong() {
		assertEquals(long.class, EntityUtils.getEntityIdClass(LongIdEntity.class));
	}
	
	@Test
	public void testProxyNull() {
		assertFalse(EntityUtils.isProxy(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadedProxyNull() {
		EntityUtils.isLoadedProxy(null);
	}

	@Test
	public void testProxyYes() {
		assertTrue(EntityUtils.isProxy(new Proxy(){}));
	}
	
	@Test
	public void testProxyLoaded() {
		Object entity = ProxyFactory.getInstance().getProxyObjectFactory(ProxyTest.class).newObject();
		assertTrue(EntityUtils.isProxy(entity));
		assertTrue(EntityUtils.isLoadedProxy(entity)); // the loader field is null at this time
		
		Field loader = ProxyFactorySupport.findLoaderField(entity.getClass());
		Function<Object, Object> f = id -> new Object();
		ReflectionUtils.setField(loader, entity, f);
		assertFalse(EntityUtils.isLoadedProxy(entity)); // the loader field is set at this time
	}
	
	
	@Test
	public void testProxyNo() {
		assertFalse(EntityUtils.isProxy(new Object()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testLoadedProxyEx() {
		EntityUtils.isLoadedProxy(new Object());
	}
	
	@Test
	public void isProxyForCollection() {
		List<?> collectionProxy = CollectionProxyFactory.getInstance().getProxyObjectFactory(ArrayList.class).newObject();
		assertTrue(EntityUtils.isProxy(collectionProxy));
	}

	@Test
	public void forceLoad() {
		Object entity = ProxyFactory.getInstance().getProxyObjectFactory(ProxyTest.class).newObject();
		Field loader = ProxyFactorySupport.findLoaderField(entity.getClass());
		Function<Object, Object> f = id -> new ProxyTest();
		ReflectionUtils.setField(loader, entity, f);
		assertFalse(EntityUtils.isLoadedProxy(entity));
		
		EntityUtils.loadProxy(entity);
		assertTrue(EntityUtils.isLoadedProxy(entity));
		
	}

	// ------------------------------------------------------- //
	
	private static class TestEntity implements Entity {
		
		private Object entityId;

		@Override
		public Object getEntityId() {
			return entityId;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.entityId = entityId;
		}
	}
	
	private static class TestEntityWithFieldReflection {

		@PkColumn("entityId")
		private Object entityId;

		public Object getEntityId() {
			return entityId;
		}
	}
	

	private static class TestEntityWithMethodReflection {

		
		private Object entityId;

		
		public Object getEntityId() {
			return entityId;
		}

		@PkColumn("entityId")
		public void setEntityId(Object entityId) {
			this.entityId = entityId;
		}
	}
	
	private static class IntIdEntity {
		@PkColumn("id")
		private int id;
	}
	
	private static class IntegerIdEntity {
		private Integer id;

		@SuppressWarnings("unused")
		public Integer getId() {
			return id;
		}

		@PkColumn("id")
		public void setId(Integer id) {
			this.id = id;
		}
	}

	private static class LongIdEntity {
		@PkColumn("id")
		private long id;
	}

	@Table("ProxyTest")
	public static class ProxyTest {
		@PkColumn("id")
		private long id;
	}

}
