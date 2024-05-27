package com.asentinel.common.jdbc;

/**
 * Default strategy used by the {@link SqlQueryTemplate} class
 * for boolean IN parameters conversion.
 * 
 * @see BooleanParameterConverter
 * @see SqlQueryTemplate
 * 
 * @author Razvan Popian
 */
public class DefaultBooleanParameterConverter implements BooleanParameterConverter<String> {

	@Override
	public String asObject(boolean b) {
		return b?"Y":"N";
	}

}
