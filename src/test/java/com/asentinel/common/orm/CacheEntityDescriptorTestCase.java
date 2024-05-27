package com.asentinel.common.orm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.ResultSetUtils;

public class CacheEntityDescriptorTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(CacheEntityDescriptorTestCase.class);
	
	private final ServiceType st1 = new ServiceType(1, "Wireless");
	private final ServiceType st2 = new ServiceType(2, "Local");
	
	private final List<ServiceType> serviceTypes = new ArrayList<ServiceType>();
	
	@Before
	public void setup() {
		serviceTypes.add(st1);
		serviceTypes.add(st2);
	}
	
	@Test
	public void testNormalOperation() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);

		expect(rs.getRow()).andReturn(0).anyTimes();
		expect(rs.getFetchSize()).andReturn(10).anyTimes();
		expect(rs.wasNull()).andReturn(false).anyTimes();
		
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("b_BillId")).andReturn(1);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_1");
		expect(rs.getInt("ServiceTypeId")).andReturn(1).times(2);
		

		expect(rs.next()).andReturn(true);
		expect(rs.getInt("b_BillId")).andReturn(2);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_2");
		expect(rs.getInt("ServiceTypeId")).andReturn(2).times(2);
		
		expect(rs.next()).andReturn(true);
		expect(rs.getInt("b_BillId")).andReturn(3);
		expect(rs.getObject("b_ItemNumber")).andReturn("bill_3");
		expect(rs.getInt("ServiceTypeId")).andReturn(1);
		

		expect(rs.next()).andReturn(false);
		rs.close();
		
		Node<EntityDescriptor> nodeBill = new SimpleNode<EntityDescriptor>(
				new SimpleEntityDescriptor.Builder(ServiceTypeBill.class).tableAlias("b").build()
		);
		Node<EntityDescriptor> nodeServiceType = new SimpleNode<EntityDescriptor>(
				CacheEntityDescriptor.forIntPk(ServiceType.class, "ServiceTypeId", serviceTypes)
		);
		nodeBill.addChild(nodeServiceType);
		
		EntityBuilder<ServiceTypeBill> builder = new EntityBuilder<ServiceTypeBill>(nodeBill);
		
		replay(rs);
		
		ResultSetUtils.processResultSet(rs, builder);
		
		verify(rs);
		
		List<ServiceTypeBill> bills = builder.getEntityList();
		for (ServiceTypeBill bill: bills) {
			log.debug("testNormalOperation - bill: " + bill);
		}
		
		assertEquals(3, bills.size());
		assertEquals(1, bills.get(0).billId);
		assertEquals("bill_1", bills.get(0).itemNumber);
		assertSame(st1, bills.get(0).serviceType);

		assertEquals(2, bills.get(1).billId);
		assertEquals("bill_2", bills.get(1).itemNumber);
		assertSame(st2, bills.get(1).serviceType);

		assertEquals(3, bills.get(2).billId);
		assertEquals("bill_3", bills.get(2).itemNumber);
		assertSame(st1, bills.get(2).serviceType);

	}

}
