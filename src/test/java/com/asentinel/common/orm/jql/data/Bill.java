package com.asentinel.common.orm.jql.data;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("bill")
public class Bill {
	public final static String TABLE_ALIAS = "b";
	
	public final static String C_ID = "b_id";
	public final static String C_PH_NUMBER = "phoneNumber";
	
	@PkColumn(C_ID)
	public int id;
	
	@Column(C_PH_NUMBER)
	public String phNumber;
	
	@Child(tableAlias = Invoice.TABLE_ALIAS)
	public Invoice invoice;

}
