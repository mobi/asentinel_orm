package com.asentinel.common.orm.proxy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Base class that serves as a template for the lazy loading algorithm. The intercepting method called by ByteBuddy
 * is {@link #loadProxy(Object, Callable)}, the name of this method matches the {@link ProxyFactorySupport#INTERCEPTOR_METHOD_NAME}.
 * An important aspect is that final methods can not be intercepted, therefore they will not trigger the lazy loading. Avoid using final
 * methods in classes that may be proxied.
 * <br>
 * Note that the lazy loading logic assures the visibility of the target instance state (fields)
 * in a multithreaded environment by synchronizing on the proxy implicit lock.
 * <br><br>
 * This class should not be used directly by any client code.
 * 
 * @see ProxyFactorySupport
 * @see AbstractToStringInterceptor
 * 
 * @author Razvan Popian
 */
public abstract class AbstractLazyLoadInterceptor<T> {
	private final static Logger log = LoggerFactory.getLogger(AbstractLazyLoadInterceptor.class);
	
	protected AbstractLazyLoadInterceptor() { }
	
	/**
	 * @return {@code true} if a method call should bypass the lazy loading logic and it should
	 * 		be routed directly to the target instance, {@code false} otherwise. A method call can bypass
	 * 		the lazy loading logic if it is actually called from the lazy loading logic. This is to avoid 
	 * 		an infinite loop and a stack overflow error ;).
	 * 
	 * @see #loadProxy(Object, Callable)
	 */
	protected boolean isRouteDirectlyToTarget() {
		return false;
	}
	
	/**
	 * Important note: when this gets called the default lock of the proxy has been already acquired.
	 */
	protected abstract T load(Function<Object, T> loader, Object proxy, List<Field> toBeDiscarded);

	/**
	 * Important note: when this gets called the default lock of the proxy has been already acquired.
	 */
	protected abstract void copyState(T source, Object proxy);

	/**
	 * Important note: when this gets called the default lock of the proxy has been already acquired.
	 */
	protected void discardHelperFields(Object proxy, List<Field> toBeDiscarded) {
		for (Field field: toBeDiscarded) {
			ReflectionUtils.setField(field, proxy, null);
		}
	}
	
	@RuntimeType
	public final Object loadProxy(@This Object proxy, @SuperCall Callable<?> zuper) {
		if (isRouteDirectlyToTarget()) {
			return superCall(zuper);
		}
		Field fieldLoaded = ProxyFactorySupport.findLoaderField(proxy.getClass());
		// all operations on the target are guarded by the proxy implicit lock, see also AbstractToStringInterceptor
		synchronized (proxy) {
			@SuppressWarnings("unchecked")
			Function<Object, T> loader = (Function<Object, T>) ReflectionUtils.getField(fieldLoaded, proxy);
			if (loader != null) {
				List<Field> toBeDiscarded = new ArrayList<>(2);
				T source = load(loader, proxy, toBeDiscarded);
				copyState(source, proxy);
				ReflectionUtils.setField(fieldLoaded, proxy, null);
				discardHelperFields(proxy, toBeDiscarded);
				if (log.isDebugEnabled()) {
					log.debug("loadProxy - Successfully copied the target state into the proxy.");
				}
			}
			return superCall(zuper);
		}
	}
	
	
	private static Object superCall(Callable<?> zuper) {
		try {
			return zuper.call();
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke the target method.", e);
		}
	}

	
}
