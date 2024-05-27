package com.asentinel.common.orm.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.asentinel.common.orm.EntityBuilder;

/**
 * {@code TreeSet} subclass that defers the addition to the set until the
 * {@link #markAsOrmDone()} method is called. This method should be called once
 * the {@link EntityBuilder} is done processing the resultset.
 * <br>
 * It should never be used in client code as it can break the ORM logic.
 * 
 * @see #add(Object)
 * @see #markAsOrmDone()
 * 
 * @author Razvan Popian
 */
 public class OrmTreeSet<T> extends TreeSet<T> implements OrmCollection {
	
	private static final long serialVersionUID = 1L;

	private transient List<T> list = new ArrayList<>();
	
	private boolean isOrmDone() {
		return list == null;
	}
	
	public void markAsOrmDone() {
		if (isOrmDone()) {
			return;
		}
		for (T e: list) {
			super.add(e);
		}
		this.list = null;
	}
	
	/**
	 * This is the method that we want intercepted because it is used by the
	 * {@link EntityBuilder} to add entities. This method defers the addition to the
	 * actual {@code TreeSet} until the {@code EntityBuilder} is done processing the
	 * entire resultset, because only then we can be sure that all the children of
	 * the entity to be added are fully populated and therefore the comparison of
	 * sorted set elements will work without problems like
	 * {@code NullPointerException} because of not yet populated {@code Child} members.
	 * 
	 * @see #markAsOrmDone()
	 */
	@Override
	public boolean add(T e) {
		if (isOrmDone()) {
			return super.add(e);
		}
		list.add(e);
		return false;
	}

}
