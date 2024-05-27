package com.asentinel.common.jdbc.arrays;

import java.sql.PreparedStatement;
import java.util.Collection;

import com.asentinel.common.jdbc.InOutCall;

/**
 * Adapter class for passing a number array or collection to a {@link PreparedStatement}.
 *
 * @see InOutCall
 * @see Array
 * 
 * @author Razvan Popian
 */
public class NumberArray extends Array {
	
	public NumberArray(String sqlTypeName, Number ... numbers) {
		super(sqlTypeName, numbers);
	}

	public NumberArray(String sqlTypeName, Collection<? extends Number> numbers) {
		super(sqlTypeName, convertToArray(numbers));
	}
	
	public NumberArray(String sqlTypeName, int ... primitives) {
		super(sqlTypeName, convert(primitives));
	}

	public NumberArray(String sqlTypeName, long ... primitives) {
		super(sqlTypeName, convert(primitives));
	}

	public NumberArray(String sqlTypeName, byte ... primitives) {
		super(sqlTypeName, convert(primitives));
	}

	public NumberArray(String sqlTypeName, short ... primitives) {
		super(sqlTypeName, convert(primitives));
	}
	
	public NumberArray(String sqlTypeName, float ... primitives) {
		super(sqlTypeName, convert(primitives));
	}

	public NumberArray(String sqlTypeName, double ... primitives) {
		super(sqlTypeName, convert(primitives));
	}
	
	
	private static Number[] convertToArray(Collection<? extends Number> numbers) {
		if (numbers != null) {
			return numbers.toArray(new Number[numbers.size()]);
		} else {
			return null;
		}
	}
	
	
	
	// static primitive array to Number[] conversion methods
	
	private static Number[] convert(int[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}

	private static Number[] convert(long[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}

	private static Number[] convert(byte[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}
	
	private static Number[] convert(short[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}
	

	private static Number[] convert(float[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}

	private static Number[] convert(double[] primitives) {
		if (primitives == null) {
			return null;
		}
		Number[] numbers = new Number[primitives.length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = primitives[i];
		}
		return numbers;
	}
	
}
