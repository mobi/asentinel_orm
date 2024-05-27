package com.asentinel.common.text;

import java.time.LocalTime;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.asentinel.common.util.Assert;

/**
 * Decorator for a {@link Converter} that guarantees that a default value is returned when an empty or {@code null}
 * string is passed to the conversion methods.
 * For numeric types the default value is {@code 0}, for string is empty string, for boolean is {@code false} and
 * for date and entity is {@code null}.
 * 
 * @see StandardConverter
 * @see Converter
 *  
 * @author Razvan Popian
 */
public class DefaultValueForEmptyConverter implements Converter {
	
	private static final Integer ZERO_INTEGER = Integer.valueOf(0);
	private static final Long ZERO_LONG = Long.valueOf(0);
	private static final Double ZERO_DOUBLE = Double.valueOf(0);
	
	private final Converter converter;
	
	/**
	 * Constructor.
	 * @param converter the target converter to decorate.
	 */
	public DefaultValueForEmptyConverter(Converter converter) {
		Assert.assertNotNull(converter, "converter");
		this.converter = converter;
	}

	@Override
	public Integer convertToInteger(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return ZERO_INTEGER;
		}
		return converter.convertToInteger(value, id);
	}

	@Override
	public Long convertToLong(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return ZERO_LONG;
		}
		return converter.convertToLong(value, id);
	}

	@Override
	public Double convertToDouble(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return ZERO_DOUBLE;
		}
		return converter.convertToDouble(value, id);
	}

	@Override
	public String convertToString(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		return converter.convertToString(value, id);
	}

	@Override
	public Date convertToDate(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return converter.convertToDate(value, id);
	}
	
	@Override
	public LocalTime convertToTime(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return converter.convertToTime(value, id);
	}
	
	@Override
	public Boolean convertToBoolean(String value, Object id) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return Boolean.FALSE;
		}
		return converter.convertToBoolean(value, id);
	}

	@Override
	public <T> T convertToEntity(String value, Object id, Class<T> clazz) throws ConversionException {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return converter.convertToEntity(value, id, clazz);
	}

	
	public Converter getTargetConverter() {
		return converter;
	}

}
