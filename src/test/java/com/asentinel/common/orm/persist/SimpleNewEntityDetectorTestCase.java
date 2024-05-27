package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.persist.SimpleUpdaterTestCase.Bean;

public class SimpleNewEntityDetectorTestCase {

	SimpleNewEntityDetector d = new SimpleNewEntityDetector();
	
	
	@Test
	public void test1() {
		Bean b = new Bean();
		assertTrue(d.isNewEntity(b));
	}

	@Test
	public void test2() {
		Bean b = new Bean(-1, 1, 2, 0, 10, 20);
		assertTrue(d.isNewEntity(b));
	}

	@Test
	public void test3() {
		Bean b = new Bean(100, 1, 2, 0, 10, 20);
		assertFalse(d.isNewEntity(b));
	}
	
	@Test
	public void test4() {
		NullIdBean b = new NullIdBean();
		assertTrue(d.isNewEntity(b));
	}

	@Test
	public void test5() {
		NullIdBean b = new NullIdBean();
		b.id = 7;
		assertFalse(d.isNewEntity(b));
	}

	
}

class NullIdBean {
	@PkColumn("id")
	Integer id;
}
