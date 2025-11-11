package com.asentinel.common.text;

import java.time.LocalTime;
import java.util.Date;

/**
 * @author Razvan Popian
 */
public interface Converter {
	
	/**
	 * this method attempts to convert the string value to an Integer object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return Integer
	 */
	Integer convertToInteger(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a Long object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return Long
	 */
	Long convertToLong(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a Double object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return Double
	 */
	Double convertToDouble(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a String object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return String
	 */	
	String convertToString(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a Date object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return Date
	 */
	Date convertToDate(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a LocalTime object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return LocalTime
	 */
	LocalTime convertToTime(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to a Boolean object
	 * @throws ConversionException if error
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @return Boolean
	 */
	Boolean convertToBoolean(String value, Object id) throws ConversionException;
	
	/**
	 * this method attempts to convert the string value to an entity where this values represents
	 *			the id of that entity
	 * @param value - the string value to be converted
	 * @param id - an id passed in by the caller (for example a BaseData field index) to allow the implementer
	 * 				to perform custom conversions for certain fields 
	 * @param clazz - class of the entity to which the value will be converted to
	 * @return entity
	 */
	<T> T convertToEntity(String value, Object id, Class<T> clazz) throws ConversionException;

}
