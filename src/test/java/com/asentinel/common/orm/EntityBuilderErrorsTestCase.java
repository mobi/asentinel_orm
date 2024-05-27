package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ResultSetUtils;

public class EntityBuilderErrorsTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(EntityBuilderErrorsTestCase.class);

	@Test
	public void testWithEmptyResultSet() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);

		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		
		// first row
		expect(rs.next()).andReturn(false);
		rs.close();
		
		Node<EntityDescriptor> ed = 
			new SimpleNode<EntityDescriptor>(EntityDescriptor.forColumnPk(InvoiceParentEntity.class, new InvoiceParentEntity.InvoiceRowMapper(), "InvoiceId"))
				.addChild(new SimpleNode<EntityDescriptor>(EntityDescriptor.forColumnPk(BillParentEntity.class, new BillParentEntity.BillRowMapper(), "BillId")));
		
		EntityBuilder<InvoiceParentEntity> handler = new EntityBuilder<InvoiceParentEntity>(ed);
		
		replay(rs);
		ResultSetUtils.processResultSet(rs, handler);
		
		List<InvoiceParentEntity> invoices = handler.getEntityList();
		StringBuilder sb = new StringBuilder();
		for (InvoiceParentEntity invoice:invoices) {
			sb.append(invoice).append("\n");
			for (Bill b:invoice.bills) {
				sb.append("   ").append(b).append("\n");
			}
		}
		log.debug("testNormalOperation - Invoices:\n" + sb);
		verify(rs);

		assertEquals(0, invoices.size());
		
		// check getEntity()
		try {
			handler.getEntity();
			fail("Should throw exception.");
		} catch(EmptyResultDataAccessException e){
			assertEquals(0, e.getActualSize());
		}

	}

	@Test
	public void testWihInvalidTree() {
		Node<EntityDescriptor> ed = 
				new SimpleNode<EntityDescriptor>(EntityDescriptor.forColumnPk(InvoiceParentEntity.class, new InvoiceParentEntity.InvoiceRowMapper(), "InvoiceId"))
					.addChild(new SimpleNode<EntityDescriptor>(null));
			
		try {
			new EntityBuilder<InvoiceParentEntity>(ed);
			fail("Should not get to this line.");
		} catch (NullPointerException e) {
			
		}
	}

}
