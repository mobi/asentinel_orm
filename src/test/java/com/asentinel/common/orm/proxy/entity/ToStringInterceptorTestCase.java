package com.asentinel.common.orm.proxy.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

import static com.asentinel.common.orm.proxy.entity.ToStringInterceptor.*;

/**
 * Written to fix a bug. On versions 1.55.45 and prior the {@link #toStringUnloaded_String_Pk()}
 * test was failing.  
 * 
 * @author Razvan Popian
 */
public class ToStringInterceptorTestCase {
	
	private final ToStringInterceptor i = new ToStringInterceptor();
	
	@Test
	public void toStringUnloaded_Int_Pk() {
		IntPkClass proxy = new IntPkClass();
		String toStringValue = i.toStringUnloaded(proxy, () -> "Super.toString");
		System.out.println(toStringValue);
		assertEquals(
				String.format(PROXY, 
						proxy.getClass().getSuperclass().getName(), 
						EntityUtils.getEntityId(proxy)),
				toStringValue
		);
		
	}

	@Test
	public void toStringUnloaded_Long_Pk() {
		LongPkClass proxy = new LongPkClass();
		String toStringValue = i.toStringUnloaded(proxy, () -> "Super.toString");
		System.out.println(toStringValue);
		assertEquals(
				String.format(PROXY, 
						proxy.getClass().getSuperclass().getName(), 
						EntityUtils.getEntityId(proxy)),
				toStringValue
		);
		
	}


	@Test
	public void toStringUnloaded_String_Pk() {
		StringPkClass proxy = new StringPkClass();
		String toStringValue = i.toStringUnloaded(proxy, () -> "Super.toString");
		System.out.println(toStringValue);
		assertEquals(
				String.format(PROXY, 
						proxy.getClass().getSuperclass().getName(), 
						EntityUtils.getEntityId(proxy)),
				toStringValue
		);
	}

	@Test
	public void toStringUnloaded_String_Pk_Null() {
		StringPkClass proxy = new StringPkClass();
		proxy.setPk(null);
		String toStringValue = i.toStringUnloaded(proxy, () -> "Super.toString");
		System.out.println(toStringValue);
		assertEquals(
				String.format(PROXY, 
						proxy.getClass().getSuperclass().getName(), 
						EntityUtils.getEntityId(proxy)),
				toStringValue
		);
	}
	
	
	@Table("StringPkTable")
	private static class StringPkClass {
		@PkColumn("StringPk")
		private String pk = "stringId";

		void setPk(String pk) {
			this.pk = pk;
		}
	}
	
	@Table("IntPkTable")
	private static class IntPkClass {
		@PkColumn("IntPk")
		private int pk = 10;
	}

	@Table("LongPkTable")
	private static class LongPkClass {
		@PkColumn("LongPk")
		private int pk = 11;
	}

}
