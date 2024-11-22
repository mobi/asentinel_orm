package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.PrefixReflectionRowMapper;
import com.asentinel.common.jdbc.ResultSetUtils;
import com.asentinel.common.orm.mappers.ColumnRowMapper;

public class EntityBuilder2TestCase {

	private static final BigDecimal INVOICE_ID = new BigDecimal("1");
	
	private static final BigDecimal BILL_ID = new BigDecimal("100");

	/**
	 * Tests the scenario when a parent entity has the same child for 2 entity descriptors. 
	 */
	@Test
	public void testSameObjectAsChild() throws SQLException {
		PrefixReflectionRowMapper<Invoice2> invoiceRm = new PrefixReflectionRowMapper<Invoice2>(Invoice2.class, "i_");
		PrefixReflectionRowMapper<BillParentEntity> bill1Rm = new PrefixReflectionRowMapper<BillParentEntity>(BillParentEntity.class, "b1_");
		PrefixReflectionRowMapper<BillParentEntity> bill2Rm = new PrefixReflectionRowMapper<BillParentEntity>(BillParentEntity.class, "b2_");
		invoiceRm.ignore("i_InvoiceId");
		bill1Rm.ignore("b1_BillId");
		bill2Rm.ignore("b2_BillId");
		
		Node<EntityDescriptor> invoiceEd = new SimpleNode<EntityDescriptor>(
				new EntityDescriptor(Invoice2.class, new ColumnRowMapper("i_InvoiceId"), invoiceRm, "invoice")
		);
		Node<EntityDescriptor> bill1Ed = new SimpleNode<EntityDescriptor>(
				new EntityDescriptor(BillParentEntity.class, new ColumnRowMapper("b1_BillId"), bill1Rm, "bill1")
		);
		Node<EntityDescriptor> bill2Ed = new SimpleNode<EntityDescriptor>(
				new EntityDescriptor(BillParentEntity.class, new ColumnRowMapper("b2_BillId"), bill2Rm, "bill2")				
		);
		invoiceEd.addChild(bill1Ed).addChild(bill2Ed);	
		
		EntityBuilder<Invoice2> handler = new EntityBuilder<Invoice2>(invoiceEd);

		
		String[] cols = new String[] {
			"i_InvoiceId",
			"i_InvoiceNumber",
			"b1_BillId",
			"b1_ItemNumber",
			"b2_BillId",
			"b2_ItemNumber"
		};
		
		
		ResultSetMetaData meta = createMock(ResultSetMetaData.class);
		expect(meta.getColumnCount()).andReturn(cols.length).anyTimes();
		
		int i = 1;
		for (String col: cols) {
			expect(meta.getColumnName(i++)).andReturn(col).anyTimes();
		}

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getMetaData()).andReturn(meta).anyTimes();
		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		
		// first row invoice 1 with 2 bills on the same row (same bills)
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("i_InvoiceNumber")).andReturn("inv_1111");
		expect(rs.getObject("b1_BillId")).andReturn(BILL_ID);
		expect(rs.getObject("b1_ItemNumber")).andReturn("bill_1111");
		expect(rs.getObject("b2_BillId")).andReturn(BILL_ID);
		
		// no more rows
		expect(rs.next()).andReturn(false);
		rs.close();
		
		replay(rs, meta);
		ResultSetUtils.processResultSet(rs, handler);
		verify(rs, meta);
		
		Invoice2 invoice = handler.getEntity();
		
		assertNotNull(invoice.bill1);
		assertNotNull(invoice.bill2);
		assertSame(invoice.bill1, invoice.bill2);
		assertEquals(BILL_ID.intValue(), invoice.bill1.billId);
		assertEquals(BILL_ID.intValue(), invoice.bill2.billId);
		
	}
}
