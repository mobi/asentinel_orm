package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EDTRDuplicateTableAliasTestCase {
	private static final Logger log = LoggerFactory.getLogger(EDTRDuplicateTableAliasTestCase.class);
	
	EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	
	@Test
	public void test1() {
		try {
			edtr.getEntityDescriptorTree(A.class);
			fail("Should have thrown exception because we have a duplicated table alias.");
		} catch (IllegalArgumentException e) {
			log.debug("test1 - Expected exception: {}", e.getMessage());
		}
	}
	
	@Test
	public void test2() {
		try {
			edtr.getEntityDescriptorTree(A1.class);
			fail("Should have thrown exception because we have a duplicated table alias.");
		} catch (IllegalArgumentException e) {
			log.debug("test2 - Expected exception: {}", e.getMessage());
		}
	}
	
	@Test
	public void test3() {
		try {
			edtr.getEntityDescriptorTree(A2.class, "A2");
			fail("Should have thrown exception because we have a duplicated table alias.");
		} catch (IllegalArgumentException e) {
			log.debug("test3 - Expected exception: {}", e.getMessage());
		}
	}

	@Table("A")
	private static class A {
		@PkColumn("id")
		int pk;
		
		@Child(tableAlias="a1")
		B b;
	}

	@Table("B")
	private static class B {
		@PkColumn("id")
		int pk;
		
		@Child(tableAlias="A2")
		C b;
	}

	@Table("C")
	private static class C {
		@PkColumn("id")
		int pk;

		@Child(tableAlias="a2")
		D d;
		
	}
	
	@Table("D")
	private static class D {
		@PkColumn("id")
		int pk;
	}
	
	// --------------	

	@Table("A")
	private static class A1 {
		@PkColumn("id")
		int pk;
		
		@Child(tableAlias="a1")
		B1 b;
		
		@Child(tableAlias="A1")
		C1 c;
	}

	@Table("B")
	private static class B1 {
		@PkColumn("id")
		int pk;
	}

	@Table("C")
	private static class C1 {
		@PkColumn("id")
		int pk;
		
	}
	
	
	// ---------------

	@Table("A")
	private static class A2 {
		@PkColumn("id")
		int pk;
		
		@Child(tableAlias="a1")
		B2 b;
		
		@Child(tableAlias="a2")
		C2 c;
	}

	@Table("B")
	private static class B2 {
		@PkColumn("id")
		int pk;
	}

	@Table("C")
	private static class C2 {
		@PkColumn("id")
		int pk;
		
	}
}
