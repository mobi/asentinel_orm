package com.asentinel.common.orm.proxy.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

/**
 * Test written while fixing APM-12800. For covariant return types the proxy loading was triggered for
 * id access or a StackOverflow error was thrown.
 * 
 * @author Razvan Popian
 */
public class ProxyCovariantIdTestCase {
	
	ProxyFactory pf = ProxyFactory.getInstance();
	
	@Test
	public void annotatedField() {
		BasicUserField u = pf.newProxy(BasicUserField.class, id -> new BasicUserField(10, "test"));
		u.getUserId();
		assertFalse(EntityUtils.isLoadedProxy(u));
		u.setUserId(11);
		assertFalse(EntityUtils.isLoadedProxy(u));
		u.getUsername();
		assertTrue(EntityUtils.isLoadedProxy(u));
	}
	
	@Test
	public void annotatedMethod() {
		BasicUserMethod u = pf.newProxy(BasicUserMethod.class, id -> new BasicUserMethod(10, "test"));
		u.getUserId();
		assertFalse(EntityUtils.isLoadedProxy(u));
		u.setUserId(11);
		assertFalse(EntityUtils.isLoadedProxy(u));
		u.getUsername();
		assertTrue(EntityUtils.isLoadedProxy(u));
	}
	

	interface User {

		Object getUserId();

		String getUsername();

	}
	
	// FYI: The following classes need to be public for ByteBuddy 1.9.2 if the class loading strategy is not ClassLoadingStrategy.Default.INJECTION. 
	// For 1.5.10 they can be declared as package private (this is ideal, because I don't want them to be visible outside the package). 
	// I opened a stackoverflow question on this issue: 
	// https://stackoverflow.com/questions/52829988/behavior-change-on-bytebuddy-version-1-9-1-compared-to-1-5-10

	@Table("User")
	public static class BasicUserField implements User {

		@PkColumn("UserId")
		private int userId;

		private String username;

		public BasicUserField() {	
		}

		public BasicUserField(int userId, String username) { 
			this.userId = userId; 
			this.username = username; 
		}

		@Override
		public Integer getUserId() { 
			return userId;
		}
		

		public void setUserId(int userId) { 
			this.userId = userId;
		}

		@Override
		public String getUsername() {
			return username;
		}
	}
	
	@Table("User")
	public static class BasicUserMethod implements User {

		private int userId;

		private String username;

		public BasicUserMethod() {	
		}

		public BasicUserMethod(int userId, String username) { 
			this.userId = userId; 
			this.username = username; 
		}

		@Override
		public Integer getUserId() { 
			return userId;
		}
		

		@PkColumn("UserId")
		public void setUserId(int userId) { 
			this.userId = userId;
		}

		@Override
		public String getUsername() {
			return username;
		}
	}
}
