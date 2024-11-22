package com.asentinel.common.orm;

import static com.asentinel.common.orm.TargetMembersHolder.findFirstNonViewTableAnnotation;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asentinel.common.orm.mappers.Table;

/**
 * Tests the static utility method {@link TargetMembersHolder#findFirstNonViewTableAnnotation(Class)} 
 */
public class TargetMembersHolder2TestCase {

	@Test
	public void testNull() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(null);
		assertNull(info);
	}
	
	@Test
	public void testObject() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Object.class);
		assertNull(info);
	}

	@Test
	public void testInteger() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Integer.class);
		assertNull(info);
	}


	@Test
	public void testNoAnn() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(NoAnn.class);
		assertNull(info);
	}

	@Test
	public void testSimple() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Simple.class);
		assertNotNull(info);
		assertEquals(Simple.class, info.getTargetClass());
		assertEquals("Simple", info.getTableAnn().value());
	}

	@Test
	public void testExtended1() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Extended1.class);
		assertNotNull(info);
		assertEquals(Simple.class, info.getTargetClass());
		assertEquals("Simple", info.getTableAnn().value());
	}
	
	@Test
	public void testExtended2() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Extended2.class);
		assertNotNull(info);
		assertEquals(Extended2.class, info.getTargetClass());
		assertEquals("Simple", info.getTableAnn().value());
	}

	@Test
	public void testView() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(View.class);
		assertNull(info);
	}

	@Test
	public void testExtendedView() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(ExtendedView.class);
		assertNull(info);
	}

	@Test
	public void testExtendedViewNoAnn() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(ExtendedViewNoAnn.class);
		assertNull(info);
	}

	/**
	 * intermediate class without Table annotation
	 */
	@Test
	public void testExtended4() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Extended4.class);
		assertNotNull(info);
		assertEquals(Extended3.class, info.getTargetClass());
		assertEquals("Simple", info.getTableAnn().value());
	}
	
	/**
	 * intermediate class without Table annotation and abstract class
	 */
	@Test
	public void testVeryComplex() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(VeryComplex.class);
		assertNotNull(info);
		assertEquals(Complex.class, info.getTargetClass());
		assertEquals("Simple", info.getTableAnn().value());
	}
	

	@Test
	public void testSuperClassIsView() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(SubView.class);
		assertNull(info);
	}
	
	
	@Test
	public void testImplementer1() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Implementer1.class);
		assertNotNull(info);
		assertEquals(Implementer1.class, info.getTargetClass());
		assertEquals("Interface1", info.getTableAnn().value());
	}
	
	@Test
	public void testImplementer2() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Implementer2.class);
		assertNotNull(info);
		assertEquals(Implementer2.class, info.getTargetClass());
		assertEquals("Interface1", info.getTableAnn().value());
	}
	
	@Test
	public void testImplementer3() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Implementer3.class);
		assertNotNull(info);
		assertEquals(Implementer3.class, info.getTargetClass());
		assertEquals("Interface2", info.getTableAnn().value());
	}
	
	
	@Test
	public void testBaseClassIsTableAnd2SubclassesAreViews() {
		TableAnnotationInfo info = findFirstNonViewTableAnnotation(Class3.class);
		assertNotNull(info);
		assertEquals(Class1.class, info.getTargetClass());
		assertEquals("Class1", info.getTableAnn().value());

	}
	
	static class NoAnn {
		
	}
	
	@Table("Simple")
	static class Simple {
		
	}
	
	@Table(value = "Extended", view = true)
	static class Extended1 extends Simple {
	
	}
	
	static class Extended2 extends Simple {
		
	}
	
	@Table(value = "View", view = true)
	static class View {
		
	}
	
	@Table(value = "ExtendedView", view = true)
	static class ExtendedView {
		
	}

	@Table(value = "ExtendedViewNoAnn", view = true)
	static class ExtendedViewNoAnn extends NoAnn {
		
	}
	
	// --------- intermediate class without Table annotation ------- //
	
	static class Extended3 extends Simple {
		
	}
	
	@Table(value = "Extended4View", view = true)
	static class Extended4 extends Extended3 {
		
	}
	
	
	// --------- intermediate class without Table annotation and abstract class ------- //
	
	static class Complex extends Simple {
		
	}

	abstract static class AbstractVeryComplex extends Complex {
		
	}

	
	@Table(value = "VeryComplex", view = true)
	static class VeryComplex extends AbstractVeryComplex {
		
	}
	
	
	
	// -------- super class is view, subclass is non view ------ //
	
	@Table(value = "SuperView", view = true)
	static class SuperView {
		
	}
	
	@Table(value = "SubView")
	static class SubView extends SuperView {
		
	}

	
	// -------- interface level @Table annotation ------ //
	
	@Table(value = "Interface1")
    interface Interface1 {
		
	}
	
	@Table(value = "Interface2")
    interface Interface2 {
		
	}
	
	
	static class Implementer1 implements Interface1 {
		
	}
	
	static class Implementer2 extends Implementer1 {
		
	}

	static class Implementer3 extends Implementer2 implements Interface2 {
		
	}
	
	// ------ Base class is table, 2 extending classes are views -----
	
	
	@Table(value = "Class1")
	private static class Class1 {
		
	}
	
	@Table(value = "Class2", view = true)
	private static class Class2 extends Class1 {
		
	}

	@Table(value = "Class3", view = true)
	private static class Class3 extends Class2 {
		
	}
	
}
