package com.asentinel.common.orm;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;

public class EntityDescriptorNodeMatcherTestCase {
	
	// not strict tests
	
	@Test
	public void testOnlyClass_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertTrue(matcher.match(node));
	}
	
	@Test
	public void testOnlyClassWithSubclass_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Object.class);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertTrue(matcher.match(node));
	}
	
	@Test
	public void testOnlySubClassWithClass_notStrict() {
		class ExtBill extends Bill { };
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(ExtBill.class);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertTrue(matcher.match(node));
	}
	


	@Test
	public void testClassAndTableAlias_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, "test");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("test").build());
		assertTrue(matcher.match(node));
	}
	
	@Test
	public void testOnlyClass_Fail_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Integer.class);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertFalse(matcher.match(node));
	}

	@Test
	public void testClassAndTableAlias_Fail_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, "test1");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("test").build());
		assertFalse(matcher.match(node));
	}

	@Test
	public void testClassAndFkName_notStrict() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("test").fkName("BillId").build());
		assertTrue(matcher.match(node));
	}

	@Test
	public void testClassAndFkName_Fail_notStrict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, "billId");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("invoiceId").build());
		assertFalse(matcher.match(node));
	}
	
	@Test
	public void testClassAndEmptyFkName_notStrict() {
		NodeMatcher<EntityDescriptor> matcher = EntityDescriptorNodeMatcher.forTypeAndEmptyFkName(Bill.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("").build());
		assertTrue(matcher.match(node));
	}

	@Test
	public void testClassAndEmptyFkName_Fail_notStrict() {
		NodeMatcher<EntityDescriptor> matcher = EntityDescriptorNodeMatcher.forTypeAndEmptyFkName(Bill.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("invoiceId").build());
		assertFalse(matcher.match(node));
	}

	
	// strict tests
	
	@Test
	public void testOnlyClass_strict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertTrue(matcher.match(node));
	}
	
	@Test
	public void testOnlyClassWithSubclass_strict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Object.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertFalse(matcher.match(node));
	}


	@Test
	public void testClassAndTableAlias_strict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, "test", true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("test").build());
		assertTrue(matcher.match(node));
	}
	
	@Test
	public void testOnlyClass_Fail_strict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Integer.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor(Bill.class));
		assertFalse(matcher.match(node));
	}

	@Test
	public void testClassAndTableAlias_Fail_strict() {
		EntityDescriptorNodeMatcher matcher = new EntityDescriptorNodeMatcher(Bill.class, "test1", true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).tableAlias("test").build());
		assertFalse(matcher.match(node));
	}
	
	
	@Test
	public void testClassAndFkName_strict() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId", true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("BILLId").build());
		assertTrue(matcher.match(node));
	}

	
	@Test
	public void testClassAndFkName_Fail_strict() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId", true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("invoiceId").build());
		assertFalse(matcher.match(node));
	}
	
	@Test
	public void testClassAndEmptyFkName_strict() {
		NodeMatcher<EntityDescriptor> matcher = EntityDescriptorNodeMatcher.forTypeAndEmptyFkName(Bill.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("").build());
		assertTrue(matcher.match(node));
	}


	@Test
	public void testClassAndEmptyFkName_Fail_strict() {
		NodeMatcher<EntityDescriptor> matcher = EntityDescriptorNodeMatcher.forTypeAndEmptyFkName(Bill.class, true);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new SimpleEntityDescriptor.Builder(Bill.class).fkName("invoiceId").build());
		assertFalse(matcher.match(node));
	}

	
	// non QueryReady tests
	
	@Test
	public void testClassAndFkName_NotQueryReady() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new EntityDescriptor(Bill.class, "bill", null));
		assertFalse(matcher.match(node));
	}

	
	@Test
	public void testClassAndFkName_ProxyEntityDescriptor() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new ProxyEntityDescriptor<Bill>(id -> new Bill(), Bill.class, null, "BillId"));
		assertFalse(matcher.match(node));
	}

	@Test
	public void testClassAndFkName_CollectionProxyEntityDescriptor() {
		EntityDescriptorNodeMatcher matcher = EntityDescriptorNodeMatcher.forTypeAndFkName(Bill.class, "billId");
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new CollectionProxyEntityDescriptor(id -> new ArrayList<Object>(), Object.class, null, "SomeFkId"));
		assertFalse(matcher.match(node));
	}

	
	@Test
	public void testClassAndEmptyFkName_NotQueryReady() {
		NodeMatcher<EntityDescriptor> matcher = EntityDescriptorNodeMatcher.forTypeAndEmptyFkName(Bill.class);
		Node<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		node.setValue(new EntityDescriptor(Bill.class, "bill", null));
		assertFalse(matcher.match(node));
	}

}
