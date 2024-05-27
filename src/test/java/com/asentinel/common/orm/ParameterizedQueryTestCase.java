package com.asentinel.common.orm;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class ParameterizedQueryTestCase {
	
	@Test
	public void test1() {
		ParameterizedQuery pq = new ParameterizedQuery();
		assertEquals("", pq.getSql());
		assertEquals(Collections.emptyList(), pq.getMainParameters());
		assertEquals(Collections.emptyList(), pq.getSecondaryParameters());
	}

	@Test
	public void test2() {
		ParameterizedQuery pq = new ParameterizedQuery();
		pq.setSql("aaa");
		pq.addMainParameters(Arrays.asList(1, 2));
		pq.addSecondaryParameters(Arrays.asList(3, 4));
		
		assertEquals("aaa", pq.getSql());
		assertEquals(Arrays.asList(1, 2), pq.getMainParameters());
		assertEquals(Arrays.asList(3, 4), pq.getSecondaryParameters());
	}

}
