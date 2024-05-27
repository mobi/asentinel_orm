package com.asentinel.common.orm;

import com.asentinel.common.orm.mappers.Table;

/**
 * @author Razvan Popian
 */
class TableAnnotationInfo {
	private final Class<?> clasz;
	private final Table tableAnn;
	
	public TableAnnotationInfo(Class<?> clasz, Table tableAnn) {
		this.clasz = clasz;
		this.tableAnn = tableAnn;
	}

	public Class<?> getTargetClass() {
		return clasz;
	}

	public Table getTableAnn() {
		return tableAnn;
	}

	@Override
	public String toString() {
		return "TableAnnotationInfo [clasz=" + clasz + ", tableAnn="
				+ tableAnn + "]";
	}
}
