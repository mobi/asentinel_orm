package com.asentinel.common.orm;

/**
 * @author Razvan Popian
 */
public enum JoinType {
	INNER("inner"), LEFT("left"), RIGHT("right");
	
	private final String keyword;
	
	JoinType(String keyword) {
		this.keyword = keyword;
	}

	public String getKeyword() {
		return keyword;
	}

}
