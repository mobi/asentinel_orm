package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EntityBuilderCollectionProxyNullParentTestCase {
	
	ResultSet rs = mock(ResultSet.class);
	
	@Test
	public void test() throws SQLException {
		SimpleEntityDescriptor level0Ed = new SimpleEntityDescriptor.Builder(Level0.class)
			.tableAlias("l0")
			.build();
		SimpleEntityDescriptor level1Ed = new SimpleEntityDescriptor.Builder(Level1.class)
			.tableAlias("l1")
			.targetMember(ReflectionUtils.findField(Level0.class, "level1"))
			.build();
		CollectionProxyEntityDescriptor level2Ed = new CollectionProxyEntityDescriptor(id -> Arrays.asList(new Level2()), 
				Level2.class, ReflectionUtils.findField(Level1.class, "level2s"), "Level1Id");
		Node<EntityDescriptor> level0 = new SimpleNode<>(level0Ed);
		Node<EntityDescriptor> level1 = new SimpleNode<>(level1Ed);
		Node<EntityDescriptor> level2 = new SimpleNode<>(level2Ed);
		level0.addChild(level1);
		level1.addChild(level2);
		
		when(rs.getInt("l0_Level0Id")).thenReturn(10);
		when(rs.getInt("l1_Level1Id")).thenReturn(0);
		
		when(rs.wasNull()).thenReturn(false).thenReturn(true);
		
		EntityBuilder<Level0> eb = new EntityBuilder<>(level0);
		eb.processRow(rs);

		Level0 level0Instance = eb.getEntity();
		assertEquals(10, level0Instance.id);
		assertNull(level0Instance.level1);
		
	}

	
	@Table("level0")
	public static class Level0 {
		@PkColumn("Level0Id")
		int id;
		
		@Child
		Level1 level1;
	}
	
	@Table("level1")
	public static class Level1 {
		@PkColumn("Level1Id")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		List<Level2> level2s;
		
	}

	@Table("level2")
	public static class Level2 {
		@PkColumn("Level2Id")
		int id;
	}
	
}
