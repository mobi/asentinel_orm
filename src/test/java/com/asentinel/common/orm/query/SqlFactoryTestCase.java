package com.asentinel.common.orm.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.Bill;
import com.asentinel.common.orm.BillParentEntity;
import com.asentinel.common.orm.Charge;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.Formula;
import com.asentinel.common.orm.Invoice;
import com.asentinel.common.orm.InvoiceParentEntity;
import com.asentinel.common.orm.JoinType;
import com.asentinel.common.orm.ManyToManyEntityDescriptor;
import com.asentinel.common.orm.ManyToManyQueryReady;
import com.asentinel.common.orm.ParameterizedQuery;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.QueryUtils;
import com.asentinel.common.orm.RelationType;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;

public class SqlFactoryTestCase {
	private final static Logger log = LoggerFactory.getLogger(SqlFactoryTestCase.class);
	
	SqlFactory sqlFactory = new DefaultSqlFactory(new PostgresJdbcFlavor());

	@Before
	public void setup() {
		QueryUtils.resetDescriptorId();
	}
	
	/**
	 * @return all the fields for the class parameter regardless of
	 * 			their access restrictions (includes private, protected etc).
	 */
	private static List<Field> getAllFields(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		final List<Field> fields = new ArrayList<Field>();
		ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
			
			@Override
			public void doWith(Field field) {
				fields.add(field);
			}
		});
		return fields;
	}
	
	/**
	 * @return all the fields and methods for the class parameter regardless of
	 * 			their access restrictions (includes private, protected etc).
	 */
	public static List<AnnotatedElement> getPotentiallyAnnotatedElements(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		List<AnnotatedElement> elements = new ArrayList<AnnotatedElement>();
		elements.addAll(getAllFields(clazz));
		elements.addAll(Arrays.asList(ReflectionUtils.getAllDeclaredMethods(clazz)));
		return elements;
	}

	/**
	 * @param element the annotated method or field.
	 * @param ignorePkColumn
	 * @return the name of the column associated with the annotated element
	 * 			or null if the element is not annotated with <code>PkColumn</code>
	 * 			or <code>Column</code>. If the <code>ignorePkColumn</code> parameter
	 * 			is true only <code>Column</code> annotated elements will be considered.
	 */
	private static String getColumnName(AnnotatedElement element, boolean ignorePkColumn) {
		Column colAnn = AnnotationUtils.getAnnotation(element, Column.class);
		if (colAnn == null) {
			if (!ignorePkColumn) {
				PkColumn pkColAnn = AnnotationUtils.getAnnotation(element, PkColumn.class);
				if (pkColAnn == null) {
					return null;
				} else {
					if (element instanceof Method) {
						if (((Method) element).getParameterTypes().length != 1) {
							throw new IllegalArgumentException("The method " + element + " should have exactly one parameter.");
						}
					}
					return pkColAnn.value();
				}
			} else {
				return null;
			}
		} else {
			if (element instanceof Method) {
				if (((Method) element).getParameterTypes().length != 1) {
					throw new IllegalArgumentException("The method " + element + " should have exactly one parameter.");
				}
			}
			return colAnn.value();
		}
	}
	
	
	/**
	 * Checks that all the columns are selected.
	 * @param sql
	 * @param node
	 */
	private void testSql(String sql, Node<SimpleEntityDescriptor> node) {
		SimpleEntityDescriptor descriptor = node.getValue();
		List<AnnotatedElement> elements = getPotentiallyAnnotatedElements(descriptor.getEntityClass());
		for (AnnotatedElement el: elements) {
			String colName = getColumnName(el, false);
			if (colName != null) {
				assertTrue("Can not find '" + colName + "' in the sql string.", 
						sql.indexOf(descriptor.getTableAlias() + descriptor.getColumnAliasSeparator() + colName) > 0);
			}
		}
	}
	
	@Test
	public void testOneToOne() {
		SimpleEntityDescriptor descInvoiceRoot = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("ir").build();
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
			.tableAlias("i").parentRelationType(RelationType.ONE_TO_ONE)
			.build();
		
		Node<SimpleEntityDescriptor> nodeInvoiceRoot = new SimpleNode<SimpleEntityDescriptor>(descInvoiceRoot);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeInvoiceRoot.addChild(nodeInvoice);
		
		// the join is made based on both primary keys in this case
		String sql = sqlFactory.buildQuery(nodeInvoiceRoot);
		log.debug("testOneToOne - sql: " + sql);
		
		testSql(sql, nodeInvoiceRoot);
		testSql(sql, nodeInvoice);
	}
	
	@Test
	public void testOneToOne_From() {
		SimpleEntityDescriptor descInvoiceRoot = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("ir").build();
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
			.tableAlias("i").parentRelationType(RelationType.ONE_TO_ONE)
			.build();
		
		Node<SimpleEntityDescriptor> nodeInvoiceRoot = new SimpleNode<SimpleEntityDescriptor>(descInvoiceRoot);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeInvoiceRoot.addChild(nodeInvoice);
		
		// the join is made based on both primary keys in this case
		String sql = sqlFactory.buildFromQuery(nodeInvoiceRoot);
		log.debug("testOneToOne_From - sql: " + sql);
		assertEquals("from bill ir left join invoice i on ir.billid = i.invoiceid".replace(" ", ""), 
				sql.replace(" ", "").toLowerCase());
	}

	@Test
	public void testOneToMany() {
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).tableAlias("b").build();
		SimpleEntityDescriptor descCharge = new SimpleEntityDescriptor.Builder(Charge.class).parentRelationType(RelationType.MANY_TO_ONE).tableAlias("c").build();
		
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		Node<SimpleEntityDescriptor> nodeBills = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeCharges = new SimpleNode<SimpleEntityDescriptor>(descCharge);
		nodeInvoice.addChild(nodeBills);
		nodeBills.addChild(nodeCharges);
		
		String sql = sqlFactory.buildQuery(nodeInvoice);
		log.debug("testOneToMany - sql: " + sql);
		
		testSql(sql, nodeInvoice);
		testSql(sql, nodeBills);
		testSql(sql, nodeCharges);
	}
	
	@Test
	public void testOneToMany_From() {
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).tableAlias("b").build();
		SimpleEntityDescriptor descCharge = new SimpleEntityDescriptor.Builder(Charge.class).parentRelationType(RelationType.MANY_TO_ONE).tableAlias("c").build();
		
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		Node<SimpleEntityDescriptor> nodeBills = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeCharges = new SimpleNode<SimpleEntityDescriptor>(descCharge);
		nodeInvoice.addChild(nodeBills);
		nodeBills.addChild(nodeCharges);
		
		String sql = sqlFactory.buildFromQuery(nodeInvoice);
		log.debug("testOneToMany_From - sql: " + sql);
		assertEquals("from invoice i left join bill b on i.invoiceid = b.invoiceid left join charges c on b.billid = c.billid".replace(" ", ""), 
				sql.replace(" ", "").toLowerCase());

	}
	

	@Test
	public void testManyToOne() {
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeBill.addChild(nodeInvoice);
		
		String sql = sqlFactory.buildQuery(nodeBill);
		log.debug("testManyToOne - sql: " + sql);
		
		testSql(sql, nodeBill);
		testSql(sql, nodeInvoice);
	}
	
	
	@Test
	public void testManyToOne_From() {
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeBill.addChild(nodeInvoice);
		
		String sql = sqlFactory.buildFromQuery(nodeBill);
		log.debug("testManyToOne_From - sql: " + sql);
		assertEquals("from bill b left join invoice i on b.invoiceid = i.invoiceid".replace(" ", ""), 
				sql.replace(" ", "").toLowerCase());
	}
	
	
	@Test
	public void testNodeNotRoot() {
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(null);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		nodeBill.addChild(nodeInvoice);
		
		try {
			sqlFactory.buildQuery(nodeInvoice);
			fail("Should not get to this line.");
		} catch (Exception e) {
			log.debug("testNodeNotRoot - Expected exception: " + e.getMessage());
		}

		try {
			sqlFactory.buildQuery(nodeBill);
			fail("Should not get to this line.");
		} catch (Exception e) {
			log.debug("testNodeNotRoot - Expected exception: " + e.getMessage());
		}
	}
	
	
	@Test
	public void testNodeNotRoot_From() {
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(null);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		nodeBill.addChild(nodeInvoice);
		
		try {
			sqlFactory.buildFromQuery(nodeInvoice);
			fail("Should not get to this line.");
		} catch (Exception e) {
			log.debug("testNodeNotRoot_From - Expected exception: " + e.getMessage());
		}

		try {
			sqlFactory.buildFromQuery(nodeBill);
			fail("Should not get to this line.");
		} catch (Exception e) {
			log.debug("testNodeNotRoot_From - Expected exception: " + e.getMessage());
		}
	}
	
	
	@Test
	public void testManyToMany() {
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		ManyToManyEntityDescriptor descInvoice = new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "link_table")
			.tableAlias("i")
			.manyToManyTableAlias("l")
			.build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeBill.addChild(nodeInvoice);
		
		String sql = sqlFactory.buildQuery(nodeBill);
		log.debug("testManyToMany - sql: " + sql);
		
		testSql(sql, nodeBill);
		testSql(sql, nodeInvoice);
		
		assertTrue(sql.indexOf("link_table") > 0);
		assertTrue(sql.indexOf("l.") > 0);
	}
	
	@Test
	public void testManyToMany_From() {
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		ManyToManyEntityDescriptor descInvoice = new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "link_table")
			.tableAlias("i")
			.manyToManyTableAlias("l")
			.build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeBill.addChild(nodeInvoice);
		
		String sql = sqlFactory.buildFromQuery(nodeBill);
		log.debug("testManyToMany_From - sql: " + sql);
		assertEquals("from bill b left join link_table l on b.billid = l.billid left join invoice i on l.invoiceid = i.invoiceid".replace(" ", ""), 
				sql.replace(" ", "").toLowerCase());

		
	}
	
	
	@Test
	public void testAdditionalColumns() {
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		
		String sql = sqlFactory.buildQuery(nodeBill, "AAA", "BBB");
		log.debug("testAdditionalColumns - sql: " + sql);
		
		testSql(sql, nodeBill);
		assertTrue(sql.indexOf("AAA") >= 0);
		assertTrue(sql.indexOf("BBB") >= 0);
	}
	
	
	//  test buildQuery(QueryCriteria)
	
	@Test
	public void testQueryCriteriaOnlyAssociations() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(BillParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
				new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).parentInnerJoin().build()
		);
		node1.addChild(node2);
		log.debug("testQueryCriteriaOnlyAssociations - Tree:\n" + node1.toStringAsTree());
		QueryCriteria criteria  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceId_Order")
			.build();
		String q = sqlFactory.buildQuery(criteria);
		log.debug("testQueryCriteriaOnlyAssociations - q: " + q);
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		assertTrue(q.indexOf("inner join") >= 0);
		
		// ensure we add the primary key of the BillParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoiceid_order,a0.billid"));
		
	}

	@Test
	public void testQueryCriteriaCollections() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		node1.addChild(node2);
		log.debug("testQueryCriteriaCollections - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainOrderByClause("InvoiceId_Order")
			.mainAdditionalColumns("InvoiceId_Order")
			.build();
		String q = sqlFactory.buildQuery(pagination);
		log.debug("testQueryCriteriaCollections - q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		// ensure we add the primary key of the BillParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbyinvoiceid_order,a0.invoiceid"));
		
	}

	@Test
	public void testQueryCriteriaCollectionsGroupBy() {
		Node<EntityDescriptor> node1 = new SimpleNode<EntityDescriptor>(new SimpleEntityDescriptor(InvoiceParentEntity.class));
		Node<EntityDescriptor> node2 = new SimpleNode<EntityDescriptor>(
			new SimpleEntityDescriptor.Builder(BillParentEntity.class).parentRelationType(RelationType.MANY_TO_ONE).build()
		);
		node1.addChild(node2);
		log.debug("testQueryCriteriaCollectionsGroupBy - Tree:\n" + node1.toStringAsTree());
		QueryCriteria pagination  = new QueryCriteria.Builder(node1)
			.mainWhereClause("a=1")
			.mainAdditionalColumns("InvoiceId_Order")
			.mainGroupByAdditionalColumns("InvoiceId_Order")
			.useGroupByOnMainQuery(true)
			.build();
		String q = sqlFactory.buildQuery(pagination);
		log.debug("testQueryCriteriaCollectionsGroupBy - q: " + q);
		
		Pattern p = Pattern.compile("main\\s+where");
		Matcher m  = p.matcher(q);
		assertFalse(m.find());
		
		assertTrue(q.indexOf("a=1") >= 0);
		assertTrue(q.indexOf("InvoiceId_Order") >= 0);
		
		// ensure we add the primary key of the BillParentEntity class to the main order by so that the ordering is
		// always deterministic
		assertTrue(q.toLowerCase().replace(" ", "").contains("orderbya0.invoiceid"));
		
	}
	
	@Test
	public void testShouldApplyInnerJoinForOneToMany0() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		assertTrue(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n1));
	}

	
	@Test
	public void testShouldApplyInnerJoinForOneToMany1() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		n1.addChild(n2);
		assertTrue(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n2));
	}
	
	@Test
	public void testShouldApplyInnerJoinForOneToMany11() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentJoinType(JoinType.LEFT).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		n1.addChild(n2);
		assertFalse(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n2));
	}

	@Test
	public void testShouldApplyInnerJoinForOneToMany2() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentJoinType(JoinType.INNER).build();
		SimpleEntityDescriptor ed3 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		Node<EntityDescriptor> n3 = new SimpleNode<EntityDescriptor>(ed3);
		n1.addChild(n2);
		n2.addChild(n3);
		assertTrue(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n3));
	}

	@Test
	public void testShouldApplyInnerJoinForOneToMany3() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentJoinType(JoinType.LEFT).build();
		SimpleEntityDescriptor ed3 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		Node<EntityDescriptor> n3 = new SimpleNode<EntityDescriptor>(ed3);
		n1.addChild(n2);
		n2.addChild(n3);
		assertFalse(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n3));
	}

	@Test
	public void testShouldApplyInnerJoinForOneToMany4() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentRelationType(RelationType.MANY_TO_ONE).build();
		SimpleEntityDescriptor ed3 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		Node<EntityDescriptor> n3 = new SimpleNode<EntityDescriptor>(ed3);
		n1.addChild(n2);
		n2.addChild(n3);
		assertFalse(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n3));
	}

	@Test
	public void testShouldApplyInnerJoinForOneToMany5() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		SimpleEntityDescriptor ed2 = new SimpleEntityDescriptor.Builder(Bill.class).parentJoinType(JoinType.LEFT).build();
		SimpleEntityDescriptor ed3 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		SimpleEntityDescriptor ed4 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		Node<EntityDescriptor> n3 = new SimpleNode<EntityDescriptor>(ed3);
		Node<EntityDescriptor> n4 = new SimpleNode<EntityDescriptor>(ed4);
		n1.addChild(n2);
		n2.addChild(n3);
		n3.addChild(n4);
		assertFalse(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n4));
	}

	@Test
	public void testShouldApplyInnerJoinForOneToMany6() {
		// the Entity classes are just place holders here, they are not involved in the test logic
		SimpleEntityDescriptor ed1 = new SimpleEntityDescriptor(Charge.class);
		ManyToManyEntityDescriptor ed2 = new ManyToManyEntityDescriptor.Builder(Bill.class, "mtm").build();
		SimpleEntityDescriptor ed3 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		SimpleEntityDescriptor ed4 = new SimpleEntityDescriptor.Builder(Invoice.class).parentJoinType(JoinType.INNER).build();
		Node<EntityDescriptor> n1 = new SimpleNode<EntityDescriptor>(ed1);
		Node<EntityDescriptor> n2 = new SimpleNode<EntityDescriptor>(ed2);
		Node<EntityDescriptor> n3 = new SimpleNode<EntityDescriptor>(ed3);
		Node<EntityDescriptor> n4 = new SimpleNode<EntityDescriptor>(ed4);
		n1.addChild(n2);
		n2.addChild(n3);
		n3.addChild(n4);
		assertFalse(DefaultSqlFactory.shouldApplyInnerJoinForAssociation(n4));
	}
	
	@Test
	public void testFormula() {
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class)
				.tableAlias("i")
				.formulas(Map.of("INVOICEnumber", new Formula("formula(%s)")))
				.build();
		
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		String sql = sqlFactory.buildQuery(nodeInvoice);
		log.debug("testFormula - sql: " + sql);
		
		assertTrue(sql.contains("formula(i.InvoiceNumber) i_InvoiceNumber"));
	}	
	
	
	// -------------- join conditions override test cases ---------------------------

	@Test
	public void testOneToMany_JoinConditionsOverride() {
		String overrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS + "." + "TestColumn1 = ?"
				+ " and " + QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS + "." + "TestColumn2 = ?";  
		SimpleEntityDescriptor descInvoice = new SimpleEntityDescriptor.Builder(InvoiceParentEntity.class).tableAlias("i").build();
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class)
			.parentRelationType(RelationType.MANY_TO_ONE)
			.tableAlias("b")
			.joinConditionsOverride(overrideString)
			.joinConditionsOverrideParam(10)
			.build();
		
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		Node<SimpleEntityDescriptor> nodeBills = new SimpleNode<SimpleEntityDescriptor>(descBill);
		nodeInvoice.addChild(nodeBills);
		
		ParameterizedQuery sq = sqlFactory.buildParameterizedQuery(nodeInvoice);
		String sql = sq.getSql();
		log.debug("testOneToMany_JoinConditionsOverride - sql: " + sql);
		
		testSql(sql, nodeInvoice);
		testSql(sql, nodeBills);
		assertTrue(sql.replace(" ", "").endsWith("i.InvoiceId=b.InvoiceIdandi.TestColumn1=?andb.TestColumn2=?"));
		assertEquals(1, sq.getMainParameters().size());
		assertEquals(10, sq.getMainParameters().get(0));
		assertEquals(0, sq.getSecondaryParameters().size());
	}

	
	@Test
	public void testManyToMany_JoinConditionsOverride() {
		String leftOverrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + QueryReady.PLACEHOLDER_PARENT_TABLE_ALIAS + "." + "TestColumnLeft = ?"
				+ " and " + ManyToManyQueryReady.PLACEHOLDER_MTM_TABLE_ALIAS + "." + "TestColumnMiddle = ?";
		String rightOverrideString = QueryReady.PLACEHOLDER_DEFAULT_JOIN_CONDITION 
				+ " and " + ManyToManyQueryReady.PLACEHOLDER_MTM_TABLE_ALIAS + "." + "TestColumnMiddle = ?"				
				+ " and " + QueryReady.PLACEHOLDER_CHILD_TABLE_ALIAS + "." + "TestColumnRight = ?";
		SimpleEntityDescriptor descBill = new SimpleEntityDescriptor.Builder(BillParentEntity.class).tableAlias("b").build();
		ManyToManyEntityDescriptor descInvoice = new ManyToManyEntityDescriptor.Builder(InvoiceParentEntity.class, "link_table")
			.tableAlias("i")
			.manyToManyTableAlias("lnk")
			.manyToManyLeftJoinConditionsOverride(leftOverrideString)
			.manyToManyLeftJoinConditionsOverrideParam(10)
			.manyToManyRightJoinConditionsOverride(rightOverrideString)
			.manyToManyRightJoinConditionsOverrideParam(20)
			.build();
		
		Node<SimpleEntityDescriptor> nodeBill = new SimpleNode<SimpleEntityDescriptor>(descBill);
		Node<SimpleEntityDescriptor> nodeInvoice = new SimpleNode<SimpleEntityDescriptor>(descInvoice);
		
		nodeBill.addChild(nodeInvoice);
		
		ParameterizedQuery sq = sqlFactory.buildParameterizedQuery(nodeBill);
		String sql = sq.getSql();
		log.debug("testManyToMany_JoinConditionsOverride - sql: " + sql);
		
		testSql(sql, nodeBill);
		testSql(sql, nodeInvoice);
		
		assertTrue(sql.indexOf("link_table") > 0);
		assertTrue(sql.indexOf("lnk.") > 0);
		
		assertTrue(sql.replace(" ", "").contains("b.BillId=lnk.BillIdandb.TestColumnLeft=?andlnk.TestColumnMiddle=?"));
		assertTrue(sql.replace(" ", "").endsWith("lnk.InvoiceId=i.InvoiceIdandlnk.TestColumnMiddle=?andi.TestColumnRight=?"));
	}
	
	// TODO: add join conditions override tests for the method SqlFactory#buildParameterizedQuery(QueryCriteria)
	// This is hardly used so I did not add the tests yet
	
}
