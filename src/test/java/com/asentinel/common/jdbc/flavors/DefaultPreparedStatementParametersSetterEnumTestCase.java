package com.asentinel.common.jdbc.flavors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Test;
import org.springframework.jdbc.core.SqlTypeValue;

import com.asentinel.common.jdbc.EnumId;

public class DefaultPreparedStatementParametersSetterEnumTestCase {
	
	private final DefaultPreparedStatementParametersSetter paramSetter = new DefaultPreparedStatementParametersSetter();
	private final PreparedStatement ps = mock(PreparedStatement.class);
	
	@Test
	public void plainEnumSet() throws SQLException {
		paramSetter.setParameter(ps, 1, SqlTypeValue.TYPE_UNKNOWN, PlainEnum.B);
		verify(ps).setString(1, "B");
		verifyNoMoreInteractions(ps);
	}

	@Test
	public void enumIdEnumSet() throws SQLException {
		paramSetter.setParameter(ps, 1, SqlTypeValue.TYPE_UNKNOWN, EnumIdEnum.B);
		verify(ps).setObject(1, 2);
		verifyNoMoreInteractions(ps);
	}

	
	private enum PlainEnum {
		A, B, C
	}
	
	private enum EnumIdEnum implements EnumId<Integer> {
		A(1), B(2), C(3);
		
		private final int id;
		
		EnumIdEnum(int id) {
			this.id = id;
		}

		@Override
		public Integer getId() {
			return id;
		}
	}

}
