package com.asentinel.common.text;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.math.BigDecimal;
import java.text.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * Simple converter class. This class is NOT thread safe, 
 * but is reusable on the same thread.
 * Note: The convert methods in this class will return {@code null} if 
 * 		the value argument is {@code null}.
 * 
 * @author Razvan Popian 
 * @author Mihai Curca
 */
public class StandardConverter implements Converter {
	
	private final DateFormat df1;		
	private final DateFormat df2;
	private final DateFormat df3;
	
	private final NumberFormat nf;
	private final NumberFormat nfInt;
	
	private final Optional<ConversionService> conversionService;
	
	/** default constructor */
	public StandardConverter() {
		this(null, null);
	}
	
	public StandardConverter(Locale locale) {
		this(null, locale);
	}
	
	/** I18N constructor */
	public StandardConverter(ConversionService conversionService, Locale locale){
		this.conversionService = Optional.ofNullable(conversionService);
		
		if (locale == null){
			df1 = new SimpleDateFormat("MM/dd/yyyy");		
			df2 = new SimpleDateFormat("MMM dd, yyyy");
			df3 = new SimpleDateFormat("hh:mm:ss");
			
            NumberFormat nf0 = NumberFormat.getInstance(Locale.US);
            if (nf0 instanceof DecimalFormat) {
            	((DecimalFormat) nf0).setParseBigDecimal(true);
            }
            nf = new BetterNumberFormat(nf0);
            
            
            NumberFormat nfInt0 = NumberFormat.getInstance(Locale.US);
            if (nfInt0 instanceof DecimalFormat) {
            	((DecimalFormat) nfInt0).setParseBigDecimal(true);
            }
			nfInt = new BetterNumberFormat(nfInt0);
			nfInt.setMaximumFractionDigits(0);
			nfInt.setMinimumFractionDigits(0);
		} else {
			df1 = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			df2 = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			df3 = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

			NumberFormat nf0 = NumberFormat.getInstance(locale);
			if (nf0 instanceof DecimalFormat) {
				((DecimalFormat) nf0).setParseBigDecimal(true);
			}
			nf = new BetterNumberFormat(nf0);

			NumberFormat nfInt0 = NumberFormat.getInstance(locale);
			if (nfInt0 instanceof DecimalFormat) {
				((DecimalFormat) nfInt0).setParseBigDecimal(true);
			}
			nfInt = new BetterNumberFormat(nfInt0);
			nfInt.setMaximumFractionDigits(0);
			nfInt.setMinimumFractionDigits(0);
		}
	}

	/**
	 * This method attempts to convert the string value to an Integer object with
	 * the range between {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}.
	 * 
	 * @throws ConversionException with {@link OutOfBoundsException} as Throwable
	 *         cause when the number exceeds the limits.<br>
	 *         {@link ConversionException} with {@link NumberFormatException} or
	 *         {@link ParseException} as Throwable cause when the number is invalid.
	 * 
	 * @see Converter#convertToInteger(String, Object)
	 */
	@Override
	public Integer convertToInteger(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}
		Number number;
		try {
			number = nfInt.parse(value);
			checkNumberLimits(number, BigDecimal.valueOf(Integer.MAX_VALUE), BigDecimal.valueOf(Integer.MIN_VALUE), "Integer");
		} catch (NumberFormatException | ParseException | OutOfBoundsException e) {
			throw new ConversionException("Integer conversion error !", e);
		}

