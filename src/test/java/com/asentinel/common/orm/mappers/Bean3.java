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

class Bean3 {

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
	
	
	Bean3() {
		this.pk = Integer.MAX_VALUE;
	}
	
	int getPk() {
		return pk;
	}

	void setPk(int pk) {
		this.pk = pk;
	}

	int getIntVal() {
		return intVal;
	}

	void setIntVal(int intVal) {
		this.intVal = intVal;
	}

	Integer getIntObj() {
		return intObj;
	}

	void setIntObj(Integer intObj) {
		this.intObj = intObj;
	}

	long getLongVal() {
		return longVal;
	}

	void setLongVal(long longVal) {
		this.longVal = longVal;
	}

	Long getLongObj() {
		return longObj;
	}

	void setLongObj(Long longObj) {
		this.longObj = longObj;
	}

	double getDoubleVal() {
		return doubleVal;
	}

	void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}

	Double getDoubleObj() {
		return doubleObj;
	}

	void setDoubleObj(Double doubleObj) {
		this.doubleObj = doubleObj;
	}

	boolean isBoolVal() {
		return boolVal;
	}

	void setBoolVal(boolean boolVal) {
		this.boolVal = boolVal;
	}

	Boolean getBoolObj() {
		return boolObj;
	}

	void setBoolObj(Boolean boolObj) {
		this.boolObj = boolObj;
	}

	String getString() {
		return string;
	}

	void setString(String string) {
		this.string = string;
	}

	Date getDate() {
		return date;
	}

	void setDate(Date date) {
		this.date = date;
	}

	BigDecimal getBigDecimal() {
		return bigDecimal;
	}

	void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	BigInteger getBigInteger() {
		return bigInteger;
	}

	void setBigInteger(BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}

	Number getNumber() {
		return number;
	}

	void setNumber(Number number) {
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
