package com.asentinel.common.jdbc;

/**
 * Simple wrapper class for a ThreadLocal containing the user associated 
 * with the current thread. In the web app environment the user is set
 * by the ThreadLocalFilter class. In other environments the user needs  
 * to set this programmatically for each started thread if sql logging 
 * with user is required.
 * 
 * @author Razvan Popian
 */
public final class ThreadLocalUser {
	
	private ThreadLocalUser(){}

	private static final ThreadLocal<SimpleUser> threadLocalUser = new InheritableThreadLocal<SimpleUser>();
	
	public static ThreadLocal<SimpleUser> getThreadLocalUser() {
		return threadLocalUser;
	}
}
