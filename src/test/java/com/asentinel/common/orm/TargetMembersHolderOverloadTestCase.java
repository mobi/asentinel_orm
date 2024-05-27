package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class TargetMembersHolderOverloadTestCase {
	
	@Test
	public void test() {
		TargetMember pkm = TargetMembersHolder.getInstance().getTargetMembers(TestEntity.class).getPkColumnMember();
		assertEquals(int.class, pkm.getSetMethod().getParameterTypes()[0]);
		System.out.println(pkm.getSetMethod());
	}


	@SuppressWarnings("unused")
	@Table("test")
	private static class TestEntity {
		@PkColumn("id")
		int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
		
		public void setId(Integer id) {
			this.id = id;
		}

		public void setId(Long id) {
			this.id = id.intValue();
		}
		
		
	}
}
