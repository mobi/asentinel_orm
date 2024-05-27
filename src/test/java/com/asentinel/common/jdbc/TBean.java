package com.asentinel.common.jdbc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;


public class TBean  {
	private int valueInt;
	private long valueLong;
	private double valueDouble;
	private String valueString;
	private Date valueDate;
	private boolean valueBoolean;
	private BigDecimal valueBigDecimal;
	private BigInteger valueBigInteger;
	
	private byte[] valueBytes;
	private InputStream ValueInputStream;
	
	private Number valueNumber;
	
	private String[] valueStringArray;
	private Number[] valueNumberArray;
	private BigDecimal[] valueBigDecimalArray;
	private int[] valueIntArray;
	private long[] valueLongArray;
	private double[] valueDoubleArray;
	private BigInteger[] valueBigIntegerArray;
	
	private LocalDate valueLocalDate;
	private LocalTime valueLocalTime;
	private LocalDateTime valueLocalDateTime;
	
	private TBeanEnum valueEnum;
	
	public byte[] getValueBytes() {
		return valueBytes;
	}

	public void setValueBytes(byte[] valueBytes) {
		this.valueBytes = valueBytes;
	}

	public int getValueInt() {
		return valueInt;
	}

	public void setValueInt(int valueInt) {
		this.valueInt = valueInt;
	}

	public long getValueLong() {
		return valueLong;
	}

	public void setValueLong(long valueLong) {
		this.valueLong = valueLong;
	}

	public double getValueDouble() {
		return valueDouble;
	}

	public void setValueDouble(double valueDouble) {
		this.valueDouble = valueDouble;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	public boolean isValueBoolean() {
		return valueBoolean;
	}

	public void setValueBoolean(boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	public void setValueBigDecimal(BigDecimal valueBigDecimal) {
		this.valueBigDecimal = valueBigDecimal;
	}
	
	public BigDecimal getValueBigDecimal() {
		return valueBigDecimal;
	}
	
	public BigInteger getValueBigInteger() {
		return valueBigInteger;
	}
	
	public void setValueBigInteger(BigInteger valueBigInteger) {
		this.valueBigInteger = valueBigInteger;
	}
	
	public void setValueInputStream(InputStream valueInputStream) {
		ValueInputStream = valueInputStream;
	}

	public InputStream getValueInputStream() {
		return ValueInputStream;
	}

	public Number getValueNumber() {
		return valueNumber;
	}

	public void setValueNumber(Number valueNumber) {
		this.valueNumber = valueNumber;
	}

	public String[] getValueStringArray() {
		return valueStringArray;
	}

	public void setValueStringArray(String[] stringArray) {
		this.valueStringArray = stringArray;
	}

	public Number[] getValueNumberArray() {
		return valueNumberArray;
	}

	public void setValueNumberArray(Number[] valueNumberArray) {
		this.valueNumberArray = valueNumberArray;
	}

	public BigDecimal[] getValueBigDecimalArray() {
		return valueBigDecimalArray;
	}

	public void setValueBigDecimalArray(BigDecimal[] valueBigDecimalArray) {
		this.valueBigDecimalArray = valueBigDecimalArray;
	}

	public int[] getValueIntArray() {
		return valueIntArray;
	}

	public void setValueIntArray(int[] valueIntArray) {
		this.valueIntArray = valueIntArray;
	}

	public long[] getValueLongArray() {
		return valueLongArray;
	}

	public void setValueLongArray(long[] valueLongArray) {
		this.valueLongArray = valueLongArray;
	}

	public double[] getValueDoubleArray() {
		return valueDoubleArray;
	}

	public void setValueDoubleArray(double[] valueDoubleArray) {
		this.valueDoubleArray = valueDoubleArray;
	}

	public BigInteger[] getValueBigIntegerArray() {
		return valueBigIntegerArray;
	}

	public void setValueBigIntegerArray(BigInteger[] bigIntegerArray) {
		this.valueBigIntegerArray = bigIntegerArray;
	}

	public LocalDate getValueLocalDate() {
		return valueLocalDate;
	}

	public void setValueLocalDate(LocalDate valueLocalDate) {
		this.valueLocalDate = valueLocalDate;
	}

	public LocalTime getValueLocalTime() {
		return valueLocalTime;
	}

	public void setValueLocalTime(LocalTime valueLocalTime) {
		this.valueLocalTime = valueLocalTime;
	}

	public LocalDateTime getValueLocalDateTime() {
		return valueLocalDateTime;
	}

	public void setValueLocalDateTime(LocalDateTime valueLocalDateTime) {
		this.valueLocalDateTime = valueLocalDateTime;
	}
	
	public TBeanEnum getValueEnum() {
		return valueEnum;
	}

	public void setValueEnum(TBeanEnum valueEnum) {
		this.valueEnum = valueEnum;
	}
	
	public enum TBeanEnum {
		AAA, BBB
	}
}
