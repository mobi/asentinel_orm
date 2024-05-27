package com.asentinel.common.orm.jql.data;

import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Bill")
public class OverrideBill {
	

	@PkColumn("billId")
	public int id;
	
}
