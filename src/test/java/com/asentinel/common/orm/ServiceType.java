package com.asentinel.common.orm;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("ServiceTypes")
public class ServiceType implements Entity {

	@PkColumn("ServiceTypeId")
	int serviceTypeId;
	
	@Column("Description")
	String description;

	public ServiceType(int serviceTypeId, String description) {
		super();
		this.serviceTypeId = serviceTypeId;
		this.description = description;
	}

	@Override
	public Object getEntityId() {
		return serviceTypeId;
	}

	@Override
	public void setEntityId(Object object) {
		this.serviceTypeId = (Integer) object;
	}

	@Override
	public String toString() {
		return "ServiceType [serviceTypeId=" + serviceTypeId + ", description="
				+ description + "]";
	}

}
