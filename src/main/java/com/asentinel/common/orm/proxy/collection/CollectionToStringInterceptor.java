package com.asentinel.common.orm.proxy.collection;

import java.util.concurrent.Callable;

import com.asentinel.common.orm.proxy.AbstractToStringInterceptor;
import com.asentinel.common.orm.proxy.entity.LazyLoadInterceptor;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

/**
 * Interceptor for {@code Object#toString()}.
 * <br><br>
 * This class should not be used directly by any client code. 
 * 
 * @see LazyLoadInterceptor
 * @see ProxyFactory
 * 
 * @author Razvan Popian
 */
public final class CollectionToStringInterceptor extends AbstractToStringInterceptor {
	static final String LOADED_PROXY = "Loaded proxy [%s]";
	static final String PROXY = "Proxy [%s]";
	
	CollectionToStringInterceptor() { }

	@Override
	protected String toStringUnloaded(Object proxy, Callable<?> zuper) {
		return String.format(PROXY, 
				proxy.getClass().getSuperclass().getName());
	}

	@Override
	protected String toStringLoaded(Object proxy, Callable<?> zuper) throws Exception {
		return String.format(LOADED_PROXY, zuper.call());		
	}

}
