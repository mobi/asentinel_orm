package com.asentinel.common.orm.mappers;

import java.util.Objects;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.persist.SimpleUpdater;
import com.asentinel.common.util.Assert;

/**
 * {@code TypeDescriptor} subclass that encapsulates an {@link SqlParameter}
 * allowing a converter registered with the {@link ConversionService} to perform
 * custom conversions to SQL types.
 * 
 * @see SimpleUpdater
 * @see SqlParam
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
public class SqlParameterTypeDescriptor extends TypeDescriptor {
	private static final long serialVersionUID = 1L;
	
	private final SqlParameter sqlParameter;

	public SqlParameterTypeDescriptor(SqlParam sqlParam) {
		// TODO: once we add the other values in the annotation we should copy them in the SqlParameter
		this(new SqlParameter(JdbcUtils.TYPE_UNKNOWN, sqlParam.value()));
	}
	
	public SqlParameterTypeDescriptor(SqlParameter sqlParameter) {
		super(ResolvableType.forClass(Object.class), null, null);
		Assert.assertNotNull(sqlParameter, "sqlParameter");
		this.sqlParameter = sqlParameter;
	}
	
	public SqlParameter getSqlParameter() {
		return sqlParameter;
	}
	
	public String getTypeName() {
		return sqlParameter.getTypeName();
	}
	
	// @implNote: hashCode and equals are important, the ConversionService might cache TypeDescriptors
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(sqlParameter.getTypeName());
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
		SqlParameterTypeDescriptor other = (SqlParameterTypeDescriptor) obj;
		String otherSqlParameter = other.sqlParameter == null ? null : other.sqlParameter.getTypeName();
		return Objects.equals(sqlParameter.getTypeName(), otherSqlParameter);
	}
	
	@Override
	public String toString() {
		return "SqlParameterTypeDescriptor ["
				+ "sqlParameterTypeName=" + sqlParameter.getTypeName()
				+ "]";
	}
	
	public static boolean isCustomConversion(Column column) {
		if (column == null) {
			return false;
		}
		return StringUtils.hasText(column.sqlParam().value());
	}
	
	public static boolean isCustomConversion(DynamicColumn column) {
		if (column == null) {
			return false;
		}
		return column.getSqlParameter() != null;
	}

}
