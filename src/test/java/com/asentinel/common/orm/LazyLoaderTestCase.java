package com.asentinel.common.orm;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.jdbc.DefaultObjectFactory;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class LazyLoaderTestCase {
	
	EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	
	final Object lazyLoadedEntity = new Object();
	boolean supplierCalled = false;
	
	Object actualEntity;
	
	
	@Test(expected = NullPointerException.class)
	public void testNoFactorySpecified() {
		new LazyLoader.Builder()
			.removeChild(Invoice.class).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoChildrenRemoved() {
		new LazyLoader.Builder()
			.parentEntityFactory(new DefaultObjectFactory<>(Bill.class))
			.build();
	}
	
	
	@Test
	public void testMultipleSameClassesNoParentSpecified() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Bill.class,
				new LazyLoader.Builder()
					.parentEntityFactory(new DefaultObjectFactory<>(Bill.class))
					.removeChild(Invoice.class).build()
		);
		Assert.assertEquals(3, TreeUtils.countNodes(root));
		
		// the path to the CC invoice should be valid
		EntityDescriptorUtils.getEntityDescriptor(root, Bill.class, CostCenter.class, Invoice.class);
	}

	@Test
	public void testMultipleSameClassesParentSpecified() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Bill.class,
				new LazyLoader.Builder()
					.parentEntityFactory(new DefaultObjectFactory<>(Bill.class))
					.parentPath(Bill.class)
					.removeChild(Invoice.class).build()
		);
		Assert.assertEquals(3, TreeUtils.countNodes(root));
		
		// the path to the CC invoice should be valid
		EntityDescriptorUtils.getEntityDescriptor(root, Bill.class, CostCenter.class, Invoice.class);
	}

	@Test
	public void testDeepParent() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Bill.class,
				new LazyLoader.Builder()
					.parentEntityFactory(new DefaultObjectFactory<>(Bill.class))
					.parentPath(Bill.class, CostCenter.class)
					.removeChild(Invoice.class).build()
		);
		Assert.assertEquals(3, TreeUtils.countNodes(root));
		
		// the path to the CC invoice should be valid
		EntityDescriptorUtils.getEntityDescriptor(root, Bill.class, Invoice.class);
		EntityDescriptorUtils.getEntityDescriptor(root, Bill.class, CostCenter.class);
	}
	
	@Test
	public void testRootOnly() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Bill.class,
				new LazyLoader.Builder()
					.parentEntityFactory(new DefaultObjectFactory<>(Bill.class))
					.removeChild(Invoice.class)
					.removeChild(CostCenter.class)
					.build()
		);
		Assert.assertEquals(1, TreeUtils.countNodes(root));
		assertEquals(0, root.getChildren().size());
	}
	
	
	@Test
	public void testLoad() {
		LazyLoader.load(this::supplyEntity, this::getEntity, this::setEntity);
		assertEquals(lazyLoadedEntity, actualEntity);
	}
	
	@Test
	public void testLoadAndReload() {
		LazyLoader.load(this::supplyEntity, this::getEntity, this::setEntity);
		assertTrue(supplierCalled);
		supplierCalled = false;
		LazyLoader.load(this::supplyEntity, this::getEntity, this::setEntity);
		assertFalse(supplierCalled);
	}
	

	@Test
	public void testLoadWithNullSupplier() {
		LazyLoader.load(null, this::getEntity, this::setEntity);
		assertNull(actualEntity);
	}

	private Object supplyEntity() {
		supplierCalled = true;
		return lazyLoadedEntity;
	}
	
	private void setEntity(Object entity) {
		this.actualEntity = entity;
	}

	private Object getEntity() {
		return actualEntity;
	}
	

	
	
	@Table("Bill")
	public static class Bill {
		public static final String PK_NAME = "billId";
		
		@PkColumn(PK_NAME)
		int id;
		
		@Child
		Invoice invoice;

		@Child
		CostCenter costCenter;
	}
	
	public static class ExtBill extends Bill {
		@Child
		Gl gl;
	}
	

	@Table("Invoice")
	public static class Invoice {
		public static final String PK_NAME = "invoiceId";
		
		@PkColumn(PK_NAME)
		int id;
		
	}

	@Table("CostCenter")
	public static class CostCenter {
		public static final String PK_NAME = "id";
		
		@PkColumn(PK_NAME)
		int id;

		@Child
		Invoice invoice;
		
	}
	
	@Table("Gl")
	public static class Gl {
		public static final String PK_NAME = "id";
		
		@PkColumn(PK_NAME)
		int id;

	}
	
}



