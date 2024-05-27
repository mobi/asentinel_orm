package com.asentinel.common.orm.collections;

import java.util.LinkedHashSet;

// FIXME: this class should have the same logic as the OrmTreeSet because the equals method
// of the entities may not behave correctly until the entire resultset is processed

/**
 * It should never be used in client code as it can break the ORM logic.
 * 
 * @since 1.60.14
 * @author Razvan Popian
 */
public class OrmLinkedHashSet<T> extends LinkedHashSet<T> implements OrmCollection {

	private static final long serialVersionUID = 1L;

}
