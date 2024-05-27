package com.asentinel.common.orm.jql;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.Capture;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.data.Bill;
import com.asentinel.common.orm.jql.data.Charge;
import com.asentinel.common.orm.jql.data.ExtInvoice;
import com.asentinel.common.orm.jql.data.OverrideBill;
import com.asentinel.common.orm.jql.data.OverrideInvoice;
import com.asentinel.common.orm.query.DefaultSqlFactory;
import com.asentinel.common.orm.query.SqlFactory;

public class SqlBuilderPagedTestCase {
	
	final static Logger log = LoggerFactory.getLogger(SqlBuilderTestCase.class);
	
	EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	SqlFactory sf = new DefaultSqlFactory(new PostgresJdbcFlavor());
	SqlQuery ex = createMock(SqlQuery.class);
	SqlBuilderFactory sbf = new DefaultSqlBuilderFactory(edtr, sf, ex);
	
	final static String ROOT = "t0.";
	
	private void validate(PagedCompiledSql cSql, QueryCriteria criteria, Object[] expSqlParams, Object[] expSqlCountParams) {
		String expSql = sf.buildPaginatedQuery(criteria);
		String expCountSql = sf.buildCountQuery(criteria);
		
		log.debug(cSql.getSqlString());
		log.debug(expSql);
		
		log.debug(cSql.getSqlCountString());
		log.debug(expCountSql);
		
		assertEquals(expSql, cSql.getSqlString());
		assertEquals(expCountSql, cSql.getSqlCountString());
		
		Object[] sqlParams = cSql.getParameters();
		Object[] countSqlParams = cSql.getCountParameters();
		assertArrayEquals(expSqlParams, sqlParams);
		assertArrayEquals(expSqlCountParams, countSqlParams);
	}
	
	@Test
	public void testAssociations() {
		
		SqlBuilder<Charge> builder = sbf.newSqlBuilder(Charge.class);
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere().id().eq(10)
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Charge.class);

		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainWhereClause(ROOT + Charge.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.mainOrderByClause(ROOT + Charge.C_ID + SqlBuilder.DESC)
			.build();
		validate(cSql, criteria, new Object[] {10, 0l, 1l}, new Object[] {10});
	}
	
	@Test
	public void testCollections_Simple() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere().id().eq(10)
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.build();
		