		return number.intValue();
	}

	/**
	 * This method attempts to convert the string value to a Long object with
	 * the range between {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE}.
	 * 
	 * @throws ConversionException with {@link OutOfBoundsException} as Throwable
	 *         cause when the number exceeds the limits.<br>
	 *         {@link ConversionException} with {@link NumberFormatException} or
	 *         {@link ParseException} as Throwable cause when the number is invalid.
	 * @see Converter#convertToLong(String, Object) 
	 */
	@Override
	public Long convertToLong(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}
		Number number;
		try {
			number = nfInt.parse(value);
			checkNumberLimits(number, BigDecimal.valueOf(Long.MAX_VALUE), BigDecimal.valueOf(Long.MIN_VALUE), "Long");
		} catch (NumberFormatException | ParseException | OutOfBoundsException e) {
			throw new ConversionException("Long conversion error !", e);
		}
		
		return number.longValue();
	}
	
	/**
	 * This method attempts to convert the string value to a Long object with
	 * the range between -{@link Double#MAX_VALUE} and {@link Double#MAX_VALUE}.
	 * 
	 * @throws ConversionException with {@link OutOfBoundsException} as Throwable
	 *         cause when the number exceeds the limits.<br>
	 *         {@link ConversionException} with {@link NumberFormatException} or
	 *         {@link ParseException} as Throwable cause when the number is invalid.
	 * @see Converter#convertToDouble(String, Object) 
	 * */
	@Override
	public Double convertToDouble(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}
		Number number;
		try {
			number = nf.parse(value);
			checkNumberLimits(number, BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal.valueOf(-Double.MAX_VALUE), "Double");
		} catch (NumberFormatException | ParseException | OutOfBoundsException e) {
			throw new ConversionException("Double conversion error !", e);
		}
		
		return number.doubleValue();
	}
	

	/** @see Converter#convertToString(String, Object) */
	@Override
	public String convertToString(String value, Object id) throws ConversionException {
		return value;		
	}

	/** @see Converter#convertToDate(String, Object) */
	@Override
	public Date convertToDate(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}

		if (conversionService.isPresent()) {
			try {
				return conversionService.get().convert(value, Date.class);
			} catch (org.springframework.core.convert.ConversionException e) {
				throw new ConversionException("Failed to convert the value '" + value
						+ "' to " + Date.class.getName() + " for field " + id + ".",
						e);
			}
		}

		try {
			return df1.parse(value);
		} catch (ParseException pe1) {
			try {
				return df2.parse(value);
			} catch (ParseException pe2) {
				try {
					return df3.parse(value);
				} catch (ParseException pe3) {
					throw new ConversionException("Date conversion error !", pe3);
				}
			}
		}
	}
	
	/** @see Converter#convertToTime(String, Object) */
	@Override
	public LocalTime convertToTime(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}
		
		try {
			return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm a"));
		} catch(DateTimeParseException dtpe) {
			throw new ConversionException("Time conversion error !", dtpe);
		}
	}
	
	/** @see Converter#convertToBoolean(String, Object) */ 
	@Override
	public Boolean convertToBoolean(String value, Object id) throws ConversionException {
		if (value == null) {
			return null;
		}
		String tmpCellText = value.trim().toUpperCase();
		return tmpCellText.equals("Y")
							|| tmpCellText.equals("YES") 
							|| tmpCellText.equals("TRUE")
							|| tmpCellText.equals("1")
							|| tmpCellText.equals("T");
	}

	/** @see Converter#convertToEntity(String, Object, Class) */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convertToEntity(String value, Object id, Class<T> clazz) throws ConversionException {
		if (conversionService.isEmpty()) {
			throw new IllegalStateException("No Conversion Service available for creating an Entity!");
		}
		if (value == null) {
			return null;
		}
		
		try {
			return (T) conversionService.get().convert(value, TypeDescriptor.forObject(value), new FieldIdTypeDescriptor(id, clazz));
		} catch (org.springframework.core.convert.ConversionException e) {
			throw new ConversionException("Failed to convert the value '" + value 
					+ "' to " + clazz.getName() + " for field " + id + ".", 
					e);
		}
	}
	
	private void checkNumberLimits(Number number, BigDecimal maxLimit, BigDecimal minLimit, String type)
			throws OutOfBoundsException {
		if (number instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) number;
			if (bd.compareTo(maxLimit) > 0) {
				throw new OutOfBoundsException(type + " conversion error ! Number " + bd + " is too large.");
			}
			if (bd.compareTo(minLimit) < 0) {
				throw new OutOfBoundsException(type + " conversion error ! Number " + bd + " is too small.");
			}
		}
	}
	

}
