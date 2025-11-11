package com.asentinel.common.jdbc;

/**
 * Strategy interface used to convert a boolean to an Object. The 
 * reason for this interface existence is the fact that the Oracle
 * database does not support boolean columns, so a database designer
 * usually gets around this issue by using either a VARCHAR column containing
 * either 'Y' or 'N', or by using an int column. This interface allows
 * the client to pass boolean parameters to the methods defined in 
 * {@link SqlQueryTemplate} class.
 * 
 * @see DefaultBooleanParameterConverter
 * @see SqlQueryTemplate
 * @see SqlQuery
 * 
 * @author Razvan Popian
 */
public interface BooleanParameterConverter<T> {
	
	/**
	 * Object that should be passed to an InOutCall method
	 * that needs to call a stored procedure with a NULL boolean.
	 */
	Object NULL_BOOLEAN = new Object() {
		@Override
		public String toString() {
			return "NULL_BOOLEAN";
		}
	};
	
	T asObject(boolean b);
	
	default T asObject(Boolean b) {
		if (b == null) {
			return null;
		}
		return this.asObject(b.booleanValue());
	}

}
