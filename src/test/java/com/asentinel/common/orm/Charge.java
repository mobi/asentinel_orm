package com.asentinel.common.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

@Table("Charges")
public class Charge implements Entity {
	
	@PkColumn("ChargeId")
	int chargeId;
	
	@Column("Value")
	double value;

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public Object getEntityId() {
		return chargeId;
	}

	@Override
	public void setEntityId(Object object) {
		this.chargeId = ((Number) object).intValue();
	}
	
	@Override
	public String toString() {
		return "Charge [chargeId=" + chargeId + ", value=" + value
				+ "]";
	}
	
	static class ChargeRowMapper implements RowMapper<Entity> {

		@Override
		public Charge mapRow(ResultSet rs, int rowNum) throws SQLException {
			Charge charge = new Charge();
			charge.setValue(ResultSetUtils.getDoubleValue(rs, "Value"));
			return charge;
		}
		
	}
	
}
