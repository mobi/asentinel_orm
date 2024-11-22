package com.asentinel.common.orm.mappers.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class DynamicColumnsEntityDescriptorTestCase {
	
	private static final List<DefaultDynamicColumn> dynamicColumns = List.of(
		new DefaultDynamicColumn("DynamicColumn", Integer.class),
		new DefaultDynamicColumn("DynamicColumnInputStream", InputStream.class)
	);
	
	@Test
	public void getColumns_EntityDescriptorPreparedForProxyingInputStreams() {
		DynamicColumnsEntityDescriptor<DefaultDynamicColumn, DynamicColumnsEntity<DefaultDynamicColumn>> ed 
			= new DynamicColumnsEntityDescriptor<>(dynamicColumns, null, 
				new SimpleEntityDescriptor.Builder(TestBean.class)
					.queryExecutor(mock(SqlQuery.class))
					.entityFactory(TestBean::new)
			);
		
		assertEquals(
			new HashSet<>(Arrays.asList("Id", "StaticColumn", "DynamicColumn")), 
			new HashSet<>(ed.getColumnNames())
		);
	}

	
	@Test
	public void getColumns_EntityDescriptorNOTPreparedForProxyingInputStreams() {
		DynamicColumnsEntityDescriptor<DefaultDynamicColumn, DynamicColumnsEntity<DefaultDynamicColumn>> ed 
			= new DynamicColumnsEntityDescriptor<>(dynamicColumns, null,
				new SimpleEntityDescriptor.Builder(TestBean.class)
					.queryExecutor(null)
					.entityFactory(TestBean::new)
			);
		
		assertEquals(
			new HashSet<>(Arrays.asList("Id", "StaticColumn", "StaticColumnInputStream", "DynamicColumn", "DynamicColumnInputStream")), 
			new HashSet<>(ed.getColumnNames())
		);
	}

	
	@Table("Test")
	private static class TestBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		
		@PkColumn("Id")
		int id;
		
		@Column("StaticColumn")
		int staticColumn;
		
		@Column("StaticColumnInputStream")
		InputStream staticColumnInputStream;
		
		int dynamicColumn;
		
		InputStream dynamicColumnInputStream;
		

		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			if (column == dynamicColumns.get(0)) {
				this.dynamicColumn = (int) value;
			} else if (column == dynamicColumns.get(1)) {
				this.dynamicColumnInputStream = (InputStream) value;
			} else {
				fail("Unknown column");
			}
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			if (column == dynamicColumns.get(0)) {
				return dynamicColumn;
			} else if (column == dynamicColumns.get(1)) {
				return dynamicColumnInputStream;
			} else {
				fail("Unknown column");
			}
			return null; // to keep the compiler happy
		}
	}
}
