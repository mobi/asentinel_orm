package com.asentinel.common.orm;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.ResultSetUtils;

/**
 * Support class for {@link EntityBuilder} tests. It provides
 * the {@link #runForNode(Node, ResultSet, Object...)} method
 * that handles the results validation.
 */
public abstract class EntityBuilderTSupport {

	private final static Logger log = LoggerFactory.getLogger(EntityBuilderTSupport.class);
	
	protected final static BigDecimal INVOICE_ID = new BigDecimal("1");
	protected final static BigDecimal INVOICE_ID2 = new BigDecimal("2");
	
	protected void runForNode(Node<EntityDescriptor> ed, ResultSet rs, Object ... mocks) throws SQLException {
		EntityBuilder<? extends Invoice> handler = new EntityBuilder<InvoiceParentEntity>(ed);
		
		replay(mocks);
		ResultSetUtils.processResultSet(rs, handler);
		
		List<? extends Invoice> invoices = handler.getEntityList();
		Map<Object, ? extends Invoice> invoicesMap = handler.getEntityMap();
		StringBuilder sb = new StringBuilder();
		for (Invoice invoice:invoices) {
			sb.append(invoice).append("\n");
			for (Bill b:invoice.bills) {
				sb.append("   ").append(b).append("\n");
				for (Charge c:b.charges) {
					sb.append("      ").append(c).append("\n");
				}
			}
		}
		log.debug("runForNode - Invoices:\n" + sb);
		verify(mocks);

		// TODO: validates only children count, not member values
		assertEquals(2, handler.count());
		assertEquals(2, invoices.size());
		assertEquals(2, invoicesMap.size());
		
		// first invoice
		assertEquals(2, invoices.get(0).bills.size());
		assertEquals(2, invoices.get(0).bills.get(0).charges.size());
		assertEquals(0, invoices.get(0).bills.get(1).charges.size());
		
		// second invoice
		assertEquals(3, invoices.get(1).bills.size());
		assertEquals(0, invoices.get(1).bills.get(0).charges.size());
		assertEquals(0, invoices.get(1).bills.get(1).charges.size());
		assertEquals(0, invoices.get(1).bills.get(2).charges.size());
		
		// check getEntity()
		try {
			handler.getEntity();
			fail("Should throw exception.");
		} catch(IncorrectResultSizeDataAccessException e){
			assertEquals(2, e.getActualSize());
		}
	}

}
