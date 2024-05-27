package com.asentinel.common.orm.collections;

import java.util.LinkedHashMap;

/**
 * It should never be used in client code as it can break the ORM logic.
 * 
 * @since 1.60.14
 * @author Razvan Popian
 */
public class OrmLinkedHashMap<K,V> extends LinkedHashMap<K,V> implements OrmCollection {

	private static final long serialVersionUID = 1L;

}
