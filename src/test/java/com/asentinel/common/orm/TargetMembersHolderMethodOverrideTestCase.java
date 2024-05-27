package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class TargetMembersHolderMethodOverrideTestCase {
	
	@Test
	public void testBaseBean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(BaseBean.class);
		
		assertNotNull(tms.getTableAnnotation());
		
		TargetMember pk = tms.getPkColumnMember();
		assertTrue(pk.getAnnotatedElement() instanceof Method);
		assertEquals("id", ((PkColumn) pk.getAnnotation()).value());
		assertNotNull(pk.getGetMethod());
		
		List<TargetMember> cols = tms.getColumnMembers();
		assertEquals(1, cols.size());
		assertTrue(cols.get(0).getAnnotatedElement() instanceof Method);
		assertNotNull(cols.get(0).getGetMethod());
		
		List<TargetChildMember> children = tms.getChildMembers();
		assertEquals(1, children.size());
		assertTrue(children.get(0).getAnnotatedElement() instanceof Method);
		Child childAnn = (Child) children.get(0).getAnnotation();
		assertEquals(String.class, childAnn.type());
	}

	@Test
	public void testAllOverrideBean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(AllOverrideNoAnnotationsBean.class);
		
		TargetMember pk = tms.getPkColumnMember();
		assertTrue(pk.getAnnotatedElement() instanceof Method);
		assertEquals("id", ((PkColumn) pk.getAnnotation()).value());
		assertNotNull(pk.getGetMethod());
		
		List<TargetMember> cols = tms.getColumnMembers();
		assertEquals(1, cols.size());
		assertTrue(cols.get(0).getAnnotatedElement() instanceof Method);
		assertNotNull(cols.get(0).getGetMethod());
		
		List<TargetChildMember> children = tms.getChildMembers();
		assertEquals(1, children.size());
		assertTrue(children.get(0).getAnnotatedElement() instanceof Method);
		Child childAnn = (Child) children.get(0).getAnnotation();
		assertEquals(String.class, childAnn.type());
	}

	@Test
	public void testAllOverrideWithAnnotationsBean() {
		TargetMembers tms = TargetMembersHolder.getInstance().getTargetMembers(AllOverrideWithAnnotationsBean.class);

		assertNotNull(tms.getTableAnnotation());
		
		TargetMember pk = tms.getPkColumnMember();
		assertTrue(pk.getAnnotatedElement() instanceof Method);
		assertEquals("id2", ((PkColumn) pk.getAnnotation()).value());
		assertNotNull(pk.getGetMethod());
		
		List<TargetMember> cols = tms.getColumnMembers();
		assertEquals(1, cols.size());
		assertTrue(cols.get(0).getAnnotatedElement() instanceof Method);
		assertNotNull(cols.get(0).getGetMethod());
		Column colAnn = (Column) cols.get(0).getAnnotation();
		assertEquals("column2", colAnn.value());
		
		List<TargetChildMember> children = tms.getChildMembers();
		assertEquals(1, children.size());
		assertTrue(children.get(0).getAnnotatedElement() instanceof Method);
		assertNotNull(cols.get(0).getGetMethod());
		Child childAnn = (Child) children.get(0).getAnnotation();
		assertEquals(StringBuilder.class, childAnn.type());
		
	}
	
}

@Table("table")
class BaseBean {
	
	@PkColumn("id")
	public void setPk(String s) {}
	
	public String getPk() {
		return null;
	}
	
	@Column("column")
	public void setValue(String s) {}
	
	public String getValue() {
		return null;
	}
	
	@Child(type = String.class)
	protected void setChild(CharSequence s) { }
}

class AllOverrideNoAnnotationsBean extends BaseBean {
	
	@Override
	public void setPk(String s) {}
	
	@Override
	public void setValue(String s) {}
	
	@Override
	public String getValue() {
		return null;
	}
	
	@Override
	protected void setChild(CharSequence s) { }
}

class AllOverrideWithAnnotationsBean extends BaseBean {
	
	@PkColumn("id2")
	@Override
	public void setPk(String s) {}
	
	@Column("column2")
	@Override
	public void setValue(String s) {}
	
	@Child(type = StringBuilder.class)
	@Override
	protected void setChild(CharSequence s) { }
}

