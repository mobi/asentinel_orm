package com.asentinel.common.orm.persist;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.SqlParam;
import com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.orm.mappers.dynamic.DefaultDynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumn;
import com.asentinel.common.orm.mappers.dynamic.DynamicColumnsEntity;

/**
 * Tests updating a single bean with {@code ConversionService}. We don't care
 * about the generated SQL as that's tested in the other tests in this package.
 * We just ensure that the right conversions occurred. Since the same methods
 * are used to create the arguments list for the batch updates, there is no need
 * for additional tests.
 * 
 * @since 1.71.0
 * @author Razvan Popian
 */
public class SimpleUpdaterConversionServiceTestCase {
	
	private final JdbcFlavor jdbcFlavor = new PostgresJdbcFlavor();
	
	private final SqlQuery ex = mock(SqlQuery.class);
	private final ConversionService cs = mock(ConversionService.class);
	private final SimpleUpdater updater = new SimpleUpdater(jdbcFlavor, ex);
	{
		updater.setConversionService(cs);
	}
	
	private final TypeDescriptor staticSourceDesc = TargetMembersHolder.getInstance().getTargetMembers(CustomTypeBean.class)
			.getColumnMembers().get(0).getTypeDescriptor();

	private final TypeDescriptor staticTargetDesc = new SqlParameterTypeDescriptor(
				TargetMembersHolder.getInstance().getTargetMembers(CustomTypeBean.class)
					.getColumnMembers().get(0).getColumnAnnotation().sqlParam()
			);

	
	private final DynamicColumn dc = new DefaultDynamicColumn("DynamicCustomObject", Person.class, "person");
	private final TypeDescriptor dynamicSourceDesc = dc.getTypeDescriptor();
	private final TypeDescriptor dynamicTargetDesc = new SqlParameterTypeDescriptor(dc.getSqlParameter());
	
	@Test
	public void nonNullValues() {
		CustomTypeBean bean = new CustomTypeBean();
		
		// static conversion calls
		when(cs.canConvert(eq(staticSourceDesc), eq(staticTargetDesc)))
			.thenReturn(true);
		when(cs.convert(eq(bean.staticCustomObject), eq(staticSourceDesc), eq(staticTargetDesc)))
			.thenReturn(bean.staticCustomObject.toString());
		
		// dynamic conversion calls
		when(cs.canConvert(eq(dynamicSourceDesc), eq(dynamicTargetDesc)))
			.thenReturn(true);
		when(cs.convert(eq(bean.dynamicCustomObject), eq(dynamicSourceDesc), eq(dynamicTargetDesc)))
			.thenReturn(bean.dynamicCustomObject.toString());
		
		when(ex.update(any(String.class),
				eq(bean.staticCustomObject.toString()),
				eq(bean.dynamicCustomObject.toString()),
				eq(bean.id))).thenReturn(1);
		
		assertEquals(1, updater.update(bean, new UpdateSettings<>(List.of(dc))));
	}

	@Test
	public void nullStaticValue() {
		CustomTypeBean bean = new CustomTypeBean();
		bean.staticCustomObject = null;
		
		// dynamic conversion calls
		when(cs.canConvert(eq(dynamicSourceDesc), eq(dynamicTargetDesc)))
			.thenReturn(true);
		when(cs.convert(eq(bean.dynamicCustomObject), eq(dynamicSourceDesc), eq(dynamicTargetDesc)))
			.thenReturn(bean.dynamicCustomObject.toString());
		
		when(ex.update(any(String.class),
				isNull(),
				eq(bean.dynamicCustomObject.toString()),
				eq(bean.id))).thenReturn(1);
		
		assertEquals(1, updater.update(bean, new UpdateSettings<>(List.of(dc))));
	}

	@Test
	public void nullDynamicValue() {
		CustomTypeBean bean = new CustomTypeBean();
		bean.dynamicCustomObject = null;
		
		// static conversion calls
		when(cs.canConvert(eq(staticSourceDesc), eq(staticTargetDesc)))
			.thenReturn(true);
		when(cs.convert(eq(bean.staticCustomObject), eq(staticSourceDesc), eq(staticTargetDesc)))
			.thenReturn(bean.staticCustomObject.toString());
		
		when(ex.update(any(String.class),
				eq(bean.staticCustomObject.toString()),
				isNull(),
				eq(bean.id))).thenReturn(1);
		
		assertEquals(1, updater.update(bean, new UpdateSettings<>(List.of(dc))));
	}

	
	@Table("TestTable")
	private static class CustomTypeBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		@PkColumn("id")
		int id = 11;

		@Column(value = "StaticCustomType", sqlParam = @SqlParam("person"))
		Person staticCustomObject = new Person("John", "Doe");
		
		Person dynamicCustomObject = new Person("Mary", "Jones");


		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			this.dynamicCustomObject = (Person) value;
			
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			return dynamicCustomObject;
		}
	}
	
	private static class Person {
		String firstName;
		String lastName;

		
		public Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		@Override
		public String toString() {
			return firstName + " " + lastName;
		}
		
	}
}
