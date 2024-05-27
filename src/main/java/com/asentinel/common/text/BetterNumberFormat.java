package com.asentinel.common.text;

import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;

/**
 * Wrapper for a {@link NumberFormat} instance used to override behavior for the {@link NumberFormat#parse(String)} method.
 * We want to throw an exception when parsing "33dd33" string because we would have received 33. 
 * We also return null for this method if the received parameter is null
 */
public class BetterNumberFormat extends NumberFormat {
	
	private static final long serialVersionUID = 3366213427703336243L;
	
	private final NumberFormat numberFormat;
	
	public BetterNumberFormat(NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return numberFormat.format(number, toAppendTo, pos);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return numberFormat.format(number, toAppendTo, pos);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		return numberFormat.parse(source, parsePosition);
	}		
	
	@Override
	public StringBuffer format(Object number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return numberFormat.format(number, toAppendTo, pos);
	}
	
	@Override
	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		return numberFormat.formatToCharacterIterator(obj);
	}
	
	
	@Override
	public boolean isParseIntegerOnly() {
		return numberFormat.isParseIntegerOnly();
	}

	@Override
	public void setParseIntegerOnly(boolean value) {
		numberFormat.setParseIntegerOnly(value);
	}

	@Override
	public boolean isGroupingUsed() {
		return numberFormat.isGroupingUsed();
	}

	@Override
	public void setGroupingUsed(boolean newValue) {
		numberFormat.setGroupingUsed(newValue);
	}

	@Override
	public int getMaximumIntegerDigits() {
		return numberFormat.getMaximumIntegerDigits();
	}

	@Override
	public void setMaximumIntegerDigits(int newValue) {
		numberFormat.setMaximumIntegerDigits(newValue);
	}

	@Override
	public int getMinimumIntegerDigits() {
		return numberFormat.getMinimumIntegerDigits();
	}

	@Override
	public void setMinimumIntegerDigits(int newValue) {
		numberFormat.setMinimumIntegerDigits(newValue);
	}

	@Override
	public int getMaximumFractionDigits() {
		return numberFormat.getMaximumFractionDigits();
	}

	@Override
	public void setMaximumFractionDigits(int newValue) {
		numberFormat.setMaximumFractionDigits(newValue);
	}

	@Override
	public int getMinimumFractionDigits() {
		return numberFormat.getMinimumFractionDigits();
	}

	@Override
	public void setMinimumFractionDigits(int newValue) {
		numberFormat.setMinimumFractionDigits(newValue);
	}

	@Override
	public Currency getCurrency() {
		return numberFormat.getCurrency();
	}

	@Override
	public void setCurrency(Currency currency) {
		numberFormat.setCurrency(currency);
	}

	@Override
	public RoundingMode getRoundingMode() {
		return numberFormat.getRoundingMode();
	}

	@Override
	public void setRoundingMode(RoundingMode roundingMode) {
		numberFormat.setRoundingMode(roundingMode);
	}
	
	@Override
	public Object parseObject(String source) throws ParseException {
		return numberFormat.parseObject(source);
	}

	@Override
	public Number parse(String source) throws ParseException {
		if (source == null) {
			return null;
		}
		source = source.trim();
		ParsePosition pp = new ParsePosition(0);
		Number number = numberFormat.parse(source, pp);
		int parseIndex = pp.getIndex();
		
		if (number == null || source.length() != parseIndex) {
			throw new ParseException("Unexpected character", parseIndex);
		}
		return number;
	}
	
}