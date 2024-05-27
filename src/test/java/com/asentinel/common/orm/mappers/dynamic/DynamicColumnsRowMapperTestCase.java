package com.asentinel.common.orm.mappers.dynamic;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.Test;

import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;

public class DynamicColumnsRowMapperTestCase {
	
	private final Collection<DefaultDynamicColumn> dynamicColumns = singleton(new DefaultDynamicColumn("DynamicColumn", Integer.class));
	
	private final ResultSet rs = mock(ResultSet.class);
	
	@Test
	public void normalOperation() throws SQLException {
		DynamicColumnsRowMapper<DefaultDynamicColumn, TestBean> mapper 
			= new DynamicColumnsRowMapper<>(dynamicColumns, 
					() -> new TestBean(/* normally here we would inject the dynamic columns in the real bean*/),
					false,
					"t_");
	
		when(rs.next()).thenReturn(true);
		when(rs.getInt("t_Id")).thenReturn(10);
		when(rs.getInt("t_StaticColumn")).thenReturn(20);
		when(rs.getInt("t_DynamicColumn")).thenReturn(30);
		TestBean b = mapper.mapRow(rs, 1);
		
		assertEquals(10, b.id);
		assertEquals(20, b.staticColumn);
		assertEquals(30, b.dynamicColumn);
	}
	
	@Test
	public void ignorePk() throws SQLException {
		DynamicColumnsRowMapper<DefaultDynamicColumn, TestBean> mapper 
		= new DynamicColumnsRowMapper<>(dynamicColumns, 
				() -> new TestBean(/* normally here we would inject the dynamic columns in the real bean*/),
				"t_");
	
		when(rs.next()).thenReturn(true);
		when(rs.getInt("t_StaticColumn")).thenReturn(20);
		when(rs.getInt("t_DynamicColumn")).thenReturn(30);
		TestBean b = mapper.mapRow(rs, 1);
		
		assertEquals(0, b.id);
		assertEquals(20, b.staticColumn);
		assertEquals(30, b.dynamicColumn);
	}	
	
	private static class TestBean implements DynamicColumnsEntity<DefaultDynamicColumn> {
		
		@PkColumn("Id")
		int id;
		
		@Column("StaticColumn")
		int staticColumn;
		
		int dynamicColumn;
		

		@Override
		public void setValue(DefaultDynamicColumn column, Object value) {
			this.dynamicColumn = (int) value;
			
		}

		@Override
		public Object getValue(DefaultDynamicColumn column) {
			return dynamicColumn;
		}
		
	}

}
