package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Tests github issue #4.
 * 
 * @author Razvan.Popian
 */
public class EDTREntityClassOverrideTestCase {
	private static final Logger log = LoggerFactory.getLogger(EDTREntityClassOverrideTestCase.class);

	@Test
	public void test() {
		EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(A.class,
				(n, b) -> {
					if (B.class == b.getEntityClass()) {
						b.entityClass(ExtendedB.class);
					}
					return false;
				});
		log.debug("test- \n{}", root.toStringAsTree());
		Node<EntityDescriptor> bNode = root.getChildren().get(0);
		SimpleEntityDescriptor bed = (SimpleEntityDescriptor) bNode.getValue();
		log.debug("test - table: {}",bed.getTableName());
		assertSame(ExtendedB.class, bed.getEntityClass());
		assertEquals("BBB_Override_Table_Level", bed.getTableName());
	}
	
	
	@Table("AAA")
	public static class A {
		
		@PkColumn("id")
		private int id;
		
		@Child(tableName = "BBB_Override_Child_Level")
		private B b;
	}

	@Table("BBB")
	public static class B {
		
		@PkColumn("id")
		private int id;
	}
	
	
	@Table("BBB_Override_Table_Level")
	public static class ExtendedB extends B {
		
	}
}
