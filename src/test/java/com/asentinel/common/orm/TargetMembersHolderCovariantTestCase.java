package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class TargetMembersHolderCovariantTestCase {
	
	@Test
	public void mostSpecificGetterIsSelectedAnnotatedMethod() {
		TargetMember pkm = TargetMembersHolder.getInstance().getTargetMembers(IntegerIdentityMethod.class).getPkColumnMember();
		assertEquals(Integer.class, pkm.getGetMethod().getReturnType());
	}

	@Test
	public void mostSpecificGetterIsSelectedAnnotatedField() {
		TargetMember pkm = TargetMembersHolder.getInstance().getTargetMembers(IntegerIdentityField.class).getPkColumnMember();
		assertEquals(Integer.class, pkm.getGetMethod().getReturnType());
	}
	
	
	private static interface Identity {
		public Object getId();
	}
	
	@Table("test")
	private static class NumericIdentityMethod implements Identity {
		
		protected int id;

		@PkColumn("id")
		public void setId(int id) {
			this.id = id;
		}

		@Override
		public Number getId() {
			return id;
		}
	}
	
	@Table("test")
	private static class IntegerIdentityMethod extends NumericIdentityMethod {
		
		@Override
		public Integer getId() {
			return id;
		}
	}

	
	// ----------------------------------- //
	
	
	@Table("test")
	private static class NumericIdentityField implements Identity {

		@PkColumn("id")
		protected int id;
		
		@SuppressWarnings("unused")
		public void setId(int id) {
			this.id = id;
		}

		@Override
		public Number getId() {
			return id;
		}
	}
	
	@Table("test")
	private static class IntegerIdentityField extends NumericIdentityMethod {
		
		@Override
		public Integer getId() {
			return id;
		}
	}
	
}
