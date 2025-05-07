package com.asentinel.common.orm.mappers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.SqlParameter;

/**
 * Annotation holding information about the type of the mapped database column
 * if the column is not a standard SQL type. It should be used together with a
 * {@link ConversionService} when custom conversion between the java type and
 * the SQL type is needed (both ways). The value of the annotation will be used
 * to construct a {@link SqlParameterTypeDescriptor} instance that will be used
 * by converters registered with the {@code ConversionService}. Below is an
 * example on how to save and load a Postgres JSON column. <br>
 * <br>
 * Given the following entities (getters/setters omitted for brevity):
 * 
 * <pre>
 * 	&amp;Table("TestJson")
 *	public class TestJson {
 *		
 *		&amp;PkColumn("id")
 *		private int id;
 *		
 *		&amp;Column(value = "StaticJson", sqlParam = @SqlParam("json"))
 *		private Employee staticJson;
 *
 *		.....
 *	}
 *	
 *	public class Employee {
 *		private String firstName;
 *		private String lastName;
 *		
 *		.....
 *	}
 * 
 * </pre>
 * 
 * and the following converters registered with the {@code ConversionService}
 * injected in the ORM beans ({@code SimpleUpdater} and
 * {@code DefaultEntityDescriptorTreeRepository}):
 * 
 * <pre>
 * public class JsonToEmployeeConverter implements Converter&lt;PGobject, Employee&gt; {
 * 	// Jackson ObjectMapper
 * 	private final ObjectMapper mapper = new ObjectMapper();
 * 
 * 	&amp;Override
 * 	public Employee convert(PGobject source) {
 * 		try {
 * 			return mapper.readValue(source.getValue(), Employee.class);
 * 		} catch (JsonProcessingException e) {
 * 			throw new IllegalArgumentException("Failed to convert from JSON.", e);
 * 		}
 * 	}
 * }
 *
 * public class EmployeeToJsonConverter implements ConditionalGenericConverter {
 * 	// Jackson ObjectMapper
 * 	private final ObjectMapper mapper = new ObjectMapper();
 * 
 * 	&amp;Override
 * 	public Set<ConvertiblePair> getConvertibleTypes() {
 * 		return null;
 * 	}
 * 
 * 	&amp;Override
 * 	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
 * 		String s;
 * 		try {
 * 			s = mapper.writeValueAsString(source);
 * 			PGobject pgo = new PGobject();
 * 			pgo.setType("jsonb");
 * 			pgo.setValue(s);
 * 			return pgo;
 * 		} catch (JsonProcessingException | SQLException e) {
 * 			throw new IllegalArgumentException("Failed to convert to JSON.", e);
 * 		}
 * 	}
 * 
 * 	&amp;Override
 * 	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
 * 		if (sourceType.getType() != Employee.class) {
 * 			return false;
 * 		}
 * 		if (!(targetType instanceof SqlParameterTypeDescriptor)) {
 * 			return false;
 * 		}
 * 
 * 		SqlParameterTypeDescriptor typeDescriptor = (SqlParameterTypeDescriptor) targetType;
 * 		if ("json".equals(typeDescriptor.getTypeName())) {
 * 			return true;
 * 		}
 * 
 * 		return false;
 * 	}
 * 
 * }
 * </pre>
 * 
 * the following code will save and load the instances of {@code TestJson}:
 * 
 * <pre>
 * 	.....
 * 	TestJson tj = new TestJson();
 *	tj.setStaticJson(new Employee("John", "Doe"));
 *		
 *	orm.update(tj);
 *		
 *	TestJson tj2 = orm.newSqlBuilder(TestJson.class)
 *		.select()
 *		.execForEntity();
 * 	.....
 * </pre>
 * 
 * @see Column
 * @see SqlParameter
 * @see SqlParameterTypeDescriptor
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqlParam {

	// TODO: add the other attributes of the SqlParameter class
	
	/**
	 * @return information to be used by the {@code ConversionService} to perform
	 *         the conversion from the java type to the database type. The default
	 *         is empty indicating that the default conversion should be performed.
	 *         The value will be used to create an {@link SqlParameter} instance
	 *         with the type name equal to the value.
	 * 
	 * @see SqlParameter#getTypeName()
	 */
	String value() default "";

}
