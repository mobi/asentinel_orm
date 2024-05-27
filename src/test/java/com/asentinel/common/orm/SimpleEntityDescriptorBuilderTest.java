package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.util.StringUtils;

import com.asentinel.common.jdbc.ObjectFactory;

public class SimpleEntityDescriptorBuilderTest {
	private final static Logger log = LoggerFactory.getLogger(SimpleEntityDescriptorBuilderTest.class);
	
	private void printDescriptor(SimpleEntityDescriptor descriptor) {
		log.debug("testDefault - entityClass: " + descriptor.getEntityClass());
		log.debug("testDefault - name: " + descriptor.getName());
		log.debug("testDefault - pkName: " + descriptor.getPkName());
		log.debug("testDefault - fkName:" + descriptor.getFkName());
		log.debug("testDefault - relationType: " + descriptor.getParentRelationType());
		log.debug("testDefault - tableName: " + descriptor.getTableName());
		log.debug("testDefault - tableAlias: " + descriptor.getTableAlias());
		log.debug("testDefault - columnAliasSeparator: " + descriptor.getColumnAliasSeparator());
	}
	
	@Test
	public void testDefault() {
		SimpleEntityDescriptor descriptor = new SimpleEntityDescriptor(InvoiceParentEntity.class);
		printDescriptor(descriptor);
		assertSame(InvoiceParentEntity.class, descriptor.getEntityClass());
		assertEquals("InvoiceId", descriptor.getName());
		assertEquals(descriptor.getName(), descriptor.getPkName());
		assertNull(descriptor.getFkName());
		assertEquals(RelationType.ONE_TO_MANY, descriptor.getParentRelationType());
		assertEquals(JoinType.LEFT, descriptor.getParentJoinType());
		assertEquals("Invoice", descriptor.getTableName());
		assertTrue(StringUtils.hasText(descriptor.getTableAlias()));
		assertEquals(SimpleEntityDescriptor.DEFAULT_COLUMN_ALIAS_SEPARATOR, descriptor.getColumnAliasSeparator());
		assertNull(descriptor.getTargetMember());
	}

	@Test
	public void testBuilder1() {
		Member targetMember = SimpleEntityDescriptorBuilderTest.class.getMethods()[0];
		ObjectFactory<Entity> entityFactory = new ObjectFactory<Entity>() {
			@Override
			public Entity newObject() throws IllegalStateException {
				return new Entity() {
					@Override
					public void setEntityId(Object object) {}
					
					@Override
					public Object getEntityId() {return null;}
				};
			}
			
		};
		SimpleEntityDescriptor descriptor 
			= new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
				.name("Test")
				.pkName("TestId")
				.fkName("TestFId")
				.tableName("Table")
				.tableAlias("t")
				.parentRelationType(RelationType.MANY_TO_ONE)
				.parentInnerJoin()
				.columnAliasSeparator("X")
				.entityFactory(entityFactory)
				.targetMember(targetMember)
				.build();
		printDescriptor(descriptor);
		assertSame(InvoiceParentEntity.class, descriptor.getEntityClass());
		assertEquals("Test", descriptor.getName());
		assertEquals("TestId", descriptor.getPkName());
		assertEquals("TestFId", descriptor.getFkName());
		assertEquals(RelationType.MANY_TO_ONE, descriptor.getParentRelationType());
		assertEquals(JoinType.INNER, descriptor.getParentJoinType());
		assertEquals("Table", descriptor.getTableName());
		assertEquals("t", descriptor.getTableAlias());
		assertTrue(StringUtils.hasText(descriptor.getTableAlias()));
		assertEquals("X", descriptor.getColumnAliasSeparator());
		assertEquals(entityFactory, descriptor.getEntityFactory());
		assertSame(targetMember, descriptor.getTargetMember());
	}

	@Test
	public void testBuilder2() {
		SimpleEntityDescriptor descriptor 
			= new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
				.pkName("TestId")
				.fkName("TestFId")
				.tableName("Table")
				.tableAlias("t")
				.parentRelationType(RelationType.MANY_TO_ONE)
				.columnAliasSeparator("X")
				.build();
		printDescriptor(descriptor);
		assertSame(InvoiceParentEntity.class, descriptor.getEntityClass());
		assertEquals("TestId", descriptor.getName());
		assertEquals("TestId", descriptor.getPkName());
		assertEquals("TestFId", descriptor.getFkName());
		assertEquals(RelationType.MANY_TO_ONE, descriptor.getParentRelationType());
		assertEquals(JoinType.LEFT, descriptor.getParentJoinType());
		assertEquals("Table", descriptor.getTableName());
		assertEquals("t", descriptor.getTableAlias());
		assertTrue(StringUtils.hasText(descriptor.getTableAlias()));
		assertEquals("X", descriptor.getColumnAliasSeparator());
		assertNull(descriptor.getTargetMember());
	}
	
	
	@Test
	public void testManyToManyWithoutLinkTable() {
		try {
			new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "")
				.build();
			fail("Should throw exception.");
		} catch(IllegalArgumentException e) {
			log.debug("Expected exception: "  + e.getMessage());
		}
	}
	
	
	@Test
	public void testManyToManyBuilder() {
		ManyToManyEntityDescriptor descriptor 
			= new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "link")
				.pkName("TestId")
				.fkName("TestFId")
				.tableName("Table")
				.tableAlias("t")
				.manyToManyTableAlias("l")
				.manyToManyLeftFkName("idl")
				.manyToManyRightFkName("idr")
				.columnAliasSeparator("X")
				.build();
		printDescriptor(descriptor);
		assertSame(InvoiceParentEntity.class, descriptor.getEntityClass());
		assertEquals("TestId", descriptor.getName());
		assertEquals("TestId", descriptor.getPkName());
		assertEquals("TestFId", descriptor.getFkName());
		assertEquals(RelationType.MANY_TO_MANY, descriptor.getParentRelationType());
		assertEquals("Table", descriptor.getTableName());
		assertEquals("t", descriptor.getTableAlias());
		assertTrue(StringUtils.hasText(descriptor.getTableAlias()));
		assertEquals("X", descriptor.getColumnAliasSeparator());
		assertEquals("link", descriptor.getManyToManyTable());
		assertEquals("l", descriptor.getManyToManyTableAlias());
		assertEquals("idl", descriptor.getManyToManyLeftFkName());
		assertEquals("idr", descriptor.getManyToManyRightFkName());
		assertNull(descriptor.getTargetMember());
	}

	@Test
	public void testManyToManyBuilderDefaults() {
		QueryUtils.resetDescriptorId();
		ManyToManyEntityDescriptor descriptor 
			= new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "link").build();
		printDescriptor(descriptor);
		assertSame(InvoiceParentEntity.class, descriptor.getEntityClass());
		assertEquals(RelationType.MANY_TO_MANY, descriptor.getParentRelationType());
		assertEquals("link", descriptor.getManyToManyTable());
		assertEquals("a1", descriptor.getManyToManyTableAlias());
		assertNull(descriptor.getManyToManyLeftFkName());
		assertNull(descriptor.getManyToManyRightFkName());
		assertNull(descriptor.getTargetMember());
	}
	
}
