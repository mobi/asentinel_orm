package com.asentinel.common.text;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Razvan Popian
 * @since 1.71.0
 */
public class FieldIdTypeDescriptorTestCase {

	/**
	 * Validates the fix for a {@link FieldIdTypeDescriptor#toString()} bug caused
	 * by evaluation order.
	 */
	@Test
	public void testToString() {
		FieldIdTypeDescriptor td = new FieldIdTypeDescriptor("TheId", String.class);
		assertTrue(td.toString().contains("TheId"));
		assertTrue(td.toString().contains(String.class.getName()));
	}
}
