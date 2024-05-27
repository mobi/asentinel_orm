package com.asentinel.common.orm.proxy;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.proxy.collection.CollectionProxyFactory;
import com.asentinel.common.orm.proxy.entity.ProxyFactory;

/**
 * Marker interface for entity and collection proxies. It can be used to quickly determine
 * if an entity instance or a collection is a proxy or not.
 * <br><br>
 * This interface should not be used directly by any client code.
 * 
 * @see ProxyFactory#getProxyObjectFactoryInternal(Class)
 * @see CollectionProxyFactory#getProxyObjectFactoryInternal(Class)
 * @see EntityUtils#isProxy(Object)
 * @see EntityUtils#isLoadedProxy(Object)
 * 
 * @author Razvan Popian
 */
public interface Proxy {

}
