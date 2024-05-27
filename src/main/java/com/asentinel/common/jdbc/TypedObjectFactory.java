package com.asentinel.common.jdbc;

/**
 * {@code ObjectFactory} extension that adds the {@link #getType()} method that
 * can be used by clients to determine the type of object that the
 * {@link #newObject()} method will create.
 * 
 * @see ObjectFactory
 * @see DefaultObjectFactory
 * @see TypedObjectFactorySupport
 * 
 * @author Razvan.Popian
 */
public interface TypedObjectFactory<T> extends ObjectFactory<T> {

	/**
	 * Note that this method calls {@link #newObject()} to determine the type, this
	 * is inefficient, an instance is created and discarded. So this method must be
	 * overridden in implementations.
	 * 
	 * @return the type of object created by {@link #newObject()}.
	 */
	@SuppressWarnings("unchecked")
	default Class<T> getType() {
		return (Class<T>) newObject().getClass();
	}

}
