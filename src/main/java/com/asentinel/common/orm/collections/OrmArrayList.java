package com.asentinel.common.orm.collections;

import java.util.ArrayList;

/**
 * It should never be used in client code as it can break the ORM logic.
 * 
 * @since 1.60.14
 * @author Razvan Popian
 */
public class OrmArrayList<T> extends ArrayList<T> implements OrmCollection {

	private static final long serialVersionUID = 1L;

}
