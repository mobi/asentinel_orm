package com.asentinel.common.orm.mappers.dynamic;

import java.util.Arrays;

import com.asentinel.common.orm.EntityDescriptorNodeCallback;

/**
 * @see DynamicColumnsEntityNodeCallback
 * @see DynamicColumnsEntityNodeCallback#addDynamicColumnEntityOverride(DynamicColumn, DynamicColumnEntityOverride)
 * 
 * @since 1.66.0
 * @author Razvan Popian
 */
public class DynamicColumnEntityOverride {
	private final EntityDescriptorNodeCallback rootCallback;
	private final EntityDescriptorNodeCallback[] callbacks;
	
	public DynamicColumnEntityOverride(EntityDescriptorNodeCallback rootCallback, EntityDescriptorNodeCallback ... callbacks) {
		this.rootCallback = rootCallback;
		if (callbacks == null) {
			callbacks = new EntityDescriptorNodeCallback[0];
		}
		this.callbacks = callbacks;
	}

	public EntityDescriptorNodeCallback getRootCallback() {
		return rootCallback;
	}
	
	public EntityDescriptorNodeCallback[] getCallbacks() {
		return callbacks;
	}

	@Override
	public String toString() {
		return "DynamicColumnEntityOverrideData [rootCallback=" + rootCallback + ", callbacks="
				+ Arrays.toString(callbacks) + "]";
	}
	
}
