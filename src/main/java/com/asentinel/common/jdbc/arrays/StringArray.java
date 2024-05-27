package com.asentinel.common.jdbc.arrays;

import java.sql.PreparedStatement;
import java.util.Collection;

import com.asentinel.common.jdbc.InOutCall;

/**
 * Adapter class for passing a string array or collection to a {@link PreparedStatement}.
 *
 * @see InOutCall
 * @see Array
 * 
 * @author Razvan Popian
 */
public class StringArray extends Array {
	
	public StringArray(String sqlTypeName, String ... strings) {
		super(sqlTypeName, strings);
	}

	public StringArray(String sqlTypeName, Collection<? extends String> strings) {
		super(sqlTypeName, convertToArray(strings));
	}
	
	private static String[] convertToArray(Collection<? extends String> strings) {
		if (strings != null) {
			return strings.toArray(new String[strings.size()]);
		} else {
			return null;
		}
	}
}
