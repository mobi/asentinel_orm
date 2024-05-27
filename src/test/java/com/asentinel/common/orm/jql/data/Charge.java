package com.asentinel.common.orm.jql.data;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("charge")
public class Charge {
	public final static String TABLE_ALIAS = "c";
	
	public final static String C_ID = "c_id";
	public final static String C_CHARGE = "charge";
	
	@PkColumn(C_ID)
	public int id;
	
	@Column(C_CHARGE)
	public double charge;
	
	@Child(tableAlias = Bill.TABLE_ALIAS)
	public Bill bill;

}
