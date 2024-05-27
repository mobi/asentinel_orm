package com.asentinel.common.jdbc;

import com.asentinel.common.util.Assert;

/**
 * {@link TypedObjectFactory} implementation intended to be extended for
 * creating objects of the type specified on construction.
 * 
 * @author Razvan.Popian
 */
public abstract class TypedObjectFactorySupport<T> implements TypedObjectFactory<T> {
	private final Class<T> type;

	protected TypedObjectFactorySupport(Class<T> type) {
		Assert.assertNotNull(type, "type");
		this.type = type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}
}
