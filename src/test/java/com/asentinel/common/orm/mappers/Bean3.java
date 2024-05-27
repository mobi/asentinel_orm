package com.asentinel.common.orm.mappers;

import static com.asentinel.common.orm.mappers.Const.COL_BIG_DECIMAL;
import static com.asentinel.common.orm.mappers.Const.COL_BIG_INTEGER;
import static com.asentinel.common.orm.mappers.Const.COL_BOOL_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_BOOL_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_DATE;
import static com.asentinel.common.orm.mappers.Const.COL_DOUBLE_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_DOUBLE_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_INT_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_INT_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_LONG_OBJ;
import static com.asentinel.common.orm.mappers.Const.COL_LONG_VAL;
import static com.asentinel.common.orm.mappers.Const.COL_NUMBER;
import static com.asentinel.common.orm.mappers.Const.COL_PK;
import static com.asentinel.common.orm.mappers.Const.COL_STRING;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class Bean3 {

	@PkColumn(COL_PK)
	private int pk;
	
	@Column(value = COL_INT_VAL, allowNull = true)
	private int intVal;
	
	@Column(value = COL_INT_OBJ, allowNull = true)
	private Integer intObj;

	@Column(value = COL_LONG_VAL, allowNull = true)
	private long longVal;
	
	@Column(value = COL_LONG_OBJ, allowNull = true)
	private Long longObj;
	
	@Column(value = COL_DOUBLE_VAL, allowNull = true)
	private double doubleVal;
	
	@Column(value = COL_DOUBLE_OBJ, allowNull = true)
	private Double doubleObj;

	@Column(value = COL_BOOL_VAL, allowNull = true)
	private boolean boolVal;
	
	@Column(value = COL_BOOL_OBJ, allowNull = true)
	private Boolean boolObj;

	@Column(value = COL_STRING, allowNull = true)
	private String string;
	
	@Column(value = COL_DATE, allowNull = true)
	private Date date;
	
	@Column(value = COL_BIG_DECIMAL, allowNull = true)
	private BigDecimal bigDecimal;
	
	@Column(value = COL_BIG_INTEGER, allowNull = true)
	private BigInteger bigInteger;

	@Column(value = COL_NUMBER, allowNull = true)
	private Number number;
	
	
	public Bean3() {
		this.pk = Integer.MAX_VALUE;
	}
	
	public int getPk() {
		return pk;
	}

	public void setPk(int pk) {
		this.pk = pk;
	}

	public int getIntVal() {
		return intVal;
	}

	public void setIntVal(int intVal) {
		this.intVal = intVal;
	}

	public Integer getIntObj() {
		return intObj;
	}

	public void setIntObj(Integer intObj) {
		this.intObj = intObj;
	}

	public long getLongVal() {
		return longVal;
	}

	public void setLongVal(long longVal) {
		this.longVal = longVal;
	}

	public Long getLongObj() {
		return longObj;
	}

	public void setLongObj(Long longObj) {
		this.longObj = longObj;
	}

	public double getDoubleVal() {
		return doubleVal;
	}

	public void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}

	public Double getDoubleObj() {
		return doubleObj;
	}

	public void setDoubleObj(Double doubleObj) {
		this.doubleObj = doubleObj;
	}

	public boolean isBoolVal() {
		return boolVal;
	}

	public void setBoolVal(boolean boolVal) {
		this.boolVal = boolVal;
	}

	public Boolean getBoolObj() {
		return boolObj;
	}

	public void setBoolObj(Boolean boolObj) {
		this.boolObj = boolObj;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}

	public void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	public BigInteger getBigInteger() {
		return bigInteger;
	}

	public void setBigInteger(BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}

	public Number getNumber() {
		return number;
	}

	public void setNumber(Number number) {
		this.number = number;
	}

	
	@Override
	public String toString() {
		return "Bean1 [pk=" + pk + ", intVal=" + intVal + ", intObj=" + intObj
				+ ", longVal=" + longVal + ", longObj=" + longObj
				+ ", doubleVal=" + doubleVal + ", doubleObj=" + doubleObj
				+ ", boolVal=" + boolVal + ", boolObj=" + boolObj + ", string="
				+ string + ", date=" + date + ", bigDecimal=" + bigDecimal
				+ ", bigInteger=" + bigInteger + ", number=" + number + "]";
	}
	
}
