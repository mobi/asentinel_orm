package com.asentinel.common.orm;

import java.util.ArrayList;
import java.util.List;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Invoices")
public class Invoice implements Entity {
	@PkColumn("InvoiceId")
	int invoiceId;
	
	@Column("InvoiceNumber")
	String invoiceNumber;
	
	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	List<Bill> bills = new ArrayList<Bill>();
	
	public List<Bill> getBills() {
		return bills;
	}

	public void addBill(Bill bill) {
		bills.add(bill);
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public int getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(int invoiceId) {
		this.invoiceId = invoiceId;
	}
	
	@Override
	public Object getEntityId() {
		return getInvoiceId();
	}
	@Override
	public void setEntityId(Object object) {
		invoiceId = ((Number) object).intValue();
	}

	@Override
	public String toString() {
		return "Invoice [invoiceId=" + invoiceId + ", invoiceNumber=" + invoiceNumber + "]";
	}
}
