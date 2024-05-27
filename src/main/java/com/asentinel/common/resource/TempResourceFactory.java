package com.asentinel.common.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asentinel.common.util.Assert;

/**
 * Factory for temporary resources. Depending on how 
 * it is configured, on construction this factory
 * can create 2 types of resources: <br>
 *	- {@link ByteArrayTempResource}, this is the default<br>
 *	- {@link FileTempResource} <br>
 *
 * @author Razvan Popian
 */
public class TempResourceFactory {
	private static final Logger log = LoggerFactory.getLogger(TempResourceFactory.class);
	
	private final TempResourceType type;

	public TempResourceFactory() {
		this(TempResourceType.BYTE_ARRAY);
	}
	
	public TempResourceFactory(TempResourceType type) {
		Assert.assertNotNull(type, "type");
		this.type = type;
	}
	
	public TempResource createTempResource() {
		TempResource resource;
		switch (type) {
			case BYTE_ARRAY:
				resource = new ByteArrayTempResource();
				break;
			case TEMP_FILE:
				resource = new FileTempResource();
				break;
			default:
				throw new IllegalStateException("Unknown resource type.");
		}
		log.debug("createTempResource - created resource " + resource);
		return resource;
		
	}

	public TempResourceType getType() {
		return type;
	}
	
	public enum TempResourceType {
		BYTE_ARRAY,
		TEMP_FILE
	}
	
}
