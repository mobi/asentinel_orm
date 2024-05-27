package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

public class EntityBuilderWithOneChildProxyAndAnotherEntityTestCase {
	private ResultSet rs = mock(ResultSet.class);
	
	private Node<EntityDescriptor> rootNode = new SimpleNode<>();
	private Node<EntityDescriptor> child1Node = new SimpleNode<>();
	private Node<EntityDescriptor> child2Node = new SimpleNode<>();
	
	{
		EntityDescriptor rootEd = new EntityDescriptor(Root.class, 
				(rs, n) -> {
					return rs.getObject("id");
				},
				(rs, n) -> {
					Root r = new Root();
					return r;
				}, "rootMapper");
		
		EntityDescriptor child1Ed = new EntityDescriptor(Child.class, 
				(rs, n) -> {
					return rs.getObject("child1Id");
				},
				(rs, n) -> {
					return ProxyFactory.getInstance().newProxy(Child.class, (id) -> {
						assertEquals(10, id);
						return new Child("child1");
					});
				}, "child1Mapper",
				ReflectionUtils.findField(Root.class, "child1"));
		
		EntityDescriptor child2Ed = new EntityDescriptor(Child.class, 
				(rs, n) -> {
					return rs.getObject("child2Id");
				},
				(rs, n) -> {
						return new Child(rs.getString("description2"));
				}, "child2Mapper",
				ReflectionUtils.findField(Root.class, "child2"));		
		
		rootNode.setValue(rootEd);
		child1Node.setValue(child1Ed);
		child2Node.setValue(child2Ed);
	}
	
	/**
	 * This test processes the following resultset:
	 * 
	 * <pre>
	 * 	id			child1Id		child2Id		description2
	 * 	1			10				10				child
	 * </pre>
	 * 
	 * Because of how the EntityDescriptor tree was created the child with id 10 is created as a proxy (child1 is processed first),
	 * but because we encounter the same child in the resultset (child 2) the proxy will be populated with the data 
	 * extracted from the resultset.
	 */
	@Test
	public void proxyCreatedButEventuallyLoadedFromTheResultset() throws SQLException {
		when(rs.next()).thenReturn(true, false);
		
		when(rs.getObject("id")).thenReturn(1);
		
		when(rs.getObject("child1Id")).thenReturn(10);
		
		when(rs.getObject("child2Id")).thenReturn(10);
		
		when(rs.getString("description2")).thenReturn("child");

		rootNode.addChild(child1Node).addChild(child2Node);
		EntityBuilder<Root> eb = new EntityBuilder<>(rootNode);
		ResultSetUtils.processResultSet(rs, eb);
		
		List<Root> entities = eb.getEntityList();
		assertEquals(1, entities.size());
		
		Root root = entities.get(0);
		assertSame(root.getChild1(), root.getChild2());
		Child child = root.getChild1();

		// these assertions must be made before any methods are called on the child
		assertTrue(EntityUtils.isProxy(child));
		assertTrue(EntityUtils.isLoadedProxy(child));
		
		assertEquals(10, child.getEntityId());
		assertEquals("child", child.getDescription());
	}

	/**
	 * This test processes the following resultset:
	 * 
	 * <pre>
	 * 	id			child1Id		child2Id		description2
	 * 	1			10				10				child
	 * </pre>
	 * 
	 * Because of how the EntityDescriptor tree was created the child with id 10 is created as a real entity (child2 is processed first),
	 * when we encounter child1 we are reusing the already created entity.
	 */
	@Test
	public void realEntityCreated() throws SQLException {
		when(rs.next()).thenReturn(true, false);
		
		when(rs.getObject("id")).thenReturn(1);
		
		when(rs.getObject("child1Id")).thenReturn(10);
		
		when(rs.getObject("child2Id")).thenReturn(10);
		
		when(rs.getString("description2")).thenReturn("child");
		
		rootNode.addChild(child2Node).addChild(child1Node);
		EntityBuilder<Root> eb = new EntityBuilder<>(rootNode);
		ResultSetUtils.processResultSet(rs, eb);
		
		List<Root> entities = eb.getEntityList();
		assertEquals(1, entities.size());
		
		Root root = entities.get(0);
		assertSame(root.getChild1(), root.getChild2());
		Child child = root.getChild1();

		assertFalse(EntityUtils.isProxy(child));
		
		assertEquals(10, child.getEntityId());
		assertEquals("child", child.getDescription());
	}
	

	
	private static class Root implements Entity {
		
		private int id;
		
		private Child child1;
		
		private Child child2;

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.id = (int) entityId;
		}

		public Child getChild1() {
			return child1;
		}

		public Child getChild2() {
			return child2;
		}
		
	}
	
	private static class Child implements Entity {
		private int id;
		
		private String description; 
		
		@SuppressWarnings("unused")
		public Child() {
			
		}
		
		public Child(String description) {
			this.description = description;
		}

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.id = (int) entityId;
		}

		public String getDescription() {
			return description;
		}
		
	}
}
