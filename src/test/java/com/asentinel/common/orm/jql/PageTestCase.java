package com.asentinel.common.orm.jql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class PageTestCase {

	private final static Logger log = LoggerFactory.getLogger(PageTestCase.class);
	
	@Test
	public void test1() {
		Page<Integer> p = new Page<Integer>(Arrays.asList(1,2,3), 10);
		assertEquals(3, p.getItems().size());
		assertEquals(10, p.getCount());
		log.debug("test1 - Page:" + p);
	}

	@Test(expected = NullPointerException.class)
	public void test2() {
		new Page<Integer>(null, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test3() {
		new Page<Integer>(new ArrayList<Integer>(), -1);
	}

}
