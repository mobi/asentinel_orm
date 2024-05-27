package com.asentinel.common.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Razvan Popian
 */
public class ColumnMetadataTestCase {

	@Test
	public void simple() {
		ColumnMetadata cm1 = new ColumnMetadata("test");
		ColumnMetadata cm2 = new ColumnMetadata("TEST");
		
		assertEquals(cm1.hashCode(), cm2.hashCode());
		assertTrue(cm1.equals(cm2));
	}

	
	@Test
	public void simpleDifferent() {
		ColumnMetadata cm1 = new ColumnMetadata("test1");
		ColumnMetadata cm2 = new ColumnMetadata("TEST2");
		
		assertNotEquals(cm1.hashCode(), cm2.hashCode());
		assertFalse(cm1.equals(cm2));
	}
	
	
	@Test
	public void withPrefix() {
		ColumnMetadata cm1 = new ColumnMetadata("pre", "test", false);
		ColumnMetadata cm2 = new ColumnMetadata("Pre", "TEST", false);
		
		assertEquals(cm1.hashCode(), cm2.hashCode());
		assertTrue(cm1.equals(cm2));
	}

	@Test
	public void withPrefixDifferent() {
		ColumnMetadata cm1 = new ColumnMetadata("pre", "test1", false);
		ColumnMetadata cm2 = new ColumnMetadata("Pre", "TEST2", false);
		
		assertNotEquals(cm1.hashCode(), cm2.hashCode());
		assertFalse(cm1.equals(cm2));
	}

	@Test
	public void withPrefixDifferentNullSetting() {
		ColumnMetadata cm1 = new ColumnMetadata("pre", "test", true);
		ColumnMetadata cm2 = new ColumnMetadata("Pre", "TEST", false);
		
		assertEquals(cm1.hashCode(), cm2.hashCode());
		assertTrue(cm1.equals(cm2));
	}
	
	@Test
	public void withNullPrefix() {
		ColumnMetadata cm1 = new ColumnMetadata(null, "test", false);
		ColumnMetadata cm2 = new ColumnMetadata(null, "TEST", false);
		
		assertEquals(cm1.hashCode(), cm2.hashCode());
		assertTrue(cm1.equals(cm2));
	}
	
	@Test
	public void withNullPrefixDifferent() {
		ColumnMetadata cm1 = new ColumnMetadata(null, "test1", false);
		ColumnMetadata cm2 = new ColumnMetadata(null, "TEST2", false);
		
		assertNotEquals(cm1.hashCode(), cm2.hashCode());
		assertFalse(cm1.equals(cm2));
	}

}