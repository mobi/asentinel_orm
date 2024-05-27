package com.asentinel.common.orm;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class EntityBuilderDefaultCollectionInitTestCase {

	ResultSet rs = mock(ResultSet.class);
	
	@Before
	public void setup() throws SQLException {
		when(rs.getInt("t_ParentId")).thenReturn(10);
		when(rs.getInt("t1_KidId")).thenReturn(20);
	}
	
	@Test
	public void testCollectionRootCollection() throws SQLException {
		testClass(CollectionRootCollection.class, ArrayList.class);
	}

	@Test
	public void testCollectionRootList() throws SQLException {
		testClass(CollectionRootList.class, ArrayList.class);
	}

	@Test
	public void testCollectionRootSet() throws SQLException {
		testClass(CollectionRootSet.class, LinkedHashSet.class);
	}
	
	@Test
	public void testCollectionRootHashSet() throws SQLException {
		testClass(CollectionRootHashSet.class, HashSet.class);
	}

	@Test
	public void testCollectionRootMap() throws SQLException {
		testClass(CollectionRootMap.class, Map.class);
	}
	
	@Test
	public void testCollectionRootSortedSet() throws SQLException {
		testClass(CollectionRootSortedSet.class, SortedSet.class);
	}
	
	@Test
	public void testCollectionRootSortedMap() throws SQLException {
		testClass(CollectionRootSortedMap.class, SortedMap.class);
	}
	
	private Node<EntityDescriptor> createTreeForClass(Class<?> clasz) {
		SimpleEntityDescriptor rootEd = new SimpleEntityDescriptor.Builder(clasz).tableAlias("t").build();
		SimpleEntityDescriptor kidEd = new SimpleEntityDescriptor.Builder(Kid.class)
			.tableAlias("t1")
			.targetMember(ReflectionUtils.findField(clasz, "kids"))
			.build();
		Node<EntityDescriptor> root = new SimpleNode<>(rootEd);
		root.addChild(new SimpleNode<EntityDescriptor>(kidEd));
		return root;
	}
	
	
	private void testClass(Class<?> clasz, Class<?> expectedCollectionType) throws SQLException {
		Node<EntityDescriptor> root = createTreeForClass(clasz);
		
		EntityBuilder<CollectionRootCollection> eb = new EntityBuilder<>(root);
		eb.processRow(rs);
		Object rootInstance = eb.getEntity();
		Object kidsInstance = ReflectionTestUtils.getField(rootInstance, "kids");
		
		assertTrue(expectedCollectionType.isAssignableFrom(kidsInstance.getClass()));
	}

	@Table("root")
	public static class CollectionRootCollection {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		Collection<Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}
	
	@Table("root")
	public static class CollectionRootList {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		List<Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}

	@Table("root")
	public static class CollectionRootSet {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		Set<Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}

	@Table("root")
	public static class CollectionRootHashSet {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		Set<Kid> kids = new HashSet<>();

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}

	@Table("root")
	public static class CollectionRootMap {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		Map<Integer, Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}
	
	@Table("root")
	public static class CollectionRootSortedSet {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		SortedSet<Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}

	@Table("root")
	public static class CollectionRootSortedMap {
		
		@PkColumn("ParentId")
		int id;
		
		@Child(parentRelationType = RelationType.MANY_TO_ONE)
		SortedMap<Integer, Kid> kids;

		@Override
		public String toString() {
			return "Root [id=" + id + ", kid=" + kids + "]";
		}
	}
	
	
	
	@Table("kid")
	public static class Kid implements Comparable<Kid> {
		@PkColumn("KidId")
		int id;

		public Kid() {
		}

		public Kid(int id) {
			this.id = id;
		}

		@Override
		public int compareTo(Kid o) {
			return Integer.compare(this.id, o.id);
		}
	}
	
}
