package com.asentinel.common.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.mappers.Table;

@Table("Invoice")
public class InvoiceParentEntity extends Invoice implements ParentEntity {
	
	@Override
	public void addChild(Object entity, EntityDescriptor descriptor) {
		addBill((BillParentEntity) entity);
	}
	
	static class InvoiceRowMapper implements RowMapper<Entity> {
		
		@Override
		public InvoiceParentEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
			InvoiceParentEntity invoice = new InvoiceParentEntity();
			invoice.setInvoiceNumber(ResultSetUtils.getStringValue(rs, "InvoiceNumber"));
			return invoice;
		}
		
	}
	
}