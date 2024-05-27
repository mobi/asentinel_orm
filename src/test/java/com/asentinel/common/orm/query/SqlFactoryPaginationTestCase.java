package com.asentinel.common.orm.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.BillParentEntity;
import com.asentinel.common.orm.Charge;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.InvoiceParentEntity;
import com.asentinel.common.orm.ManyToManyEntityDescriptor;
import com.asentinel.common.orm.ParameterizedQuery;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.QueryUtils;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.query.DefaultSqlFactory.TreeBreakdown;

public class SqlFactoryPaginationTestCase {
	
	private final static Logger log = LoggerFactory.getLogger(SqlFactoryPaginationTestCase.class);
	
	JdbcFlavor jdbcF = new PostgresJdbcFlavor();
	SqlFactory sqlFactory = new DefaultSqlFactory(jdbcF);
	
	@Before
	public void setup() {
		QueryUtils.resetDescriptorId();
	}
	
	@Test
	public void testBreakTreeOnlyAssociations() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(Charge.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(BillParentEntity.class));
		Node<EntityDescriptor> node3 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		node2.addChild(node3);
		node1.addChild(node2);
		
		TreeBreakdown tbd = DefaultSqlFactory.breakTree(node1);
		assertFalse(tbd.hasSubTrees());
		assertEquals(node1.toStringAsTree(), tbd.getMainTree().toStringAsTree());
	}

	@Test
	public void testBreakTreeWithCollections1() {
		Node<EntityDescriptor> node3 = new SimpleNode<EntityDescriptor>(
				new SimpleEntityDescriptor.Builder(Charge.class).parentRelationType(RelationType.MANY_TO_ONE).build()				
		);
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
				new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		node1.addChild(node2);
		node2.addChild(node3);
		
		TreeBreakdown tbd = DefaultSqlFactory.breakTree(node1);
		assertEquals(1, tbd.getSubTrees().size());
		assertEquals(0, tbd.getMainTree().getChildren().size());
	}

	@Test
	public void testBreakTreeWithCollections2() {
		Node<EntityDescriptor> node3 = new SimpleNode<EntityDescriptor>(
				new ManyToManyEntityDescriptor.Builder(Charge.class, "t0").build()				
		);
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
				new ManyToManyEntityDescriptor.Builder(BillParentEntity.class, "t1").build()
		);
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		node1.addChild(node2);
		node2.addChild(node3);
		
		TreeBreakdown tbd = DefaultSqlFactory.breakTree(node1);
		assertEquals(1, tbd.getSubTrees().size());
		assertEquals(0, tbd.getMainTree().getChildren().size());
	}

	
	@Test
	public void testPaginationOnlyAssociations() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(BillParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		node1.addChild(node2);
		log.debug("testPaginationOnlyAssociations - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.secondaryWhereClause("b=2")
			.mainOrderByClause("InvoiceId_Order")
			.build();
		
		// main query
		String q = sqlFactory.buildPaginatedQuery(pagination);
		log.debug("testPaginationOnlyAssociations - q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertTrue(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		
		// ensure we add the primary key of the BillParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoiceid_order,a0.billid"));
		
		// count query, we check we only select the primary key of the root entity
		String qc = sqlFactory.buildCountQuery(pagination);
		log.debug("testPaginationOnlyAssociations - qc: " + qc);
		assertTrue(qc.toLowerCase().replace(" ", "").indexOf("selecta0.billidfrom") >= 0);
	}
	

	@Test
	public void testPaginationWithCollections() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithCollections - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceNumber_order")
			.secondaryWhereClause("b=2")
			.mainAdditionalColumns("InvoiceId_Order")
			.build();
		
		// main query
		String q = sqlFactory.buildPaginatedQuery(pagination);
		log.debug("testPaginationWithCollections - paging q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		
		// ensure we add the primary key of the InvoiceParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoicenumber_order,a0.invoiceid"));
		
		
		// count query, we check we only select the primary key of the root entity and that
		// we do not join the details - BILL table
		q = sqlFactory.buildCountQuery(pagination);
		log.debug("testPaginationWithCollections - count q: " + q);
		assertFalse(q.toUpperCase().indexOf("BILL") >= 0);
		assertTrue(q.toLowerCase().replace(" ", "").indexOf("selecta0.invoiceidfrom") >= 0);
	}
	
	@Test
	public void testPaginationWithCollections_forceManyAsOne() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class)
				.parentRelationType(RelationType.MANY_TO_ONE)
				.forceManyAsOneInPaginatedQueries(true)
				.build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithCollections_forceManyAsOne - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceNumber_order")
			.secondaryWhereClause("b=2")
			.mainAdditionalColumns("InvoiceId_Order")
			.build();
		
		// main query
		String q = sqlFactory.buildPaginatedQuery(pagination);
		log.debug("testPaginationWithCollections_forceManyAsOne - paging q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertTrue(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		
		// ensure we add the primary key of the InvoiceParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoicenumber_order,a0.invoiceid"));
		
		
		// count query, we check we only select the primary key of the root entity and that
		// we DO join the details - BILL table
		q = sqlFactory.buildCountQuery(pagination);
		log.debug("testPaginationWithCollections_forceManyAsOne - count q: " + q);
		assertTrue(q.toUpperCase().indexOf("BILL") >= 0);
		assertTrue(q.toLowerCase().replace(" ", "").indexOf("selecta0.invoiceidfrom") >= 0);
	}

	@Test
	public void testPaginationWithCollectionsGroupBy() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithCollectionsGroupBy - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.secondaryWhereClause("b=2")
			.mainAdditionalColumns("InvoiceId_Order")
			.mainGroupByAdditionalColumns("InvoiceId_Order")
			.useGroupByOnMainQuery(true)
			.build();
		
		// main query
		String q = sqlFactory.buildPaginatedQuery(pagination);
		log.debug("testPaginationWithCollections - paging q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		assertTrue(q.indexOf("group by") >= 0);
		
		// ensure we add the primary key of the InvoiceParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbya0.invoiceid"));
		
		// count query, we check we only select the primary key of the root entity and that
		// we do not join the details - BILL table
		q = sqlFactory.buildCountQuery(pagination);
		log.debug("testPaginationWithCollectionsGroupBy - count q: " + q);
		assertTrue(q.toUpperCase().indexOf("BILL") >= 0);
		assertTrue(q.toLowerCase().replace(" ", "").indexOf("selecta0.invoiceidfrom") >= 0);
	}

	
	@Test
	public void testPaginationWithNamedParamsAssociations() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(BillParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		node1.addChild(node2);
		log.debug("testPaginationWithNamedParamsAssociations - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceId_Order")
			.build();
		String q = sqlFactory.buildPaginatedQuery(pagination, true);
		log.debug("testPaginationWithNamedParamsAssociations - q: " + q);
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf(jdbcF.getSqlTemplates().getPaginationNamedParam1()) >= 0);
		assertTrue(q.indexOf(jdbcF.getSqlTemplates().getPaginationNamedParam2()) >= 0);
	}

	
	@Test
	public void testPaginationWithNamedParamsCollections() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithNamedParamsCollections - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainAdditionalColumns("InvoiceId_Order")
			.mainGroupByAdditionalColumns("InvoiceId_Order")
			.useGroupByOnMainQuery(true)
			.build();
		String q = sqlFactory.buildPaginatedQuery(pagination, true);
		log.debug("testPaginationWithNamedParamsCollections - q: " + q);
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf(jdbcF.getSqlTemplates().getPaginationNamedParam1()) >= 0);
		assertTrue(q.indexOf(jdbcF.getSqlTemplates().getPaginationNamedParam2()) >= 0);
	}
	
	
	// ------------- join conditions override -----------------

	@Test
	public void testPaginationOnlyAssociations_joinConditionsOverride() {
		String overrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS + "." + "TestColumn1 = ?"
				+ " and " + QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS + "." + "TestColumn2 = ?";
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(BillParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
				new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
					.joinConditionsOverride(overrideString)
					.joinConditionsOverrideParam(111)
					.build()
		);
		node1.addChild(node2);
		log.debug("testPaginationOnlyAssociations_joinConditionsOverride - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.secondaryWhereClause("b=2")
			.mainOrderByClause("InvoiceId_Order")
			.build();
		ParameterizedQuery pq = sqlFactory.buildPaginatedParameterizedQuery(pagination);
		String q = pq.getSql();
		log.debug("testPaginationOnlyAssociations_joinConditionsOverride - q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertTrue(m.find());
		
		assertTrue(q.contains("a=1"));
		assertTrue(q.contains("b=2"));
		assertTrue(q.contains("InvoiceId_Order"));
		assertTrue(q.replaceAll(" ", "").contains("a0.InvoiceId=a1.InvoiceIdanda0.TestColumn1=?anda1.TestColumn2=?"));
		assertEquals(Arrays.asList(111), pq.getMainParameters());
		assertTrue(pq.getSecondaryParameters().isEmpty());
		
		// ensure we add the primary key of the BillParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoiceid_order,a0.billid"));
		
		ParameterizedQuery pq2 = sqlFactory.buildCountParameterizedQuery(pagination);
		String q2 = pq2.getSql();
		log.debug("testPaginationOnlyAssociations_joinConditionsOverride - q2: " + q2);

		assertTrue(q2.replaceAll(" ", "").contains("a0.InvoiceId=a1.InvoiceIdanda0.TestColumn1=?anda1.TestColumn2=?"));
		assertEquals(Arrays.asList(111), pq2.getMainParameters());
		assertTrue(pq2.getSecondaryParameters().isEmpty());
		assertTrue(q2.toLowerCase().replace(" ", "").indexOf("selecta0.billidfrom") >= 0);
	}
	
	@Test
	public void testPaginationWithCollections_joinConditionsOverride() {
		String overrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS + "." + "TestColumn1 = ?"
				+ " and " + QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS + "." + "TestColumn2 = ?";
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class)
				.parentRelationType(RelationType.MANY_TO_ONE)
				.joinConditionsOverride(overrideString)
				.joinConditionsOverrideParam(222)
				.build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithCollections_joinConditionsOverride - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceNumber_order")
			.secondaryWhereClause("b=2")
			.mainAdditionalColumns("InvoiceId_Order")
			.build();
		ParameterizedQuery pq = sqlFactory.buildPaginatedParameterizedQuery(pagination);
		String q = pq.getSql();
		log.debug("testPaginationWithCollections_joinConditionsOverride - paging q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		assertTrue(q.replaceAll(" ", "").contains("a0_InvoiceId=a1.InvoiceIdanda0.TestColumn1=?anda1.TestColumn2=?"));
		assertTrue(pq.getMainParameters().isEmpty());
		assertEquals(Arrays.asList(222), pq.getSecondaryParameters());
		

		
		// ensure we add the primary key of the InvoiceParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoicenumber_order,a0.invoiceid"));
		
		
		ParameterizedQuery pq2 = sqlFactory.buildCountParameterizedQuery(pagination);
		String q2 = pq2.getSql();
		log.debug("testPaginationWithCollections_joinConditionsOverride - count q2: " + q2);
		assertFalse(q2.toUpperCase().indexOf("BILL") >= 0);
		assertTrue(q2.toLowerCase().replace(" ", "").indexOf("selecta0.invoiceidfrom") >= 0);
		assertTrue(pq2.getMainParameters().isEmpty());
		assertTrue(pq2.getSecondaryParameters().isEmpty());
	}
	

	@Test
	public void testPaginationWithCollections_groupByEnabled_joinConditionsOverride() {
		String overrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS + "." + "TestColumn1 = ?"
				+ " and " + QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS + "." + "TestColumn2 = ?";
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class)
				.parentRelationType(RelationType.MANY_TO_ONE)
				.joinConditionsOverride(overrideString)
				.joinConditionsOverrideParam(222)
				.build()
		);
		node1.addChild(node2);
		log.debug("testPaginationWithCollections_groupByEnabled_joinConditionsOverride - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceNumber_order")
			.secondaryWhereClause("b=2")
			.mainAdditionalColumns("InvoiceId_Order")
			.useGroupByOnMainQuery(true)
			.build();
		ParameterizedQuery pq = sqlFactory.buildPaginatedParameterizedQuery(pagination);
		String q = pq.getSql();
		log.debug("testPaginationWithCollections_groupByEnabled_joinConditionsOverride - paging q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("b=2") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		assertTrue(q.replaceAll(" ", "").contains("a0_InvoiceId=a1.InvoiceIdanda0.TestColumn1=?anda1.TestColumn2=?"));
		assertEquals(Arrays.asList(222), pq.getMainParameters());
		assertEquals(Arrays.asList(222), pq.getSecondaryParameters());
		

		
		// ensure we add the primary key of the InvoiceParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoicenumber_order,a0.invoiceid"));
		
		
		ParameterizedQuery pq2 = sqlFactory.buildCountParameterizedQuery(pagination);
		String q2 = pq2.getSql();
		log.debug("testPaginationWithCollections_groupByEnabled_joinConditionsOverride - count q2: " + q2);
		assertTrue(q2.toUpperCase().indexOf("BILL") >= 0);
		assertTrue(q2.toLowerCase().replace(" ", "").indexOf("selecta0.invoiceidfrom") >= 0);
		assertEquals(Arrays.asList(222), pq2.getMainParameters());
		assertTrue(pq2.getSecondaryParameters().isEmpty());
	}

}
