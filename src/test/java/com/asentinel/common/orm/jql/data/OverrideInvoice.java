package com.asentinel.common.orm.jql.data;

import java.util.List;

import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Invoice")
public class OverrideInvoice {
	public final static String TABLE_ALIAS = "i";
	public final static String C_ID = "id";
	public final static String C_NUMBER = "invoiceNumber";
	public final static String C_ACCOUNT = "invoiceAccount";


	@PkColumn(C_ID)
	public int id;
	
	@Column(C_NUMBER)
	public String invoiceNumber;

	@Column(C_ACCOUNT)
	public String invoiceAccount;

	
	@Child(parentRelationType = RelationType.MANY_TO_ONE, 
			tableAlias = Bill.TABLE_ALIAS,
			joinConditionsOverride = "#{defaultJoinCondition} and #{childTableAlias}.BillId = ?"
			)
	public List<OverrideBill> bills;
	
}
