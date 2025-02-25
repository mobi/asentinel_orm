package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.mappers.Child;

public class EntityBuilderWithSimpleEntityDescriptorsTestCase extends EntityBuilderTSupport {
	
	/**
	 * Tests the {@link EntityBuilder} with a generated EntityDescriptor tree. The tree
	 * is generated by recursively looking for the {@link Child} annotation in the {@link Invoice}
	 * class. All children are set using reflection, the {@link Invoice} and {@link Bill} classes
	 * do not implement {@link ParentEntity}. 
	 */
	@Test
	public void testNormalOperationWithSimpleEntityDescriptors() throws SQLException {
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
		
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getMetaData()).andReturn(meta).anyTimes();
		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		
		for (Entry<String, Integer> entry: map.entrySet()) {
			expect(meta.getColumnName(entry.getValue())).andReturn(entry.getKey()).anyTimes();
			expect(rs.findColumn(entry.getKey())).andReturn(entry.getValue()).anyTimes();
		}

		// first row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("i_InvoiceNumber")).andReturn("inv_1111");
		expect(rs.getInt("b_BillId")).andReturn(1);expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_1111");
		expect(rs.getInt("c_ChargeId")).andReturn(1);expect(rs.wasNull()).andReturn(false);
		expect(rs.getDouble("c_Value")).andReturn(10d);expect(rs.wasNull()).andReturn(false);
		
		// second row invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID2.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("i_InvoiceNumber")).andReturn("inv_2222");
		expect(rs.getInt("b_BillId")).andReturn(20);expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_XXXX");
		expect(rs.getInt("c_ChargeId")).andReturn(0);expect(rs.wasNull()).andReturn(true);
		
		// 3rd row invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("b_BillId")).andReturn(2);expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_222");
		expect(rs.getInt("c_ChargeId")).andReturn(0);expect(rs.wasNull()).andReturn(true);		

		// 4th row has a null billid for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("b_BillId")).andReturn(0);expect(rs.wasNull()).andReturn(true);
		expect(rs.getInt("c_ChargeId")).andReturn(0);expect(rs.wasNull()).andReturn(true);
		
		// 5th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID2.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("b_BillId")).andReturn(40);expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_YYYY");
		expect(rs.getInt("c_ChargeId")).andReturn(0);expect(rs.wasNull()).andReturn(true);

		// 6th row for invoice 2
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID2.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("b_BillId")).andReturn(60);expect(rs.wasNull()).andReturn(false);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_ZZZZ");
		expect(rs.getInt("c_ChargeId")).andReturn(0);expect(rs.wasNull()).andReturn(true);
		
		// 7th row for invoice 1
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("i_InvoiceId")).andReturn(INVOICE_ID.intValue());expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("b_BillId")).andReturn(1);expect(rs.wasNull()).andReturn(false);
		expect(rs.getInt("c_ChargeId")).andReturn(2);expect(rs.wasNull()).andReturn(false);
		expect(rs.getDouble("c_Value")).andReturn(20d);expect(rs.wasNull()).andReturn(false);
		
		// no more rows
		expect(rs.next()).andReturn(false);
		rs.close();

		
		Node<EntityDescriptor> ed = new DefaultEntityDescriptorTreeRepository().getEntityDescriptorTree(Invoice.class,
			new EntityDescriptorNodeCallback() {
				
				@Override
				public boolean customize(Node<EntityDescriptor> node, Builder builder) {
					if (Invoice.class.equals(builder.getEntityClass())) {
						builder.tableAlias("i");
					} else if (Bill.class.equals(builder.getEntityClass())) {
						builder.tableAlias("b");
					} else if (Charge.class.equals(builder.getEntityClass())) {
						builder.tableAlias("c");
					} else {
						fail("Unexpected class.");
					}
					return true;
				}
			}
		);
		runForNode(ed, rs, rs, meta);
	}

}
