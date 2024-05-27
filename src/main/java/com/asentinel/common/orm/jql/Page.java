package com.asentinel.common.orm.jql;

import java.util.List;

import com.asentinel.common.util.Assert;

/**
 * Object that holds the number of items a query returns 
 * and the content of a page.
 * 
 * @see SqlBuilder#execForPage(com.asentinel.common.jdbc.SqlQuery)
 * 
 * @author Razvan Popian
 */
public class Page<T> {
	private final List<T> items;
	private final long count;
	
	public Page(List<T> items, long count) {
		Assert.assertNotNull(items, "items");
		Assert.assertPositive(count, "count");
		this.items = items;
		this.count = count;
	}

	public List<T> getItems() {
		return items;
	}

	public long getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "Page [" 
				+ items.size() + "/" + count
				+ "]";
	}
}
