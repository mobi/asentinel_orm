package com.asentinel.common.orm.mappers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static com.asentinel.common.orm.mappers.Const.*;

/**
 * Field annotated bean.
 */
class Bean1 {
	
	@PkColumn(COL_PK)
	private int pk;
	
	@Column(COL_INT_VAL)
	private int intVal;
	
	@Column(COL_INT_OBJ)
	private Integer intObj;

	@Column(COL_LONG_VAL)
	private long longVal;
	
	@Column(COL_LONG_OBJ)
	private Long longObj;
	
	@Column(COL_DOUBLE_VAL)
	private double doubleVal;
	
	@Column(COL_DOUBLE_OBJ)
	private Double doubleObj;

	@Column(COL_BOOL_VAL)
	private boolean boolVal;
	
	@Column(COL_BOOL_OBJ)
	private Boolean boolObj;

	@Column(COL_STRING)
	private String string;
	
	@Column(COL_DATE)
	private Date date;
	
	@Column(COL_BIG_DECIMAL)
	private BigDecimal bigDecimal;
	
	@Column(COL_BIG_INTEGER)
	private BigInteger bigInteger;

	@Column(COL_NUMBER)
	private Number number;
	
	
	Bean1() {
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
