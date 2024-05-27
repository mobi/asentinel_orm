package com.asentinel.common.orm;

import java.util.function.UnaryOperator;

import com.asentinel.common.util.Assert;

/**
 * @see SimpleEntityDescriptor.Builder#formulas(java.util.Map)
 * @see QueryReady#getFormula(String)
 * 
 * @since 1.65.0
 * @author Razvan Popian
 */
public class Formula implements UnaryOperator<String> {
	
	private final String template;
	
	/**
	 * Constructor that takes the formula template as parameter. The template should
	 * contain exactly one {@code %s} format placeholder for the column name. Some
	 * valid example templates are {@code "1 + %s + 3"} or {@code "formula(%s)"}.
	 */
	public Formula(String template) {
		Assert.assertNotEmpty(template, "template");
		this.template = template;
	}

	@Override
	public String apply(String columnName) {
		return String.format(template, columnName);
	}

}
