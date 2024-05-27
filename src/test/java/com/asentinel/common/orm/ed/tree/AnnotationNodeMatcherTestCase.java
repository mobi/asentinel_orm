package com.asentinel.common.orm.ed.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.EntityDescriptor;

public class AnnotationNodeMatcherTestCase {

	@Test
	public void matchField() {
		AnnotationNodeMatcher matcher = new AnnotationNodeMatcher(TargetAnn.class);
		EntityDescriptor ed = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findField(AnnotatedFieldTest.class, "child"));
		assertTrue(matcher.match(new SimpleNode<>(ed)));
	}

	@Test
	public void matchMethod() {
		AnnotationNodeMatcher matcher = new AnnotationNodeMatcher(TargetAnn.class);
		EntityDescriptor ed = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findMethod(AnnotatedMethodTest.class, "setChild"));
		assertTrue(matcher.match(new SimpleNode<>(ed)));
	}


	@Test
	public void noMatchField() {
		AnnotationNodeMatcher matcher = new AnnotationNodeMatcher(TargetAnn.class);
		EntityDescriptor ed = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findField(NotAnnotatedFieldTest.class, "child"));
		assertFalse(matcher.match(new SimpleNode<>(ed)));
	}

	@Test
	public void noMatchMethod() {
		AnnotationNodeMatcher matcher = new AnnotationNodeMatcher(TargetAnn.class);
		EntityDescriptor ed = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findMethod(NotAnnotatedMethodTest.class, "setChild"));
		assertFalse(matcher.match(new SimpleNode<>(ed)));
	}
	
	@Test
	public void matchField_2AnnotationsMatcher() {
		AnnotationNodeMatcher matcher = new AnnotationNodeMatcher(TargetAnn.class, TargetAnn2.class);
		EntityDescriptor ed = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findField(DoubleAnnotatedFieldTest.class, "child"));
		assertTrue(matcher.match(new SimpleNode<>(ed)));
		
		EntityDescriptor ed2 = new EntityDescriptor(Object.class,
				(rs, n) -> {return null;}, (rs, n) -> {return null;}, 
				"test", (Member) ReflectionUtils.findField(DoubleAnnotatedFieldTest.class, "child2"));
		assertTrue(matcher.match(new SimpleNode<>(ed2)));
	}	

	private static class AnnotatedFieldTest {
		@TargetAnn
		String child;
	}

	private static class AnnotatedMethodTest {
		@TargetAnn
		public void setChild() {
			
		}
	}
	
	private static class NotAnnotatedFieldTest {
		@SuppressWarnings("unused")
		String child;
	}

	private static class NotAnnotatedMethodTest {
		@SuppressWarnings("unused")
		public void setChild() {
			
		}
	}
	
	private static class DoubleAnnotatedFieldTest extends AnnotatedFieldTest {
		@TargetAnn2
		String child2;
	}
	
	
	@Target({ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	private static @interface TargetAnn {
		
	}
	
	@Target({ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	private static @interface TargetAnn2 {
		
	}	
}