		validate(cSql, criteria, new Object[] {10, 0l, 1l}, new Object[] {10});		
	}
	
	@Test
	public void testCollections_AdditionalColumns() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere("test").id().eq(10)
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainAdditionalColumns("test")
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.build();
		
		validate(cSql, criteria, new Object[] {10, 0l, 1l}, new Object[] {10});		
	}
	

	@Test
	public void testCollections_GroupBy() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere().id().eq(10)
			.pagedEnableGroupBy()
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.useGroupByOnMainQuery(true)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.build();
		
		validate(cSql, criteria, new Object[] {10, 0l, 1l}, new Object[] {10});	
	}
	
	@Test
	public void testCollections_GroupBy_AdditionalColumns() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere("test").id().eq(10)
			.pagedEnableGroupBy("test")
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainAdditionalColumns("test")
			.mainGroupByAdditionalColumns("test")
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.useGroupByOnMainQuery(true)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.build();
		
		validate(cSql, criteria, new Object[] {10, 0l, 1l}, new Object[] {10});	
	}
	
	@Test
	public void testCollections_GroupBy_Having() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere().id().eq(10)
			.pagedEnableGroupBy()
			.pagedHaving().alias(ExtInvoice.class, Bill.class).id().eq(11)
			.pagedOrderBy().id().desc()
			.pagedCompile();
		
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.useGroupByOnMainQuery(true)
			.mainHavingClause(Bill.TABLE_ALIAS + "." + Bill.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.build();
		
		validate(cSql, criteria, new Object[] {10, 11, 0l, 1l}, new Object[] {10, 11});
	}
	
	@Test
	public void testCollections_SecondaryWhere() {
		SqlBuilder<ExtInvoice> builder = sbf.newSqlBuilder(ExtInvoice.class);
		
		
		PagedCompiledSql cSql = builder.pagedSelect(0, 1)
			.pagedWhere("test").id().eq(10)
			.pagedOrderBy().id().desc()
			.pagedSecondaryWhere().sql("? not null", 12)
			.pagedCompile();
		
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(ExtInvoice.class);
		
		QueryCriteria criteria = new QueryCriteria.Builder(root)
			.mainAdditionalColumns("test")
			.mainWhereClause(ROOT + ExtInvoice.C_ID + SqlBuilder.EQ + CompiledSql.QUESTION_MARK)
			.mainOrderByClause(ROOT + ExtInvoice.C_ID + SqlBuilder.DESC)
			.secondaryWhereClause("? not null")
			.build();
		
		validate(cSql, criteria, new Object[] {10, 0l, 1l, 12}, new Object[] {10});		
	}
	
	
	@Test
	public void testExecForIndex_ResultFound() {
		Capture<String> sql = Capture.newInstance();
		expect(ex.queryForLong(capture(sql), anyInt(), anyInt()))
			.andReturn(10l);
		
		SqlBuilder<Charge> builder = sbf.newSqlBuilder(Charge.class);
		
		replay(ex);
		builder.pagedSelect(0, 20);
		String innerSql = builder.pagedCompile().getSqlString();
		long i = builder.execForIndex();
		verify(ex);
		assertEquals(10l, i);
		assertTrue(sql.getValue().startsWith("select " + SqlTemplates.ROW_INDEX_COL + " from ("));
		assertTrue(sql.getValue().indexOf(innerSql) > 0);
		assertTrue(sql.getValue().endsWith("offset 0 limit 1"));
	}
	
	@Test
	public void testExecForIndex_ResultNotFound() {
		expect(ex.queryForLong(anyObject(String.class), anyInt(), anyInt()))
			.andThrow(new EmptyResultDataAccessException(1));
		
		SqlBuilder<Charge> builder = sbf.newSqlBuilder(Charge.class);
		
		replay(ex);
		long i = builder.pagedSelect(0, 20).execForIndex();
		verify(ex);
		assertEquals(-1, i);
	}
	

	// --------- tests for join conditions override ---------

	@Test
	public void testJoinConditionsOverride() {
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(OverrideInvoice.class);
		QueryCriteria qc = new QueryCriteria.Builder(root)
			.mainWhereClause(ROOT + "id = ?")
			.secondaryWhereClause("? = ?")
			.build();
		String expectedPageSql = sf.buildPaginatedQuery(qc);
		String expectedCountSql = sf.buildCountQuery(qc);
		log.debug("testJoinConditionsOverride - expectedPageSql: " + expectedPageSql);
		log.debug("testJoinConditionsOverride - expectedCountSql: " + expectedCountSql);
		
		
		SqlBuilder<OverrideInvoice> sb = sbf.newSqlBuilder(OverrideInvoice.class);
		PagedCompiledSql cSql = sb.pagedSelect(0, 100, new EntityDescriptorNodeCallback() {
			
			@Override
			public boolean customize(Node<EntityDescriptor> node, Builder builder) {
				if (OverrideBill.class.equals(builder.getEntityClass())) {
					builder.joinConditionsOverrideParam(10);
				}
				return false;
			}
		}).pagedWhere().id().eq(20)
			.pagedSecondaryWhere().sql("? = ?", 17, 18)
			.pagedCompile();
		log.debug("testJoinConditionsOverride - page sql: " + cSql.getSqlString());
		log.debug("testJoinConditionsOverride - count sql: " + cSql.getSqlCountString());
		
		assertTrue(cSql.getSqlString().replace(" ", "").equals(expectedPageSql.replace(" ", "")));
		assertTrue(cSql.getSqlCountString().replace(" ", "").equals(expectedCountSql.replace(" ", "")));
		assertEquals(Arrays.asList(20, 0l, 100l, 10, 17, 18), cSql.getParametersList());
		assertEquals(Arrays.asList(20), cSql.getCountParametersList());
	}

	@Test
	public void testJoinConditionsOverride_useGroupBy() {
		
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(OverrideInvoice.class);
		QueryCriteria qc = new QueryCriteria.Builder(root)
			.useGroupByOnMainQuery(true)
			.mainWhereClause(ROOT + "id = ?")
			.secondaryWhereClause("? = ?")
			.build();
		String expectedPageSql = sf.buildPaginatedQuery(qc);
		String expectedCountSql = sf.buildCountQuery(qc);
		log.debug("testJoinConditionsOverride - expectedPageSql: " + expectedPageSql);
		log.debug("testJoinConditionsOverride - expectedCountSql: " + expectedCountSql);
		
		
		SqlBuilder<OverrideInvoice> sb = sbf.newSqlBuilder(OverrideInvoice.class);
		PagedCompiledSql cSql = sb.pagedSelect(0, 100, new EntityDescriptorNodeCallback() {
			
			@Override
			public boolean customize(Node<EntityDescriptor> node, Builder builder) {
				if (OverrideBill.class.equals(builder.getEntityClass())) {
					builder.joinConditionsOverrideParam(10);
				}
				return false;
			}
		}).pagedEnableGroupBy()
			.pagedWhere().id().eq(20)
			.pagedSecondaryWhere().sql("? = ?", 17, 18)
			.pagedCompile();
		log.debug("testJoinConditionsOverride - page sql: " + cSql.getSqlString());
		log.debug("testJoinConditionsOverride - count sql: " + cSql.getSqlCountString());
		
		assertTrue(cSql.getSqlString().replace(" ", "").equals(expectedPageSql.replace(" ", "")));
		assertTrue(cSql.getSqlCountString().replace(" ", "").equals(expectedCountSql.replace(" ", "")));
		assertEquals(Arrays.asList(10, 20, 0l, 100l, 10, 17, 18), cSql.getParametersList());
		assertEquals(Arrays.asList(10, 20), cSql.getCountParametersList());
	}
	
}
