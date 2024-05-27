package com.asentinel.common.orm.proxy;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import org.springframework.util.ReflectionUtils;

/**
 * Interceptor for {@code Object#toString()}. The intercepting method
 * is {@link #toStringInterceptor(Object, Callable)}, the name of this method matches 
 * the {@link ProxyFactorySupport#TO_STRING_INTERCEPTOR_METHOD_NAME}.
 * <br>
 * Note that the lazy loading logic in {@code AbstractLazyLoadInterceptor} ensures the visibility of the target instance state (fields)
 * in a multithreaded environment by synchronizing on the proxy implicit lock. The {@code #toStringInterceptor(Object, Callable)}
 * method synchronizes on the same lock to ensure it sees the correct state of the proxy.
 * <br><br>
 * This class should not be used directly by any client code. 
 * 
 * @see AbstractLazyLoadInterceptor
 * @see ProxyFactorySupport
 * 
 * @author Razvan Popian
 */
public abstract class AbstractToStringInterceptor {

	protected AbstractToStringInterceptor() { }
	
	protected abstract String toStringUnloaded(Object proxy, Callable<?> zuper);
	
	protected abstract String toStringLoaded(Object proxy, Callable<?> zuper) throws Exception;
	
	
	public String toStringInterceptor(@This Object proxy, @SuperCall Callable<?> zuper) {
		Field fieldLoader = ProxyFactorySupport.findLoaderField(proxy.getClass());
		// all operations on the target are guarded by the proxy implicit lock, see also AbstractLazyLoadInterceptor
		synchronized (proxy) {
			Object loader = ReflectionUtils.getField(fieldLoader, proxy);
			if (loader == null) {
				try {
					return toStringLoaded(proxy, zuper);
				} catch (Exception e) {
					throw new RuntimeException("Failed to invoke the target toString method.", e);
				}
			} else {
				return toStringUnloaded(proxy, zuper);
			}
		}
	}
	
	
}
