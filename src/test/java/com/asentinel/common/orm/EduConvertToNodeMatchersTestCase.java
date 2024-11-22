package com.asentinel.common.orm;

import static com.asentinel.common.orm.EntityDescriptorUtils.convertToNodeMatchers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.orm.ed.tree.AnnotationNodeMatcher;

public class EduConvertToNodeMatchersTestCase {

	@Test
	public void nul() {
		assertEquals(0, convertToNodeMatchers((Object[]) null).length);
	}

	@Test
	public void nullElement() {
		NodeMatcher<EntityDescriptor>[] matchers = convertToNodeMatchers(null, String.class);
		assertTrue(matchers[0].match(null));
	}
	
	@Test
	public void classes() {
		NodeMatcher<EntityDescriptor>[] matchers = convertToNodeMatchers(Integer.class, String.class);
        assertSame(Integer.class, ((EntityDescriptorNodeMatcher) matchers[0]).getType());
        assertSame(String.class, ((EntityDescriptorNodeMatcher) matchers[1]).getType());
	}
	
	@Test
	public void classAndMatcher() {
		EntityDescriptorNodeMatcher m2 = new EntityDescriptorNodeMatcher(String.class);
		NodeMatcher<EntityDescriptor>[] matchers = convertToNodeMatchers(Integer.class, m2);
        assertSame(Integer.class, ((EntityDescriptorNodeMatcher) matchers[0]).getType());
		assertSame(matchers[1], m2);
	}

	@Test
	public void classAndAnnotation() {
		NodeMatcher<EntityDescriptor>[] matchers = convertToNodeMatchers(Integer.class, Override.class);
        assertSame(Integer.class, ((EntityDescriptorNodeMatcher) matchers[0]).getType());
        assertSame(Override.class, ((AnnotationNodeMatcher) matchers[1]).getAnnotationTypes()[0]);
	}
}
