package com.asentinel.common.orm.jql;

/**
 * @see SqlBuilder#asc(Nulls)
 * @see SqlBuilder#desc(Nulls)
 *  
 * @author Razvan Popian
 */
public enum Nulls {
	FIRST("nulls first"), LAST("nulls last");
	
	private final String keyword;
	
	String getKeyword() {
		return keyword;
	}

	Nulls(String keyword) {
		this.keyword = keyword;
	}
}
