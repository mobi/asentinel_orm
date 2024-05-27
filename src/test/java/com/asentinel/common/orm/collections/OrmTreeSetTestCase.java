package com.asentinel.common.orm.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 * @author Razvan Popian
 */
public class OrmTreeSetTestCase {

	private final OrmTreeSet<Integer> set = new OrmTreeSet<>();
	
	@Test
	public void add() {
		assertFalse(set.add(20));
		assertFalse(set.add(10));
		
		assertTrue(set.isEmpty());
		
		set.markAsOrmDone();
		
		assertEquals(2, set.size());
		
		assertEquals(10, set.first().intValue());
		assertEquals(20, set.last().intValue());
		
		assertTrue(set.add(5));
		assertEquals(3, set.size());
		assertEquals(5, set.first().intValue());
		assertEquals(20, set.last().intValue());
	}
	
	
	@Test
	public void equals() {
		assertFalse(set.add(20));
		assertFalse(set.add(10));
		
		assertNotEquals(Set.of(10, 20), set);
		
		set.markAsOrmDone();
		assertEquals(Set.of(10, 20), set);
	}

	
	@Test
	public void markAsOrmDoneDoubleCall() {
		assertFalse(set.add(20));
		assertFalse(set.add(10));
		
		set.markAsOrmDone();
		set.markAsOrmDone();
		
		assertEquals(2, set.size());
	}

}
