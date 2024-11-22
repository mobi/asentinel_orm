package com.asentinel.common.orm.mappers.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;

import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.TypedObjectFactorySupport;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Tests that the {@link DynamicColumnsEntityNodeCallback} works correctly for
 * both static and dynamic columns.
 * 
 * @since 1.66.0
 * @author Razvan Popian
 */
public class DynamicColumnsEntityNodeCallbackTestCase {
	
	private final TypedObjectFactorySupport<TestBean> factory = new TypedObjectFactorySupport<>(TestBean.class) {

		@Override
		public TestBean newObject() {
			return new TestBean();
		}
		
	};
	
	@Test
	public void standard() {
		DynamicColumnsEntityNodeCallback<DefaultDynamicColumn, TestBean> callback = new DynamicColumnsEntityNodeCallback<>(factory, 
				List.of(
					new DefaultDynamicColumn("SomeField1", String.class),
					new DefaultDynamicColumn("SomeField2", String.class),
					new DefaultDynamicColumn("SomeFK", AssociationBean.class, EnumSet.of(DynamicColumn.DynamicColumnFlags.ENTITY_FK))
		));
		test(callback, null, null);
	}
	
	
	@Test
	public void overriddenTableAndPk() {
		DynamicColumnsEntityNodeCallback<DefaultDynamicColumn, TestBean> callback = new DynamicColumnsEntityNodeCallback<>(factory, 
				List.of(
					new DefaultDynamicColumn("SomeField1", String.class),
					new DefaultDynamicColumn("SomeField2", String.class),
					new DefaultDynamicColumn("SomeFK", AssociationBean.class, EnumSet.of(DynamicColumn.DynamicColumnFlags.ENTITY_FK))),
					new DefaultDynamicColumn("OverriddenId", Integer.class),
					"OverriddenTable"
			);
		test(callback, "OverriddenTable", "OverriddenId");
	}
	
	private SimpleNode<EntityDescriptor> test(DynamicColumnsEntityNodeCallback<DefaultDynamicColumn, TestBean> callback, String tableName, String pk) {
		SimpleEntityDescriptor.Builder builder = new SimpleEntityDescriptor.Builder(TestBean.class);
		SimpleNode<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		
		SqlBuilderFactory sbf = mock(SqlBuilderFactory.class);
		when(sbf.getEntityDescriptorTreeRepository()).thenReturn(new DefaultEntityDescriptorTreeRepository());
		
		assertFalse(callback.customize(node, builder, sbf));
		
		assertTrue(node.getValue() instanceof DynamicColumnsEntityDescriptor);
		@SuppressWarnings("unchecked")
		DynamicColumnsEntityDescriptor<DefaultDynamicColumn, TestBean> dced = (DynamicColumnsEntityDescriptor<DefaultDynamicColumn, TestBean>) node.getValue();

        assertEquals(Objects.requireNonNullElse(tableName, "Test"), dced.getTableName());
		
		if (pk == null) {
			assertEquals("Id", dced.getPkName());
		} else {
			assertEquals(tableName, dced.getTableName());
		}
		
		// simple columns assertions
		assertEquals(
				Set.of("StaticColumn", "StaticColumnInputStream", "SomeField1", "SomeField2", pk == null ? "Id" : pk), 
				new HashSet<>(dced.getColumnNames()));
		assertEquals(
				Set.of(new DefaultDynamicColumn("SomeField1", String.class), new DefaultDynamicColumn("SomeField2", String.class)), 
				new HashSet<>(dced.getDynamicColumns()));
		
		// FK column assertions
		assertEquals(1, node.getChildren().size());
		assertTrue(node.getChildren().get(0).getValue() instanceof SimpleEntityDescriptor);
		SimpleEntityDescriptor childEd = (SimpleEntityDescriptor) node.getChildren().get(0).getValue();
		assertSame(AssociationBean.class, childEd.getEntityClass());
		assertEquals("Association", childEd.getTableName());
		assertEquals("Aid", childEd.getPkName());
		assertEquals("SomeFK", childEd.getFkName());
		assertEquals(new DefaultDynamicColumn("SomeFK", AssociationBean.class), childEd.getName());
		assertTrue(childEd.getTableAlias().startsWith(dced.getTableAlias()));
		assertTrue(childEd.getTableAlias().endsWith("0"));
		
		return node;
	}

	/**
	 * This test validates that if callbacks are added for a certain column, those
	 * callbacks get called.
	 */
	@Test
	public void callbackForSomeFk() {
		DynamicColumnsEntityNodeCallback<DefaultDynamicColumn, TestBean> callback = new DynamicColumnsEntityNodeCallback<>(factory, 
				List.of(
					new DefaultDynamicColumn("SomeField1", String.class),
					new DefaultDynamicColumn("SomeField2", String.class),
					new DefaultDynamicColumn("SomeFK", ParentAssociationBean.class, EnumSet.of(DynamicColumn.DynamicColumnFlags.ENTITY_FK))
		));
		callback.addDynamicColumnEntityOverride(
			new DefaultDynamicColumn("SomeFK", ParentAssociationBean.class, EnumSet.of(DynamicColumn.DynamicColumnFlags.ENTITY_FK)),
			new DynamicColumnEntityOverride(
				null,
				(n, b) -> {
					// change the table name, we will assert the change later
					b.tableName("OverriddenDynamicChildTable");
					return true;
				}
			)
		);
		
		SimpleEntityDescriptor.Builder builder = new SimpleEntityDescriptor.Builder(TestBean.class);
		SimpleNode<EntityDescriptor> node = new SimpleNode<EntityDescriptor>();
		
		SqlBuilderFactory sbf = mock(SqlBuilderFactory.class);
		when(sbf.getEntityDescriptorTreeRepository()).thenReturn(new DefaultEntityDescriptorTreeRepository());
		
		assertFalse(callback.customize(node, builder, sbf));
		assertTrue(node.getValue() instanceof DynamicColumnsEntityDescriptor);
		
		SimpleNode<EntityDescriptor> child = (SimpleNode<EntityDescriptor>) node.getChildren().get(0).getChildren().get(0);
		SimpleEntityDescriptor parentEd = (SimpleEntityDescriptor) node.getChildren().get(0).getValue();
		SimpleEntityDescriptor childEd = (SimpleEntityDescriptor) child.getValue();
		
		// ensure our callback got called
		assertEquals("OverriddenDynamicChildTable", childEd.getTableName());
		
		// ensure the OverrideTableAliasNodeCallback got called and it set an alias based on the
		// parent alias
		assertTrue(childEd.getTableAlias().startsWith(parentEd.getTableAlias()));
		assertNotEquals(parentEd.getTableAlias(), childEd.getTableAlias());
	}

	
	@Table("Test")
	private static class TestBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		
		@PkColumn("Id")
		int id;
		
		@Column("StaticColumn")
		int staticColumn;
		
		@Column("StaticColumnInputStream")
		InputStream staticColumnInputStream;
		
		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			return null;
		}
	}

	@Table("Association")
	private static class AssociationBean {
		@PkColumn("Aid")
		int id;
	}
	
	@Table("ParentAssociation")
	private static class ParentAssociationBean {
		@PkColumn("pid")
		int id;
		
		@Child
		ChildAssociation childAssociation;
	}
	
	@Table("ChildAssociation")
	private static class ChildAssociation {
		@PkColumn("cid")
		int id;
	}

}
