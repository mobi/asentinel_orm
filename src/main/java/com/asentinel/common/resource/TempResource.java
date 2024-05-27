package com.asentinel.common.resource;

import org.springframework.core.io.WritableResource;

/**
 * Interface to be implemented by temporary resources. Adds the 
 * {@link #cleanup()} method to the methods provided by the {@link WritableResource}
 * interface.<br>
 * It extends {@link AutoCloseable} so implementations can be used in try-with-resources
 * constructs.
 * 
 * @author Razvan Popian
 */
public interface TempResource extends WritableResource, AutoCloseable {
	
	/**
	 * Method to be called to cleanup (delete) a temporary
	 * resource after it is no longer needed. It should be called
	 * in a <code>finally</code> block. 
	 */
	public void cleanup();
	
	@Override
	public default void close() {
		cleanup();
	}

}
