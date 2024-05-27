package com.asentinel.common.orm.jql.data;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("invoice")
public class Invoice {
	
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

}
