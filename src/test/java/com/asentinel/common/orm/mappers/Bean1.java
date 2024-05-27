package com.asentinel.common.orm.mappers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static com.asentinel.common.orm.mappers.Const.*;

/**
 * Field annotated bean.
 */
public class Bean1 {
	
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
	
	
	public Bean1() {
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
