package com.asentinel.common.orm;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.orm.jql.SqlBuilder;
import com.asentinel.common.orm.jql.SqlBuilderFactory;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class AutoLazyLoaderTestCaseSqlBuilderFactoryFocusedTestCase {
	
	@SuppressWarnings("unchecked")
	SqlBuilder<Bill> sb = mock(SqlBuilder.class);
	
	SqlBuilderFactory sbfMember = mock(SqlBuilderFactory.class);
	SqlBuilderFactory sbfParam = mock(SqlBuilderFactory.class);
	
	Node<EntityDescriptor> parent = new SimpleNode<>();
	Node<EntityDescriptor> child = new SimpleNode<>();
	
	{
		parent.setValue(new SimpleEntityDescriptor.Builder(Invoice.class).build());
		parent.addChild(child);
	}

	private void test(SqlBuilderFactory sbf) throws SQLException {
		assertTrue(child.getValue() instanceof ProxyEntityDescriptor);
		@SuppressWarnings("unchecked")
		ProxyEntityDescriptor<Bill> ped = (ProxyEntityDescriptor<Bill>) child.getValue();

		RowMapper<?> rm = ped.getEntityRowMapper();
		
		ResultSet rs = mock(ResultSet.class);
		Bill bill = (Bill) rm.mapRow(rs, 1);
		
		when(sbf.newSqlBuilder(Bill.class)).thenReturn(sb);
		when(sb.select()).thenReturn(sb);
		when(sb.where()).thenReturn(sb);
		when(sb.id()).thenReturn(sb);
		when(sb.eq(anyInt())).thenReturn(sb);
		when(sb.execForEntity()).thenReturn(new Bill());
		bill.getItemNumber();
		assertTrue(EntityUtils.isLoadedProxy(bill));
	}
	
	@Test(expected = IllegalStateException.class)
	public void noMemberOrParameter() {
		AutoLazyLoader all = new AutoLazyLoader();
		all.replaceWithProxyEntityDescriptor(child, 
				new SimpleEntityDescriptor.Builder(Bill.class), 
				null);
	}
	
	@Test
	public void MemberSetAndParameterNull() throws SQLException {
		AutoLazyLoader all = new AutoLazyLoader(sbfMember);
		all.replaceWithProxyEntityDescriptor(child, 
				new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.ONE_TO_MANY).tableAlias("X"), 
				null);
		test(sbfMember);
	}

	@Test
	public void MemberNullAndParameterSet() throws SQLException {
		AutoLazyLoader all = new AutoLazyLoader();
		all.replaceWithProxyEntityDescriptor(child, 
				new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.ONE_TO_MANY).tableAlias("X"), 
				sbfParam);
		test(sbfParam);
	}
	
	@Test
	public void bothMemberAndParameterAreSet() throws SQLException {
		AutoLazyLoader all = new AutoLazyLoader(sbfMember);
		SimpleEntityDescriptor.Builder b = new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.ONE_TO_MANY).tableAlias("X");
		all.replaceWithProxyEntityDescriptor(child, 
				b, 
				sbfParam);
		test(sbfParam);
		
		// ensure build was not called on the builder param
		assertNull(b.getTableName());
		
		verifyNoMoreInteractions(sbfMember);
	}
	
}
