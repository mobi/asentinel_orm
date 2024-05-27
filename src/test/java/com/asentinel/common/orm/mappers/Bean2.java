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

/**
 * Method annotated bean.
 */
public class Bean2 {
	
	private int pk;
	private int intVal;
	private Integer intObj;
	private long longVal;
	private Long longObj;
	private double doubleVal;
	private Double doubleObj;
	private boolean boolVal;
	private Boolean boolObj;
	private String string;
	private Date date;
	private BigDecimal bigDecimal;
	private BigInteger bigInteger;
	private Number number;
	
	
	public Bean2() {
		this.pk = Integer.MAX_VALUE;
	}
	
	public int getPk() {
		return pk;
	}
	
	/** test with private method */
	@PkColumn(COL_PK)
	private void setPk(int pk) {
		this.pk = pk;
	}

	public int getIntVal() {
		return intVal;
	}

	/** test with private method */
	@Column(COL_INT_VAL)
	private void setIntVal(int intVal) {
		this.intVal = intVal;
	}

	public Integer getIntObj() {
		return intObj;
	}

	@Column(COL_INT_OBJ)
	public void setIntObj(Integer intObj) {
		this.intObj = intObj;
	}

	public long getLongVal() {
		return longVal;
	}

	@Column(COL_LONG_VAL)
	public void setLongVal(long longVal) {
		this.longVal = longVal;
	}

	public Long getLongObj() {
		return longObj;
	}

	@Column(COL_LONG_OBJ)
	public void setLongObj(Long longObj) {
		this.longObj = longObj;
	}

	public double getDoubleVal() {
		return doubleVal;
	}

	@Column(COL_DOUBLE_VAL)
	public void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}

	public Double getDoubleObj() {
		return doubleObj;
	}

	@Column(COL_DOUBLE_OBJ)
	public void setDoubleObj(Double doubleObj) {
		this.doubleObj = doubleObj;
	}

	public boolean isBoolVal() {
		return boolVal;
	}

	@Column(COL_BOOL_VAL)
	public void setBoolVal(boolean boolVal) {
		this.boolVal = boolVal;
	}

	public Boolean getBoolObj() {
		return boolObj;
	}

	@Column(COL_BOOL_OBJ)
	public void setBoolObj(Boolean boolObj) {
		this.boolObj = boolObj;
	}

	public String getString() {
		return string;
	}

	@Column(COL_STRING)
	public void setString(String string) {
		this.string = string;
	}

	public Date getDate() {
		return date;
	}

	@Column(COL_DATE)
	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}

	@Column(COL_BIG_DECIMAL)
	public void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}

	public BigInteger getBigInteger() {
		return bigInteger;
	}

	@Column(COL_BIG_INTEGER)
	public void setBigInteger(BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}

	public Number getNumber() {
		return number;
	}

	@Column(COL_NUMBER)
	public void setNumber(Number number) {
		this.number = number;
	}

	
	@Override
	public String toString() {
		return "Bean2 [pk=" + pk + ", intVal=" + intVal + ", intObj=" + intObj
				+ ", longVal=" + longVal + ", longObj=" + longObj
				+ ", doubleVal=" + doubleVal + ", doubleObj=" + doubleObj
				+ ", boolVal=" + boolVal + ", boolObj=" + boolObj + ", string="
				+ string + ", date=" + date + ", bigDecimal=" + bigDecimal
				+ ", bigInteger=" + bigInteger + ", number=" + number + "]";
	}
}
