package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class EntityBuilderWithCircularReferenceProxyTestCase {
	
	private final ResultSet rs = mock(ResultSet.class);
	
	private final EntityBuilder<Inventory> eb;
	
	{
		EntityDescriptor rootEd = new EntityDescriptor(Inventory.class, 
				(rs, n) -> rs.getObject("id"),
				(rs, n) -> {
					Inventory i = new Inventory();
					i.number = rs.getString("number");
					return i;
				}, "rootMapper");
		
		EntityDescriptor childEd = new EntityDescriptor(Inventory.class, 
				(rs, n) -> rs.getObject("parentId"),
				(rs, n) -> {
					return ProxyFactory.getInstance().newProxy(Inventory.class, id -> {
						assertEquals(2, id);
						return new Inventory((int) id, "parentInventoryNo");
					});
				}, "childMapper",
				ReflectionUtils.findField(Inventory.class, "parent"));		
		Node<EntityDescriptor> rootNode = new SimpleNode<>(rootEd);
		Node<EntityDescriptor> childNode = new SimpleNode<>(childEd);
		rootNode.addChild(childNode);
		eb = new EntityBuilder<>(rootNode);
	}
	
	/**
	 * This test processes the following resultset:
	 * 
	 * <pre>
	 * 	id			number						parentId
	 * 	1			childInventoryNo			2
	 * 	2			parentInventoryNo			null
	 * </pre>
	 * 
	 * The expected result for the result set above is that the {@link EntityBuilder}
	 * returns 2 entities. The first one has the id 1 and the parent is a proxy with id 2. 
	 * The second entity is a proxy with id 2 and is the same as the parent of the first one.
	 * <br>
	 * This test fails for versions lower or equal to 1.55.22 because of a bug.
	 */
	@Test
	public void parentOnFirstRow() throws SQLException {
		when(rs.next()).thenReturn(true, true, false);
		
		when(rs.getObject("id")).thenReturn(1, 2);
		
		when(rs.getObject("parentId")).thenReturn(2, (Integer) null);
		
		when(rs.getString("number")).thenReturn("childInventoryNo", "parentInventoryNo");
		
		ResultSetUtils.processResultSet(rs, eb);
		
		List<Inventory> entities = eb.getEntityList();
		assertEquals(2, entities.size());
		
		Inventory i0 = entities.get(0);
		Inventory i1 = entities.get(1);

		// these assertions must be made before any methods are called on i0
		assertTrue(EntityUtils.isProxy(i0.getParent()));
		assertTrue(EntityUtils.isLoadedProxy(i0.getParent()));
		
		assertEquals(1, i0.getId());
		assertEquals("childInventoryNo", i0.getNumber());
		assertEquals(2, i0.getParent().getId());

		assertEquals(2, i1.getId());
		assertEquals("parentInventoryNo", i1.getNumber());
		assertNull(i1.getParent());
		assertTrue(EntityUtils.isProxy(i1));
		
		assertSame(i0.getParent(), i1);
	}
	

	/**
	 * This test processes the following result set:
	 * 
	 * <pre>
	 * 	id			number						parentId
	 * 	2			parentInventoryNo			null
	 * 	1			childInventoryNo			2
	 * </pre>
	 * 
	 * The expected result for the result set above is that the {@link EntityBuilder}
	 * returns 2 entities. The first one has the id 2 and the parent is {@code null}. 
	 * The second entity has the id 2 and the parent is exactly the first entity.
	 */
	@Test
	public void parentOnLastRow() throws SQLException {
		when(rs.next()).thenReturn(true, true, false);
		
		when(rs.getObject("id")).thenReturn(2, 1);
		
		when(rs.getObject("parentId")).thenReturn((Integer) null, 2);
		
		when(rs.getString("number")).thenReturn( "parentInventoryNo", "childInventoryNo");
		
		ResultSetUtils.processResultSet(rs, eb);
		
		List<Inventory> entities = eb.getEntityList();
		assertEquals(2, entities.size());
		
		Inventory i0 = entities.get(0);
		Inventory i1 = entities.get(1);
		
		assertEquals(2, i0.getId());
		assertEquals("parentInventoryNo", i0.getNumber());
		assertNull(i0.getParent());

		assertEquals(1, i1.getId());
		assertEquals("childInventoryNo", i1.getNumber());
		assertNotNull(i1.getParent());
		assertFalse(EntityUtils.isProxy(i1.getParent()));
		
		assertSame(i0, i1.getParent());
	}

	
	private static class Inventory implements Entity {
		
		private int id;
		
		private String number;
		
		private Inventory parent;
		
		public Inventory() { }

		public Inventory(int id, String number) {
			this.id = id;
			this.number = number;
		}

		@Override
		public String toString() {
			return "Inventory [id=" + id + ", number=" + number + "]";
		}

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void setEntityId(Object entityId) {
			this.id = (int) entityId;
		}

		public int getId() {
			return id;
		}

		public String getNumber() {
			return number;
		}

		public Inventory getParent() {
			return parent;
		}
	}
}
