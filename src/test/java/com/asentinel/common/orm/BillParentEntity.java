package com.asentinel.common.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.mappers.Table;

@Table("Bill")
public class BillParentEntity extends Bill implements ParentEntity {
	
	@Override
	public void addChild(Object entity, EntityDescriptor descriptor) {
		charges.add((Charge) entity);
	}
	
	static class BillRowMapper implements RowMapper<Entity> {

		@Override
		public BillParentEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			BillParentEntity bill = new BillParentEntity();
			bill.setItemNumber(ResultSetUtils.getStringValue(rs, "ItemNumber"));
			return bill;
		}
		
	}
	
}