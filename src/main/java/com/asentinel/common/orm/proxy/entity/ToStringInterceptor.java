package com.asentinel.common.orm.proxy.entity;

import java.util.concurrent.Callable;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.proxy.AbstractToStringInterceptor;

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
public final class ToStringInterceptor extends AbstractToStringInterceptor {
	
	static final String LOADED_PROXY = "Loaded proxy [%s]";
	static final String PROXY = "Proxy [%s, id=%s]";
	
	ToStringInterceptor() { }

	@Override
	protected String toStringUnloaded(Object proxy, Callable<?> zuper) {
		return String.format(PROXY, 
				proxy.getClass().getSuperclass().getName(), 
				String.valueOf(EntityUtils.getEntityId(proxy)));
	}

	@Override
	protected String toStringLoaded(Object proxy, Callable<?> zuper) throws Exception {
		return String.format(LOADED_PROXY, zuper.call());
	}

}
