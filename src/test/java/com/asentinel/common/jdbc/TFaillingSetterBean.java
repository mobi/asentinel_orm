package com.asentinel.common.jdbc;

public class TFaillingSetterBean {
	private int intValue;

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		throw new UnsupportedOperationException();
	}
}
