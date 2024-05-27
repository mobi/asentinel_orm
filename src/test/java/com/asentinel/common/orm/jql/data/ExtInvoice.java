package com.asentinel.common.orm.jql.data;

import java.util.List;

import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.mappers.Child;

public class ExtInvoice extends Invoice {
	
	@Child(parentRelationType = RelationType.MANY_TO_ONE, tableAlias = Bill.TABLE_ALIAS)
	public List<Bill> bills;

}
