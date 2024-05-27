package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class TargetMembersHolderTestCase {
	
	@Test
	public void testCache() {
		TargetMembers tms0 = TargetMembersHolder.getInstance().getTargetMembers(EmptyBean.class);
		TargetMembers tms1 = TargetMembersHolder.getInstance().getTargetMembers(Bean.class);
		TargetMembers tms2 = TargetMembersHolder.getInstance().getTargetMembers(PkColumn_Column_Child_Bean.class);
		TargetMembers tms3 = TargetMembersHolder.getInstance().getTargetMembers(Column_Child_Bean.class);
		
		assertSame(tms0, TargetMembersHolder.getInstance().getTargetMembers(EmptyBean.class));
		assertSame(tms1, TargetMembersHolder.getInstance().getTargetMembers(Bean.class));
		assertSame(tms2, TargetMembersHolder.getInstance().getTargetMembers(PkColumn_Column_Child_Bean.class));
		assertSame(tms3, TargetMembersHolder.getInstance().getTargetMembers(Column_Child_Bean.class));
	}
	
	@Test
	public void testEmptyBean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(EmptyBean.class);
		
		assertNull(tms.getTableAnnotation());
		assertNull(tms.getPkColumnMember());
		assertEquals(0, tms.getColumnMembers().size());
		assertEquals(0, tms.getChildMembers().size());
	}

	@Test
	public void testNormalOperation() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(Bean.class);
		
		assertNotNull(tms.getTableAnnotation());
		
		TargetMember pk = tms.getPkColumnMember();
		assertTrue(pk.getAnnotatedElement() instanceof Field);
		assertEquals("id", ((PkColumn) pk.getAnnotation()).value());
		assertNull(pk.getGetMethod());
		
		List<TargetMember> cols = tms.getColumnMembers();
		assertEquals(2, cols.size());
		assertTrue(cols.get(0).getAnnotatedElement() instanceof Field);
		assertTrue(cols.get(1).getAnnotatedElement() instanceof Method);
		assertNotNull(cols.get(1).getGetMethod());
		
		List<TargetChildMember> children = tms.getChildMembers();
		assertEquals(2, children.size());
		assertTrue(children.get(0).getAnnotatedElement() instanceof Field);
		assertTrue(children.get(1).getAnnotatedElement() instanceof Method);
	}

	@Test
	public void testMultiplePkColumn() {
		try {
			TargetMembersHolder.getInstance().getTargetMembers(MultiplePkBean.class);
			fail("Should not get to this line. Exception expected.");
		} catch (IllegalStateException e) {
			
		}
	}
	
	@Test
	public void testPkColumn_Column_Child_Bean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(PkColumn_Column_Child_Bean.class);
		
		assertNull(tms.getTableAnnotation());
		assertNotNull(tms.getPkColumnMember());
		assertEquals(0, tms.getColumnMembers().size());
		assertEquals(0, tms.getChildMembers().size());
	}
	
	@Test
	public void testColumn_Child_Bean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(Column_Child_Bean.class);
		
		assertNull(tms.getTableAnnotation());
		assertNull(tms.getPkColumnMember());
		assertEquals(1, tms.getColumnMembers().size());
		assertEquals(0, tms.getChildMembers().size());
	}

	@Test
	public void testGetterForPk() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(GetterForPk.class);
		assertNotNull(tms.getPkColumnMember());
		TargetMember tm = tms.getPkColumnMember();
		assertNotNull(tm.getAnnotatedElement());
		assertNotNull(tm.getAnnotation());
		assertNotNull(tm.getGetMethod());
	}

	@Test
	public void testNoGetterForPk() {
		try {
			TargetMembersHolder.getInstance().getTargetMembers(NoGetterForPk.class);			
			fail("Should throw exception here");
		} catch(IllegalArgumentException e) {
			
		}
	}

	@Test
	public void testInvalidGetterForPk() {
		try {
			TargetMembersHolder.getInstance().getTargetMembers(InvalidGetterForPk.class);			
			fail("Should throw exception here");
		} catch(IllegalArgumentException e) {
			
		}
	}

	@Test
	public void testInvalidGetterForPk2() {
		try {
			TargetMembersHolder.getInstance().getTargetMembers(InvalidGetterForPk2.class);			
			fail("Should throw exception here");
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	@Test
	public void testBeanWithoutGetter() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(BeanWithoutGetter.class);
		assertNotNull(tms.getPkColumnMember());
		TargetMember tm = tms.getPkColumnMember();
		assertNotNull(tm.getAnnotatedElement());
		assertNotNull(tm.getAnnotation());
		assertNull(tm.getGetMethod());
		
		List<TargetMember> list = tms.getColumnMembers();
		assertEquals(1, list.size());
		assertNotNull(list.get(0).getAnnotatedElement());
		assertNull(list.get(0).getGetMethod());
	}

	@Test
	public void testBeanWithChildWithoutGetter() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(BeanWithChildWithoutGetter.class);
		assertNotNull(tms.getPkColumnMember());
		TargetMember tm = tms.getPkColumnMember();
		assertNotNull(tm.getAnnotatedElement());
		assertNotNull(tm.getAnnotation());
		assertNull(tm.getGetMethod());
		
		List<TargetChildMember> list = tms.getChildMembers();
		assertEquals(1, list.size());
		assertNotNull(list.get(0).getAnnotatedElement());
		assertNotNull(list.get(0).getSetMethod());
		assertNull(list.get(0).getGetMethod());
		assertEquals(String.class, list.get(0).getChildType());
	}

	@Test
	public void testBeanWithChildWithGetter() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(BeanWithChildWithGetter.class);
		assertNotNull(tms.getPkColumnMember());
		TargetMember tm = tms.getPkColumnMember();
		assertNotNull(tm.getAnnotatedElement());
		assertNotNull(tm.getAnnotation());
		assertNull(tm.getGetMethod());
		
		List<TargetChildMember> list = tms.getChildMembers();
		assertEquals(1, list.size());
		assertNotNull(list.get(0).getAnnotatedElement());
		assertNotNull(list.get(0).getSetMethod());
		assertNotNull(list.get(0).getGetMethod());
		assertEquals(String.class, list.get(0).getChildType());
	}
	
}

