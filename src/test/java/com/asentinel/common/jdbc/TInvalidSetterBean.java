package com.asentinel.common.jdbc;

public class TInvalidSetterBean {
	private int intValue;

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue, Object dummy) {
		this.intValue = intValue;
	}

}
