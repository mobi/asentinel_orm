package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ReusableRowMappers;
import com.asentinel.common.orm.Bill;
import com.asentinel.common.orm.CacheEntityDescriptor;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.ProxyEntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Tests for the entity tree customization callback chain.
 * 
 * @see EntityDescriptorNodeCallback
 * @see EntityDescriptorTreeRepository
 */
public class EDTRNodeCallbacksTest {
	
	private static final String T1 = "t1";
	private static final String T2 = "t2";
	private static final String T3 = "t3";
	
	private DefaultEntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	
	private Node<EntityDescriptor> node;
	private SimpleEntityDescriptor.Builder builder;

	
	@Before
	public void setup() {
		node = new SimpleNode<EntityDescriptor>();
		builder = new SimpleEntityDescriptor.Builder(Bill.class).name(T1);
	}

	@Test
	public void testNoCallbacks() {
		edtr.processNodeCallbackChain(node, builder);
		assertNotNull(node.getValue());
	}

	@Test
	public void testNullCallbacks() {
		assertTrue(edtr.processNodeCallbackChain(node, builder, (EntityDescriptorNodeCallback[]) null));
		assertNotNull(node.getValue());
	}

	@Test
	public void testEmptyCallbacks() {
		assertTrue(edtr.processNodeCallbackChain(node, builder, new EntityDescriptorNodeCallback[0]));
		assertNotNull(node.getValue());
	}
	
	@Test
	public void testEmptyCallbacksWithProxyEntityDescriptor() {
		ProxyEntityDescriptor<A> ped = new ProxyEntityDescriptor<>(id -> new A(), A.class, null, "id"); 
		node.setValue(ped);
		assertFalse(edtr.processNodeCallbackChain(node, builder));
		assertSame(ped, node.getValue());
	}
	

	@Test
	public void testOneCallback1() {
		assertTrue(edtr.processNodeCallbackChain(node, builder, new EntityDescriptorNodeCallback[]{
			new TestEntityDescriptorNodeCallback(false, T2)
		}));
		assertNotNull(node.getValue());
		assertEquals(T2, node.getValue().getName());
	}

	@Test
	public void testOneCallback2() {
		final EntityDescriptor setEd = new CacheEntityDescriptor(Bill.class, ReusableRowMappers.ROW_MAPPER_LONG, 
				"test", Collections.emptyList());
		assertFalse(edtr.processNodeCallbackChain(node, builder, 
			new EntityDescriptorNodeCallback() {

				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					node.setValue(setEd);
					return false;
				}
			}
		));
		assertEquals(setEd, node.getValue());
	}
	
	@Test
	public void testTwoCallbacks1() {
		assertTrue(edtr.processNodeCallbackChain(node, builder, 
			new TestEntityDescriptorNodeCallback(false, T2),
			new TestEntityDescriptorNodeCallback(false, T3)
		));
		assertNotNull(node.getValue());
		assertEquals(T2, node.getValue().getName());
	}

	@Test
	public void testTwoCallbacks2() {
		assertTrue(edtr.processNodeCallbackChain(node, builder, 
			new TestEntityDescriptorNodeCallback(true, T2),
			new TestEntityDescriptorNodeCallback(false, T3)
		));
		assertNotNull(node.getValue());
		assertEquals(T3, node.getValue().getName());
	}
	
	@Test
	public void testExceptionInCallback() {
		try {
			edtr.processNodeCallbackChain(node, builder, 
					new EntityDescriptorNodeCallback() {
	
						@Override
						public boolean customize(Node<EntityDescriptor> node, Builder builder) {
							throw new RuntimeException();
						}
					}
				);
		} catch(RuntimeException e) {
			// ensure no processing occurs after an exception
			assertNull(node.getValue());
		}
	}
	
	
	@Test
	public void testSqlBuilderFactoryIsPassed() {
		edtr.setSqlBuilderFactory(mock(SqlBuilderFactory.class));
		edtr.processNodeCallbackChain(node, builder,
			new EntityDescriptorNodeCallback() {
			
				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder, SqlBuilderFactory sqlBuilderFactory) {
					assertNotNull(sqlBuilderFactory);
					return false;
				}


				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					return customize(node, builder, null);
				}
			
			}
		);
	}
	
	
	private static class TestEntityDescriptorNodeCallback implements EntityDescriptorNodeCallback {
		
		private final boolean continueChain;
		private final String name;
		
		TestEntityDescriptorNodeCallback(boolean continueChain, String name) {
			this.continueChain = continueChain;
			this.name = name;
		}

		@Override
		public boolean customize(Node<EntityDescriptor> node, Builder builder) {
			builder.name(name);
			return continueChain;
		}
		
	}
	
	@Table("A")
	private static class A {
		
		@PkColumn("id")
		int id;
	}
}