class EmptyBean {
	
}

@Table("table")
class Bean {
	
	@Column("name2")
	public void setName2(String s) {
		
	}
	
	public String getName2() {
		return null;
	}
	
	@Child
	public void setChild2(String s) {
		
	}
	
	@PkColumn("id")
	private int pk;
	
	@Column("name")
	private String name;
	
	@Child
	private String child;
}

@Table("table")
class MultiplePkBean {
	
	@PkColumn("id2")
	public void setPkColumn(int id2) {
		
	}
	
	@Column("name2")
	public void setName2(String s) {
		
	}
	
	public String getName2() {
		return "";
	}
	
	@Child
	public void setChild2(String s) {
		
	}
	
	@PkColumn("id")
	private int pk;
	
	@Column("name")
	private String name;
	
	@Child
	private String child;
}


class PkColumn_Column_Child_Bean{
	
	@PkColumn("id")
	@Column("name")
	@Child
	private int id;
}

class Column_Child_Bean{
	
	@Column("name")
	@Child
	private int id;
}

class GetterForPk {
	
	@PkColumn("int")
	public void setPk(int i) {
		
	}
	
	public int getPk() {
		return 0;
	}
}

class NoGetterForPk {
	
	@PkColumn("int")
	public void setPk(int i) {
		
	}
}

class InvalidGetterForPk {
	
	@PkColumn("int")
	public void setPk(int i) {
		
	}
	
	public int getPKa() {
		return 0;
	}
}

class InvalidGetterForPk2 {
	
	@PkColumn("int")
	public void setPk(int i) {
		
	}
	
	public void getPK() {
	}
}


@Table("table")
class BeanWithoutGetter {
	
	@PkColumn("id")
	private int id;
	
	@Column("col")
	public void setValue(String s) {
		
	}
}

@Table("table")
class BeanWithChildWithoutGetter {
	
	@PkColumn("id")
	private int id;
	
	@Child
	public void setValue(String s) {
		
	}
}

class BeanWithChildWithGetter extends BeanWithChildWithoutGetter {
	
	public String getValue() {
		return null;
	}
}