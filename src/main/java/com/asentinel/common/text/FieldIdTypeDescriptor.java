package com.asentinel.common.text;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

/**
 * {@code TypeDescriptor} subclass that can store additional custom information
 * about the field to be converted - usually some field identifier. It is useful for 
 * bridging the gap between the {@link Converter} interface and the 
 * Spring {@link ConversionService} system. It allows {@code Converters} to have
 * access to the custom field identifier.
 * 
 * @author Razvan Popian
 */
public class FieldIdTypeDescriptor extends TypeDescriptor {

	private static final long serialVersionUID = 1L;
	
	private final Object fieldId;

	public FieldIdTypeDescriptor(Object fieldId, Class<?> type) {
		super(ResolvableType.forClass(type), null, null);
		this.fieldId = fieldId;
	}
	
	public Object getFieldId() {
		return fieldId;
	}
	
	// @implNote: hashCode and equals are important, the ConversionService might cache TypeDescriptors

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fieldId == null) ? 0 : fieldId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldIdTypeDescriptor other = (FieldIdTypeDescriptor) obj;
		if (fieldId == null) {
			if (other.fieldId != null)
				return false;
		} else if (!fieldId.equals(other.fieldId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "FieldIdTypeDescriptor ["
				+ "fieldId=" + fieldId
				+ ", type = " + getType() == null ? null : getType().getName()
				+ "]";
	}
}
