package com.asentinel.common.jdbc;

import java.lang.reflect.Constructor;

import org.springframework.util.ReflectionUtils;

import com.asentinel.common.util.Assert;

/**
 * Immutable default implementation of the {@link TypedObjectFactory} interface
 * that creates objects using the class specified on construction. The class
 * must have a no-args constructor. The access modifier of the constructor is
 * not relevant.
 *
 * @see ObjectFactory
 * @see TypedObjectFactory
 * @see ReflectionRowMapper
 * 
 * @author Razvan Popian
 */
public class DefaultObjectFactory<T> implements TypedObjectFactory<T> {
	private final Constructor<T> noArgsConstructor;
	
	/**
	 * @param clasz - the type of object that this factory will create. The class
	 * 					specified must have a no-args constructor. The access modifier 
	 * 					of the constructor is not relevant.
	 */
	public DefaultObjectFactory(Class<T> clasz) throws IllegalStateException {
		Assert.assertNotNull(clasz, "clasz");
		try {
			this.noArgsConstructor = clasz.getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Can not find a no-args constructor in class " + clasz.getName(), e);
		}
		ReflectionUtils.makeAccessible(noArgsConstructor);
	}
	
	/**
	 * @see ObjectFactory#newObject()
	 */
	@Override
	public T newObject() throws IllegalStateException {
		try {
			T object = noArgsConstructor.newInstance();
			return object;
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new IllegalStateException("Failed to create instance.", e);
		}
	}
	
	/**
	 * @return the {@code class} of objects this factory creates.
	 */
	@Override
	public Class<T> getType() {
		return noArgsConstructor.getDeclaringClass();
	}

	@Override
	public String toString() {
		return "DefaultObjectFactory [type=" + getType().getName() + "]";
	}

}
