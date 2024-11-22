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

public class EntityBuilderWith2ProxyChildrenOfSameTypeTestCase {
	private final ResultSet rs = mock(ResultSet.class);

	private final EntityBuilder<Root> eb;
	
	{
		EntityDescriptor rootEd = new EntityDescriptor(Root.class, 
				(rs, n) -> rs.getObject("id"),
				(rs, n) -> new Root(), "rootMapper");
		
		EntityDescriptor child1Ed = new EntityDescriptor(Child1.class, 
				(rs, n) -> rs.getObject("child1Id"),
				(rs, n) -> ProxyFactory.getInstance().newProxy(Child1.class, id -> {
                    assertEquals(10, id);
                    return new Child1("child");
                }), "child1Mapper",
				ReflectionUtils.findField(Root.class, "child1"));
		
		EntityDescriptor child2Ed = new EntityDescriptor(Child1.class, 
				(rs, n) -> rs.getObject("child2Id"),
				(rs, n) -> {
					return ProxyFactory.getInstance().newProxy(Child1.class, id -> {
						assertEquals(10, id);
						return new Child1("child");
					});
				}, "child2Mapper",
				ReflectionUtils.findField(Root.class, "child2"));		
		
		Node<EntityDescriptor> rootNode = new SimpleNode<>(rootEd);
		Node<EntityDescriptor> child1Node = new SimpleNode<>(child1Ed);
		Node<EntityDescriptor> child2Node = new SimpleNode<>(child2Ed);
		
		rootNode.setValue(rootEd);
		child1Node.setValue(child1Ed);
		child2Node.setValue(child2Ed);
		rootNode.addChild(child1Node).addChild(child2Node);
		eb = new EntityBuilder<>(rootNode);
	}
	
	/**
	 * This test processes the following resultset:
	 * 
	 * <pre>
	 * 	id			child1Id		child2Id
	 * 	1			10				10		
	 * </pre>
	 *
	 * Both children of the root entity are created as proxies. Because they have the same id, exactly one
	 * instance is created and referenced for both of them. 
	 */
	@Test
	public void proxyCreatedButEventuallyLoadedFromTheResultset() throws SQLException {
		when(rs.next()).thenReturn(true, false);
		
		when(rs.getObject("id")).thenReturn(1);
		
		when(rs.getObject("child1Id")).thenReturn(10);
		
		when(rs.getObject("child2Id")).thenReturn(10);
		
		ResultSetUtils.processResultSet(rs, eb);
		
		List<Root> entities = eb.getEntityList();
		assertEquals(1, entities.size());
		
		Root root = entities.get(0);
		assertSame(root.getChild1(), root.getChild2());
		Child1 child = root.getChild1();

		// these assertions must be made before any methods are called on the child
		assertTrue(EntityUtils.isProxy(child));
		assertFalse(EntityUtils.isLoadedProxy(child));
		
		assertEquals(10, child.getEntityId());
		assertEquals("child", child.getDescription());
	}

	
	private static class Root implements Entity {
		
		private int id;
		
		private Child1 child1;
		
		private Child1 child2;

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.id = (int) entityId;
		}

		public Child1 getChild1() {
			return child1;
		}

		public Child1 getChild2() {
			return child2;
		}
		
	}
	
	private static class Child1 implements Entity {
		private int id;
		
		private String description; 
		
		@SuppressWarnings("unused")
		public Child1() {
			
		}
		
		public Child1(String description) {
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
