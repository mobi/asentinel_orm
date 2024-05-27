package com.asentinel.common.orm.jql;

import com.asentinel.common.util.Assert;

/**
 * @see InstructionType
 * @see Instructions
 * 
 * @author Razvan Popian
 */
class Instruction {
	private final InstructionType type;
	private final Object actual;
	
	public Instruction(InstructionType type, Object actual) {
		Assert.assertNotNull(type, "type");
		this.type = type;
		this.actual = actual;
	}

	/**
	 * @return the instruction type.
	 */
	public InstructionType getType() {
		return type;
	}

	/**
	 * @return parameters of the instruction if any. Can return
	 * 		any type of <code>Object</code>, the {@link Instructions} class
	 * 		will figure out what this object represents based on 
	 * 		the instruction type.
	 */
	public Object getActual() {
		return actual;
	}

	@Override
	public String toString() {
		return "Instruction [type=" + type + "]";
	}
}
