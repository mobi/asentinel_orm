package com.asentinel.common.orm;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Invoice")
public class Invoice2 implements ParentEntity {
	
	@PkColumn("InvoiceId")
	int invoiceId;
	
	@Column("InvoiceNumber")
	String invoiceNumber;
	
	BillParentEntity bill1;
	
	BillParentEntity bill2;

	@Override
	public Object getEntityId() {
		return invoiceId;
	}
	@Override
	public void setEntityId(Object object) {
		invoiceId = ((Number) object).intValue();
	}

	@Override
	public void addChild(Object entity, EntityDescriptor entityDescriptor) {
		if (entity instanceof BillParentEntity) {
			if (entityDescriptor.getName().toString().equalsIgnoreCase("bill1")) {
				this.bill1 = (BillParentEntity) entity;
			} else if (entityDescriptor.getName().toString().equalsIgnoreCase("bill2")) {
				this.bill2 = (BillParentEntity) entity;
			} else {
				throw new IllegalArgumentException("Invalid argument.");
			}
		} else {
			throw new IllegalArgumentException("Invalid argument.");
		}
	}
	
	
	public int getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(int invoiceId) {
		this.invoiceId = invoiceId;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public BillParentEntity getBill1() {
		return bill1;
	}
	public void setBill1(BillParentEntity bill1) {
		this.bill1 = bill1;
	}
	public BillParentEntity getBill2() {
		return bill2;
	}
	public void setBill2(BillParentEntity bill2) {
		this.bill2 = bill2;
	}

}
