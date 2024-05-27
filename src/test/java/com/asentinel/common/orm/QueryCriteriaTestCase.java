package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;

public class QueryCriteriaTestCase {
	
	Node<EntityDescriptor> root = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(Bill.class, "b");
	
	@Test
	public void test_getMainOrderByClauseSafe_add_id() {
		QueryCriteria c = new QueryCriteria.Builder(root)
			.mainOrderByClause("b.ItemNumber")
			.build();
		assertEquals("b.ItemNumber", c.getMainOrderByClause());
		assertEquals("b.ItemNumber,b.BillId", c.getMainOrderByClauseSafe().replace(" ", ""));
	}

	@Test
	public void test_getMainOrderByClauseSafe_no_order() {
		QueryCriteria c = new QueryCriteria.Builder(root)
			.build();
		assertNull(c.getMainOrderByClause());
		assertEquals("b.BillId", c.getMainOrderByClauseSafe().replace(" ", ""));
	}

	@Test
	public void test_getMainOrderByClauseSafe_order_by_bill_id() {
		QueryCriteria c = new QueryCriteria.Builder(root)
			.mainOrderByClause("B.biLLId")
			.build();
		assertEquals("b.billid", c.getMainOrderByClause().toLowerCase());
		assertEquals("b.billid", c.getMainOrderByClauseSafe().replace(" ", "").toLowerCase());
	}
	
}
