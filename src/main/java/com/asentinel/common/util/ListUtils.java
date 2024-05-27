package com.asentinel.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author Razvan Popian
 */
public final class ListUtils {
	private ListUtils() {}
	
	
	/**
	 * @param collection collection of elements that need to be converted to CSV.
	 * @return String containing the objects in the list parameters converted to strings
	 * 			and separated by commas.
	 */
	public static String toCsvString(Collection<?> collection) {
		if (collection == null || collection.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(collection.size() * 2);
		for (Object o:collection) {
			sb.append("," + o.toString());
		}
		return sb.substring(1);
	}
	
	/**
	 * @param collection
	 * @return string representation of the <code>Collection</code> parameter with elements 
	 * 			separated by comma and <code>\n</code>.
	 */
	public static String toString(Collection<?> collection) {
		if (collection == null) {
			return "";
		}
		final String sep = ",\n";
		StringBuilder sb = new StringBuilder();
		for (Object o:collection) {
			sb.append(o).append(sep);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - sep.length(), sb.length());
		}
		sb.append("\n");
		return sb.toString();
	}
	
	
	/**
	 * This method splits the list of objects received as parameter into 
	 * multiple sublists (rows) according to the columns parameter.
	 * @param <T> - type o the objects in the list.
	 * @param list
	 * @param columns
	 * @return List of List<T> resulted from splitting the list parameter.
	 * @see #toMultiRowList(List, int)
	 */
	public static <T> List<List<T>> toMultiRowList(List<T> list, int columns) {
		Assert.assertNotNull(list, "list");
		Assert.assertStrictPositive(columns, "columns");
		List<List<T>> rows = new ArrayList<List<T>>(list.size()/columns + 1);
		List<T> row = new ArrayList<T>();
		for (int i=0; i < list.size(); i++) {
			if (i != 0 && i % columns == 0) {
				rows.add(row);
				row = new ArrayList<T>();
			}
			row.add(list.get(i));
		}
		if (row.size() != 0) {
			rows.add(row);
		}
		return rows;
	}
	
	/**
	 * Calls {@link #toMultiRowListSymetric(List, int)} with columns value 2. 
	 * @see #toMultiRowListSymetric(List, int) 
	 * @param <T>
	 * @param list
	 * @return List of List<T> resulted from splitting the list parameter.
	 */
	public static <T> List<List<T>> toMultiRowListSymetric(List<T> list) {
		return toMultiRowListSymetric(list, 2);
	}
	
	/**
	 * This method calls {@link #toMultiRowList(List, int)} and fills the last row
	 * returned by this method with nulls up to the required columns size
	 * @see #toMultiRowList(List, int)
	 * @param <T>
	 * @param list
	 * @param columns
	 * @return List of List<T> resulted from splitting the list parameter.
	 */
	public static <T> List<List<T>> toMultiRowListSymetric(List<T> list, int columns) {
		List<List<T>> list2 = toMultiRowList(list, columns);
		if (list2.isEmpty()) {
			return list2;
		}
		List<T> list3 = list2.get(list2.size() - 1);
		int size = columns - list3.size();
		for (int i=0; i<size; i++) {
			list3.add(null);
		}
		return list2;
	}
	
	
	
	// ---------------------- CSV conversion to lists methods ------------------------------- //
	
	/**
	 * Method that converts a csv String to a list of T objects.
	 * @param csvString the csv string to process.
	 * @param clasz
	 * @return list of type T, one element for each element in the delimited string.
	 * @see #toList(String, String, Class)
	 * @see #toList(String, String, ElementParser)
	 *
	 * @throws IllegalArgumentException if an element can not be converted to the 
	 * 		required type or if the specified Class is not supported.
	 */
	public static <T> List<T> toList(String csvString, Class<T> clasz) 
			throws IllegalArgumentException {
		return toList(csvString, ",", clasz);
	}
	
	/**
	 * Method that converts a delimited String to a list of T objects. 
	 * @param delimitedString the string to process.
	 * @param delimiter the delimiter
	 * @param clasz the class to convert each element to. For now this
	 * 			method supports String.class, Integer.class and Long.class.
	 * @return list of type T, one element for each element in the delimited string. If T is
	 * 			Number or subclass of Number and the delimitedString parameter is empty string
	 * 			or null an empty list is returned. If T is String and the delimitedString parameter 
	 * 			is empty string a list containing 1 empty string is returned.
	 *  
	 * @throws IllegalArgumentException if an element can not be converted to the 
	 * 		required type or if the specified Class is not supported.
	 * 
	 * @see #toList(String, String, ElementParser)
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(String delimitedString, String delimiter, Class<T> clasz) 
			throws IllegalArgumentException {
		Assert.assertNotNull(clasz, "clasz");
		if (Number.class.isAssignableFrom(clasz)) {
			// return empty list if a list of numbers is expected and
			// the delimited string is empty.
			if (!StringUtils.hasLength(delimitedString)) {
				return Collections.emptyList();
			}
		}
		if (clasz == String.class) {
			return (List<T>) toList(delimitedString, delimiter, STRING_ELEMENT_READER);
		} else if (clasz == Integer.class) {
			return (List<T>) toList(delimitedString, delimiter, INTEGER_ELEMENT_READER);
		} else if (clasz == Long.class) {
			return (List<T>) toList(delimitedString, delimiter, LONG_ELEMENT_READER);
		} else {
			throw new IllegalArgumentException("Class " + clasz.getName()  + " not supported.");
		}
	}
	
	
	/**
	 * Method that converts a delimited String to a list using the 
	 * elementParser strategy.
	 * @param delimitedString the string to process.
	 * @param delimiter the delimiter
	 * @param elementParser the strategy for converting each string element to type T.
	 * @return List containing the elements in the delimited string as
	 * 			resulted from parsing each of them using the elementParser. 
	 * @throws IllegalArgumentException if the parser strategy can not convert an element
	 * 			to the required type.
	 * 
	 * @see {@link ElementParser}
	 */
	public static <T> List<T> toList(String delimitedString, String delimiter, ElementParser<T> elementParser) 
			throws IllegalArgumentException {
		Assert.assertNotNull(elementParser, "elementParser");
		Assert.assertNotNull(delimiter, "delimiter");
		if (delimitedString == null) {
			return Collections.emptyList();
		}
		String[] elements = delimitedString.split(delimiter);
		List<T> list = new ArrayList<T>(elements.length);
		for(String element:elements) {
			list.add(elementParser.parse(element));
		}
		return list;
	}
	
	/**
	 * Strategy interface used by the #toList(String, String, ElementParser) method
	 * to convert an element in a delimited string to type T.  
	 *
	 * @param <T> type to convert to.
	 */
	public static interface ElementParser<T> {
		public T parse(String element) throws IllegalArgumentException;
	}

	private static final ElementParser<String> STRING_ELEMENT_READER = new ElementParser<String>() {
		@Override
		public String parse(String element) throws IllegalArgumentException {
			return element;
		}
	};
	
	private static final ElementParser<Integer> INTEGER_ELEMENT_READER = new ElementParser<Integer>() {
		@Override
		public Integer parse(String element) throws IllegalArgumentException {
			return Integer.parseInt(element.trim());
		}
	};

	private static final ElementParser<Long> LONG_ELEMENT_READER = new ElementParser<Long>() {
		@Override
		public Long parse(String element) throws IllegalArgumentException {
			return Long.parseLong(element.trim());
		}
	};
	
}
