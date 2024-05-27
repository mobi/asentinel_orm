package com.asentinel.common.orm.proxy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.jdbc.DefaultObjectFactory;
import com.asentinel.common.orm.proxy.collection.CollectionProxyFactory;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.ConcurrentCache;

import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.FieldPersistence;
import net.bytebuddy.description.modifier.ModifierContributor;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

/**
 * Base class for classes that are proxy factories.
 * <br><br>
 * This class should not be used directly by any client code.
 * 
 * @see ProxyFactory
 * @see CollectionProxyFactory
 * 
 * @author Razvan Popian
 */
public abstract class ProxyFactorySupport {
	private final static Logger log = LoggerFactory.getLogger(ProxyFactorySupport.class);
	
	/**
	 * Must match {@link AbstractLazyLoadInterceptor#loadProxy(Object, java.util.concurrent.Callable)} 
	 * method name.
	 */
	protected static final String INTERCEPTOR_METHOD_NAME = "loadProxy";

	/**
	 * Must match {@link AbstractToStringInterceptor#toStringInterceptor(Object, java.util.concurrent.Callable)} 
	 * method name.
	 */
	protected static final String TO_STRING_INTERCEPTOR_METHOD_NAME = "toStringInterceptor"; 

	protected static final String LOADER_FIELD_NAME = "com$asentinel$common$orm$proxy$loader";
	
	protected static final ModifierContributor.ForField[] HELPER_FIELD_MODIFIERS = {
		Visibility.PUBLIC, 
		FieldManifestation.VOLATILE,
		FieldPersistence.TRANSIENT
	};
	
	protected static final String PROXY_CLASS_NAME_SUFFIX = "$com$asentinel$common$orm$Proxy";
	

	protected final ConcurrentCache<Class<?>, DefaultObjectFactory<?>> cache = new ConcurrentCache<>();

	protected abstract <T> DefaultObjectFactory<? extends T> getProxyObjectFactoryInternal(Class<T> clazz);
	
	public final <T> DefaultObjectFactory<? extends T> getProxyObjectFactory(Class<T> clazz) {
		Assert.assertNotNull(clazz, "clazz");
		Assert.assertFalse(Proxy.class.isAssignableFrom(clazz), "The class " + clazz.getName() + " is already a proxy. "
				+ "Proxying a proxy is not allowed.");
		int cacheSize0 = cache.getSize();
		
		DefaultObjectFactory<? extends T> objectFactory = getProxyObjectFactoryInternal(clazz);
		
		int cacheSize1 = cache.getSize();
		if (log.isTraceEnabled()) {
			log.trace(String.format("getProxyObjectFactory - Cache (" + this.getClass().getSimpleName() + ") size before/after call: %d/%d", cacheSize0, cacheSize1));
		}
		return objectFactory;
	}
	
	protected Long getSerialVersionUid(Class<?> clazz) {
		if (!Serializable.class.isAssignableFrom(clazz)) {
			return null;
		}
		
		Field field = ReflectionUtils.findField(clazz, "serialVersionUID");
		if (field == null) {
			log.warn("getSerialVersionUid - Class " + clazz.getName() + " is serializable, but does not declare a serialVersionUID. "
					+ "Proxy deserialization will not work.");
			return null;
		}
		ReflectionUtils.makeAccessible(field);
		return (Long) ReflectionUtils.getField(field, clazz);
	}
	
	public final int getCacheSize() {
		return cache.getSize();
	}
	
	public static Field findLoaderField(Class<?> proxyClass) {
		Assert.assertNotNull(proxyClass, "proxyClass");
		Field fieldLoader = ReflectionUtils.findField(proxyClass, LOADER_FIELD_NAME);
		if (fieldLoader == null) {
			throw new IllegalArgumentException("Can not find the loader field in class " + proxyClass.getName());
		}
		return fieldLoader;
	}

	
	protected static void checkForFinalMethods(Class<?> clazz) {
		// look for final methods and warn the user
		StringBuilder finalMethods = new StringBuilder();
		ReflectionUtils.doWithMethods(clazz, m -> {
			if (m.getDeclaringClass() != Object.class
					&& !Modifier.isStatic(m.getModifiers())
					&& Modifier.isFinal(m.getModifiers())) {
				if (finalMethods.length() != 0) {
					finalMethods.append("\n");
				}
				finalMethods.append("\t").append(m);
			}
		});
		if (finalMethods.length() > 0) {
			log.warn("checkForFinalMethods - Class " + clazz.getName() + " has the following final methods:\n" + finalMethods);
			log.warn("checkForFinalMethods - These methods can not be intercepted and therefore will not trigger the lazy load.");
		}
	}
	

	protected static DefaultObjectFactory<?> newObjectFactory(Builder<?> byteBuddyBuilder, Class<?> clasz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException {
		ClassLoadingStrategy<ClassLoader> strategy;

		if (ClassInjector.UsingLookup.isAvailable()) {
			Class<?> methodHandles = Class.forName("java.lang.invoke.MethodHandles");
			Object lookup = methodHandles.getMethod("lookup").invoke(null);
			
			Method privateLookupIn = methodHandles.getMethod("privateLookupIn", Class.class,
					Class.forName("java.lang.invoke.MethodHandles$Lookup"));
			Object privateLookup = privateLookupIn.invoke(null, clasz, lookup);
			
			strategy = ClassLoadingStrategy.UsingLookup.of(privateLookup);
		} else if (ClassInjector.UsingReflection.isAvailable()) {
			strategy = ClassLoadingStrategy.Default.INJECTION;
		} else {
			throw new IllegalStateException("No code generation strategy available");
		}

		return new DefaultObjectFactory<>(
				byteBuddyBuilder
				.make()
				.load(clasz.getClassLoader(), strategy)
				.getLoaded());
	}

}
