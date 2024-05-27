package com.asentinel.common.util;

import java.lang.ref.Cleaner;

/**
 * Class that stores the library wide {@link Cleaner} instance.
 * 
 * @author Razvan Popian
 */
public final class CleanerHolder {
	public static final Cleaner CLEANER = Cleaner.create();
	
	private CleanerHolder() { }
}
