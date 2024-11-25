package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.PrefixReflectionRowMapper;

public class EntityBuilderTestCase extends EntityBuilderTSupport {
	
	@Test
	public void testNormalOperationWithStandardMappers() throws SQLException {
		// Columns:
		// InvoiceId, InvoiceNumber, BillId, ItemNumber, ChargeId, Value
		
		ResultSet rs = createMock(ResultSet.class);

		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		
		// first row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("InvoiceNumber")).andReturn("inv_1111");
		expect(rs.getObject("BillId")).andReturn(1);
		expect(rs.getObject("ItemNumber")).andReturn("bill_1111");
		expect(rs.getObject("ChargeId")).andReturn(1);
		expect(rs.getDouble("Value")).andReturn(10d);
		
		// second row invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("InvoiceNumber")).andReturn("inv_2222");
		expect(rs.getObject("BillId")).andReturn(20);
		expect(rs.getObject("ItemNumber")).andReturn("bill_XXXX");
		expect(rs.getObject("ChargeId")).andReturn(null);
		
		// 3rd row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("BillId")).andReturn(2);
		expect(rs.getObject("ItemNumber")).andReturn("bill_222");
		expect(rs.getObject("ChargeId")).andReturn(null);		

		// 4th row has a null billid for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("BillId")).andReturn(null);
		expect(rs.getObject("ChargeId")).andReturn(null);
		
		// 5th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("BillId")).andReturn(40);
		expect(rs.getObject("ItemNumber")).andReturn("bill_YYYY");
		expect(rs.getObject("ChargeId")).andReturn(null);

		// 6th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("BillId")).andReturn(60);
		expect(rs.getObject("ItemNumber")).andReturn("bill_ZZZZ");
		expect(rs.getObject("ChargeId")).andReturn(null);
		
		// 7th row for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("BillId")).andReturn(1);
		expect(rs.getObject("ChargeId")).andReturn(2);
		expect(rs.getDouble("Value")).andReturn(20d);
		
		
		// no more rows
		expect(rs.next()).andReturn(false);
		rs.close();
		
		Node<EntityDescriptor> ed = 
			new SimpleNode<>(EntityDescriptor.forColumnPk(InvoiceParentEntity.class, new InvoiceParentEntity.InvoiceRowMapper(), "InvoiceId") )
				.addChild(
						new SimpleNode<>(EntityDescriptor.forColumnPk(BillParentEntity.class, new BillParentEntity.BillRowMapper(), "BillId"))
							.addChild(new SimpleNode<>(EntityDescriptor.forColumnPk(Charge.class, new Charge.ChargeRowMapper(), "ChargeId")))
						)
					;
		runForNode(ed, rs, rs);
		
	}
	
	@Test
	public void testNormalOperationWithReflectionMappers() throws SQLException {
		// Columns:
		// InvoiceId, InvoiceNumber, BillId, ItemNumber, ChargeId, Value
		
		Map<String, Integer> map = new LinkedHashMap<>();
		int i = 1;
		map.put("i_InvoiceId", i++);
		map.put("i_InvoiceNumber", i++);
		map.put("b_BillId", i++);
		map.put("b_ItemNumber", i++);
		map.put("c_ChargeId", i++);
		map.put("c_Value", i);
		
		ResultSetMetaData meta = createMock(ResultSetMetaData.class);
		expect(meta.getColumnCount()).andReturn(map.size()).anyTimes();
		for (Entry<String, Integer> entry: map.entrySet()) {
			expect(meta.getColumnName(entry.getValue())).andReturn(entry.getKey()).anyTimes();
		}
		
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getMetaData()).andReturn(meta).anyTimes();
		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		// first row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("i_InvoiceNumber")).andReturn("inv_1111");
		expect(rs.getObject("b_BillId")).andReturn(1);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_1111");
		expect(rs.getObject("c_ChargeId")).andReturn(1);
		expect(rs.getDouble("c_Value")).andReturn(10d);
		
		// second row invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("i_InvoiceNumber")).andReturn("inv_2222");
		expect(rs.getObject("b_BillId")).andReturn(20);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_XXXX");
		expect(rs.getObject("c_ChargeId")).andReturn(null);
		
		// 3rd row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("b_BillId")).andReturn(2);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_222");
		expect(rs.getObject("c_ChargeId")).andReturn(null);		

		// 4th row has a null billid for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("b_BillId")).andReturn(null);
		expect(rs.getObject("c_ChargeId")).andReturn(null);
		
		// 5th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("b_BillId")).andReturn(40);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_YYYY");
		expect(rs.getObject("c_ChargeId")).andReturn(null);

		// 6th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID2);
		expect(rs.getObject("b_BillId")).andReturn(60);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_ZZZZ");
		expect(rs.getObject("c_ChargeId")).andReturn(null);
		
		// 7th row for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getObject("i_InvoiceId")).andReturn(INVOICE_ID);
		expect(rs.getObject("b_BillId")).andReturn(1);
		expect(rs.getObject("c_ChargeId")).andReturn(2);
		expect(rs.getDouble("c_Value")).andReturn(20d);
		
		
		// no more rows
		expect(rs.next()).andReturn(false);
		rs.close();
		
		
		PrefixReflectionRowMapper<InvoiceParentEntity> im = new PrefixReflectionRowMapper<>(InvoiceParentEntity.class, "i_");
		im.ignore("InvoiceId");
		PrefixReflectionRowMapper<BillParentEntity> ib = new PrefixReflectionRowMapper<>(BillParentEntity.class, "b_");
		ib.ignore("BillId");
		PrefixReflectionRowMapper<Charge> ic = new PrefixReflectionRowMapper<>(Charge.class, "c_");
		ic.ignore("ChargeId");

		Node<EntityDescriptor> ed = 
				new SimpleNode<>(EntityDescriptor.forColumnPk(InvoiceParentEntity.class, im, "i_InvoiceId") )
					.addChild(
							new SimpleNode<>(EntityDescriptor.forColumnPk(BillParentEntity.class, ib, "b_BillId"))
								.addChild(new SimpleNode<>(EntityDescriptor.forColumnPk(Charge.class, ic, "c_ChargeId")))
							)
						;
		runForNode(ed, rs, rs, meta);
	}
	
	
	
}
