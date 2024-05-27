package com.asentinel.common.orm;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Bills")
public class ServiceTypeBill implements ParentEntity{
	
	@PkColumn("BillId")
	int billId;
	
	@Column("ItemNumber")
	String itemNumber;
	
	ServiceType serviceType;

	@Override
	public Object getEntityId() {
		return billId;
	}

	@Override
	public void setEntityId(Object object) {
		this.billId = (Integer) object;
	}

	@Override
	public void addChild(Object entity, EntityDescriptor entityDescriptor) {
		this.serviceType = (ServiceType) entity;
	}

	@Override
	public String toString() {
		return "ServiceTypeBill [billId=" + billId + ", itemNumber="
				+ itemNumber + ", serviceType=" + serviceType + "]";
	}


}
