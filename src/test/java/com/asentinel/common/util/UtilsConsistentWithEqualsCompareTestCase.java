package com.asentinel.common.util;

import static com.asentinel.common.util.Utils.consistentWithEqualsCompare;
import static org.junit.Assert.*;

import java.util.Comparator;

import org.junit.Test;

/**
 * @author Razvan Popian
 */
public class UtilsConsistentWithEqualsCompareTestCase {
	
	private final A a1 = new A();
	{
		a1.id = 1;
		a1.name = "a1";
	}

	private final A a2 = new A();
	{
		a2.id = 2;
		a2.name = "a2";
	}
	
	@Test
	public void equal() {
		a2.id = a1.id;
		a2.name = a2.name;
        assertEquals(a1, a2);
		assertEquals(0, a1.compareTo(a2));
	}

	@Test
	public void notEqual() {
        assertNotEquals(a1, a2);
		assertTrue(a1.compareTo(a2) < 0);
	}

	
	@Test(expected = IllegalStateException.class)
	public void equalAccordingToEqualsButNotAccordingToCompare() {
		A a3 = new A() {
			@Override
			public int compareTo(A o) {
				return consistentWithEqualsCompare(this, o, Comparator.comparing(A::getName));
			}
		};
		a3.id = 3;
		a3.name = a1.name;
		
		a3.compareTo(a1);
	}
	
	
	private static class A implements Comparable<A> {
		int id;
		String name;
		
		@Override
		public int hashCode() {
			return id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			A other = (A) obj;
            return id == other.id;
        }

		@Override
		public int compareTo(A o) {
			return consistentWithEqualsCompare(this, o, Comparator.comparing(A::getName).thenComparing(A::getId));
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}
}
