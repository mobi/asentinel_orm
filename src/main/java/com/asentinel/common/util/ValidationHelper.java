package com.asentinel.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValidationHelper {
	
	/**
	 * Tests if the provided <code>email</code>
	 * is valid.
	 * @param email
	 * @return true if the email is valid, false otherwise.
	 */
	public static boolean isEmailValid(String email) {
		if (email == null) {
			return false;
		}
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(email);
	    return m.matches();	      
	}

	private ValidationHelper(){}

}
