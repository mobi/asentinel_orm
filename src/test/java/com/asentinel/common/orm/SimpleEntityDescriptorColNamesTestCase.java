package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class SimpleEntityDescriptorColNamesTestCase {
	
	@Test
	public void testA() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(A.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name")), cols);
	}

	@Test
	public void testB() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(B.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "xid")), cols);
	}
	
	
	@Test
	public void testC() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(C.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "xid")), cols);
	}
	
	@Test
	public void testD() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(D.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "xid", "x2id")), cols);
	}
	
	@Test
	public void testE_ensure_no_duplicated_columns() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(E.class);
		assertEquals("A", ed.getTableName());
		Map<String, List<String>> map = ed.getColumnNames()
				.stream().collect(Collectors.groupingBy(String::toLowerCase));
		for (List<String> cols: map.values()) {
			assertEquals(1, cols.size());
		}
	}
	
	@Test
	public void testF_if_joinConditionsOverride_fk_not_selected() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(F.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "xid", "x2id")), cols);
	}

	@Test
	public void testF_if_pk_specified_fk_not_selected() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor(G.class);
		assertEquals("A", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "xid", "x2id", "OverridePKId")), cols);
	}
	
	@Test
	public void inputStreamLazy() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor.Builder(InputStreamWrapper.class)
					.queryExecutor(mock(SqlQuery.class))
					.build();
		assertEquals("test", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name")), cols);
	}
	

	@Test
	public void inputStreamEager() {
		SimpleEntityDescriptor ed = new SimpleEntityDescriptor.Builder(InputStreamWrapper.class)
					.queryExecutor(null) // this is null by default, but it's the key setting for eager streams, that's why I am explicitly setting it null
					.build();
		assertEquals("test", ed.getTableName());
		Set<String> cols = new HashSet<>(ed.getColumnNames());
		assertEquals(new HashSet<>(Arrays.asList("id", "name", "bytes")), cols);
	}


	@Table("C")
	private static class X {
		@PkColumn("xid")
		int id;
	}
	
	
	@Table("A")
	private static class A {
		@PkColumn("id")
		int id;
		
		@Column("name")
		String name;
	}
		
	private static class B extends A {
		
		@Child
		private X x;
	}
	
	private static class C extends B {
		
		@Column("xid")
		private int xid;
	}

	private static class D extends C {
		
		@Child(fkName = "x2id")
		private X x2;
	}
	
	private static class E extends D {
		
		@Column("XID")
		int xId;
		
	}

	private static class F extends D {
		
		@Child(fkName = "x3Id", parentAvailableFk = false)
		X x3;
		
	}

	private static class G extends D {
		
		@Child(pkName = "OverridePKId")
		X x3;
		
	}
	
	@Table("test")
	private static class InputStreamWrapper {
		@PkColumn("id")
		int id;
		
		@Column("name")
		String name;
		
		@Column("bytes")
		InputStream bytes;
	}
	
}
