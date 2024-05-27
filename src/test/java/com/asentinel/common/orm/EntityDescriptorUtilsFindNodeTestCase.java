package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.getEntityDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EntityDescriptorUtilsFindNodeTestCase {
	
	Node<EntityDescriptor> root = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(Invoice.class);

	@Test
	public void testFindRoot1() {
		assertEquals(root.getValue(), getEntityDescriptor(root, (Object[]) null));
	}
	
	@Test
	public void testFindRoot2() {
		assertEquals(root.getValue(), getEntityDescriptor(root));
	}
	
	@Test
	public void testFindRoot3() {
		assertEquals(root.getValue(), getEntityDescriptor(root, Invoice.class));
	}
	
	@Test
	public void testFindRoot4() {
		Object[] path = new Object[]{null};
		assertEquals(root.getValue(), getEntityDescriptor(root, path));
		assertNull(path[0]);
	}
	
	
	@Test
	public void testFindBill1() {
		assertEquals(Bill.class, getEntityDescriptor(root, Invoice.class, Bill.class).getEntityClass());
	}

	@Test
	public void testFindBill2() {
		assertEquals(Bill.class, getEntityDescriptor(root, Invoice.class, new EntityDescriptorNodeMatcher(Bill.class)).getEntityClass());
	}
	
	
	@Test
	public void testFindBill3() {
		Object[] path = new Object[]{null, Bill.class};
		assertEquals(Bill.class, getEntityDescriptor(root, path).getEntityClass());
		assertNull(path[0]);
	}
	
	@Test
	public void testFindBill4() {
		Object[] path = new Object[]{null, new EntityDescriptorNodeMatcher(Bill.class)};
		assertEquals(Bill.class, getEntityDescriptor(root, path).getEntityClass());
		assertNull(path[0]);
	}
	

	
	@Test(expected = IllegalArgumentException.class)
	public void testNotFound() {
		getEntityDescriptor(root, Invoice.class, Integer.class).getEntityClass();
	}

	@Test
	public void testNullInPathForIndexAbove0_OK_1() {
		assertEquals(Bill.class, getEntityDescriptor(root, Invoice.class, null).getEntityClass());
	}
	
	@Test
	public void testNullInPathForIndexAbove0_OK_2() {
		// null, null explained: 
		//		first null always matches the root
		// 		the second null matches any child of the root. In this case there is only
		//		one child and that is returned
		assertEquals(Bill.class, getEntityDescriptor(root, null, null).getEntityClass());
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullInPathForIndexAbove0_FAIL() {
		root = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(ExtInvoice.class);
		assertEquals(Bill.class, getEntityDescriptor(root, Invoice.class, null).getEntityClass());
	}

	
	private static class ExtInvoice extends Invoice {
		
		@Child
		CostCenter cc;
		
	}
	
	@Table("cc")
	private static class CostCenter {
		@PkColumn("id")
		int  id;
	}
}
