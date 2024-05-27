package com.asentinel.common.orm;

import java.util.ArrayList;
import java.util.List;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Bills")
public class Bill implements Entity {

	@PkColumn("BillId")
	int billId;
	
	@Column("ItemNumber")
	String itemNumber;
	
	List<Charge> charges = new ArrayList<Charge>();
	
	@Child(parentRelationType=RelationType.MANY_TO_ONE)
	public void addCharge(Charge charge) {
		charges.add(charge);
	}
	
	public int getBillId() {
		return billId;
	}
	public void setBillId(int billId) {
		this.billId = billId;
	}
	
	public String getItemNumber() {
		return itemNumber;
	}
	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}
	
	@Override
	public Object getEntityId() {
		return billId;
	}
	
	@Override
	public void setEntityId(Object object) {
		billId = ((Number) object).intValue();
	}
	
	@Override
	public String toString() {
		return "Bill [billId=" + billId + ", itemNumber=" + itemNumber
				+ "]";
	}

}
