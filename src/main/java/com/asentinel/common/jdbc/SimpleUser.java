package com.asentinel.common.jdbc;

/**
 * Simple interface for an object that represents a user.
 *
 * @see ThreadLocalUser
 * 
 * @author Razvan Popian
 */
public interface SimpleUser {
	
	Object getUserId();
	
	String getUsername();
}
