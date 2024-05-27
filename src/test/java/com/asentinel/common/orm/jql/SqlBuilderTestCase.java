package com.asentinel.common.orm.jql;

import static com.asentinel.common.orm.EntityDescriptorNodeCallback.rootOnlyQuery;
import static com.asentinel.common.orm.jql.data.Bill.C_PH_NUMBER;
import static com.asentinel.common.orm.jql.data.Charge.C_CHARGE;
import static com.asentinel.common.orm.jql.data.Invoice.C_ACCOUNT;
import static com.asentinel.common.orm.jql.data.Invoice.C_NUMBER;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.SimpleNode;
import com.asentinel.common.collections.tree.TreeUtils;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.jdbc.arrays.NumberArray;
import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.SqlTemplates;
import com.asentinel.common.jdbc.flavors.postgres.PostgresJdbcFlavor;
import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeCallback;
import com.asentinel.common.orm.EntityDescriptorNodeMatcher;
import com.asentinel.common.orm.SimpleEntityDescriptor.Builder;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.orm.ed.tree.DefaultEntityDescriptorTreeRepository;
import com.asentinel.common.orm.ed.tree.EntityDescriptorTreeRepository;
import com.asentinel.common.orm.jql.data.Bill;
import com.asentinel.common.orm.jql.data.Charge;
import com.asentinel.common.orm.jql.data.Invoice;
import com.asentinel.common.orm.jql.data.OverrideBill;
import com.asentinel.common.orm.jql.data.OverrideInvoice;
import com.asentinel.common.orm.query.DefaultSqlFactory;
import com.asentinel.common.orm.query.SqlFactory;

public class SqlBuilderTestCase {
	
	final static Logger log = LoggerFactory.getLogger(SqlBuilderTestCase.class);
	
	EntityDescriptorTreeRepository edtr = new DefaultEntityDescriptorTreeRepository();
	JdbcFlavor jdbcf = new PostgresJdbcFlavor();
	SqlTemplates sqlTemplates = jdbcf.getSqlTemplates();
	SqlFactory sf = new DefaultSqlFactory(jdbcf);
	SqlQuery ex = createMock(SqlQuery.class);
	SqlBuilderFactory sbf = new DefaultSqlBuilderFactory(edtr, sf, ex);
	
	SqlBuilder<Charge> builder = sbf.newSqlBuilder(Charge.class);
	
	@Test(expected = IncorrectResultSizeDataAccessException.class)
	public void execForOptional_moreThan1Result() {		
		builder.select(Charge.TABLE_ALIAS);
		
		ex.query(anyObject(String.class), anyObject(EntityBuilder.class));
		expectLastCall()
			.andThrow(new IncorrectResultSizeDataAccessException(1));
		
		replay(ex);
		
		builder.execForOptional();
		
		verify(ex);
	}
	
	@Test
	public void execForOptional_empty() {		
		builder.select(Charge.TABLE_ALIAS);
		
		ex.query(anyObject(String.class), anyObject(EntityBuilder.class));
		expectLastCall()
			.andThrow(new EmptyResultDataAccessException(1));
		
		replay(ex);
		
		Optional<Charge> optional = builder.execForOptional();
		assertFalse(optional.isPresent());
		
		verify(ex);
	}
	
	@Test
	public void testInitialQuery() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(Charge.class, Charge.TABLE_ALIAS);
		builder.select(Charge.TABLE_ALIAS);
		CompiledSql cSql = builder.compile();
		log.debug("test - cSql: " + cSql);
		assertEquals(sf.buildQuery(root), cSql.getSqlString());
		assertEquals(0, cSql.getParameters().length);
		assertEquals(0, cSql.getParametersList().size());
	}
	
	@Test
	public void testCompiledSqlAliasedColumns() {
		CompiledSql cSql = sbf.newSqlBuilder(Charge.class).select(Charge.TABLE_ALIAS).compile();
		assertEquals(Charge.TABLE_ALIAS + "_" + Charge.C_CHARGE,
				cSql.getAliasedColumn(Charge.C_CHARGE));
		assertEquals(Charge.TABLE_ALIAS + "_" + Charge.C_CHARGE,
				cSql.getAliasedColumn(Charge.C_CHARGE, (Object[]) null));
		assertEquals(Charge.TABLE_ALIAS + "_" + Charge.C_CHARGE,
				cSql.getAliasedColumn(Charge.C_CHARGE, Charge.class));
		assertEquals(Bill.TABLE_ALIAS + "_" + Bill.C_PH_NUMBER,
				cSql.getAliasedColumn(Bill.C_PH_NUMBER, Charge.class, Bill.class));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testInitialQueryFail() {
		builder.compile();
	}
	

	@Test
	public void testAliasAndRootAlias() {
		builder.alias(Charge.class, Bill.class, Invoice.class)
			.column(Invoice.C_NUMBER).column(Invoice.C_ACCOUNT)
		;
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Invoice.TABLE_ALIAS + "." + C_NUMBER + Invoice.TABLE_ALIAS + "." + C_ACCOUNT,
				cSql.getSqlString().replace(" ", ""));
		
		builder.alias(Charge.class, Bill.class)
			.column(C_PH_NUMBER)
		;
		cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testAliasAndRootAlias - cSql: " + cSql);
		assertEquals(Invoice.TABLE_ALIAS + "." + C_NUMBER 
					+ Invoice.TABLE_ALIAS + "." + C_ACCOUNT
					+ Bill.TABLE_ALIAS + "." + C_PH_NUMBER
					,
				cSql.getSqlString().replace(" ", ""));
		
		builder.rootAlias()
			.column(C_CHARGE);
		cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testAliasAndRootAlias - cSql: " + cSql);
		assertEquals(Invoice.TABLE_ALIAS + "." + C_NUMBER 
					+ Invoice.TABLE_ALIAS + "." + C_ACCOUNT
					+ Bill.TABLE_ALIAS + "." + C_PH_NUMBER
					+ Charge.TABLE_ALIAS + "." + C_CHARGE
					,
				cSql.getSqlString().replace(" ", ""));
	}


	@Test
	public void testDefaultAlias() {
		builder.column(C_CHARGE);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + "." + C_CHARGE,
				cSql.getSqlString().replace(" ", ""));
	}
	
	@Test
	public void testAdvancedAlias() {
		builder.alias(Charge.class, 
				new TreeUtils.NodeMatcher<EntityDescriptor>() {

					@Override
					public boolean match(Node<? extends EntityDescriptor> node) {
						EntityDescriptor ed = node.getValue();
						return Bill.class.equals(ed.getEntityClass());
					}
			
				}, 
				Invoice.class);
		builder.compileAsIs(Charge.TABLE_ALIAS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdvancedAliasFail0() {
		builder.alias(Charge.class, 
				new TreeUtils.NodeMatcher<EntityDescriptor>() {

					@Override
					public boolean match(Node<? extends EntityDescriptor> node) {
						EntityDescriptor ed = node.getValue();
						return Integer.class.equals(ed.getEntityClass());
					}
			
				}, 
				Invoice.class);
		builder.compileAsIs(Charge.TABLE_ALIAS);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testAdvancedAliasFail1() {
		builder.alias(Charge.class, Invoice.class, Invoice.class);
		builder.compileAsIs(Charge.TABLE_ALIAS);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testAdvancedAliasFail2() {
		builder.alias(Charge.class, new EntityDescriptorNodeMatcher(Bill.class, "wrong alias"), Invoice.class);
		builder.compileAsIs(Charge.TABLE_ALIAS);
	}
	
	@Test
	public void testEmptyAliasPath() {
		builder.alias().column(Charge.C_CHARGE);
		builder.compileAsIs(Charge.TABLE_ALIAS);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + "." + C_CHARGE,
				cSql.getSqlString().replace(" ", ""));
	}
	
	@Test
	public void testNullAliasPath() {
		builder.alias((Object[]) null).column(Charge.C_CHARGE);
		builder.compileAsIs(Charge.TABLE_ALIAS);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + "." + C_CHARGE,
				cSql.getSqlString().replace(" ", ""));
	}
	

	
	@Test
	public void testSeparator() {
		builder.sep("_").column(C_CHARGE);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + "_" + C_CHARGE,
				cSql.getSqlString().replace(" ", ""));
	}

	@Test
	public void testString() {
		builder.where();
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(SqlBuilder.WHERE,
				cSql.getSqlString());
	}

	@Test
	public void testIdColumn1() {
		builder.id();
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + "." + Charge.C_ID,
				cSql.getSqlString().trim());
	}

	@Test
	public void testIdColumn2() {
		builder.alias(Charge.class, Bill.class).id();
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Bill.TABLE_ALIAS + "." + Bill.C_ID,
				cSql.getSqlString().trim());
	}
	
	@Test
	public void testIdColumn3() {
		builder.id(false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForCaseInsensitiveColumn(Charge.TABLE_ALIAS, ".", Charge.C_ID),
				cSql.getSqlString().trim());
	}
	
	@Test
	public void testIdColumn4() {
		builder.alias(Charge.class, Bill.class).id();
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Bill.TABLE_ALIAS + "." + Bill.C_ID,
				cSql.getSqlString().trim());
	}

	@Test
	public void testIdColumn5() {
		builder.alias(Charge.class, Bill.class).id(false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForCaseInsensitiveColumn(Bill.TABLE_ALIAS, ".", Bill.C_ID),
				cSql.getSqlString().trim());
	}

	@Test
	public void testIdColumn6() {
		builder.alias(Charge.class, Bill.class).id(true);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForCaseSensitiveColumn(Bill.TABLE_ALIAS, ".", Bill.C_ID),
				cSql.getSqlString().trim());
	}
	
	
	@Test
	public void testColumn1() {
		builder.alias(Charge.class, Bill.class).column(C_PH_NUMBER);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Bill.TABLE_ALIAS + "." + C_PH_NUMBER,
				cSql.getSqlString().trim());
	}
	
	@Test
	public void testColumn2() {
		builder.alias(Charge.class, Bill.class).column(C_PH_NUMBER);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(Bill.TABLE_ALIAS + "." + C_PH_NUMBER,
				cSql.getSqlString().trim());
	}

	@Test
	public void testColumn3() {
		builder.alias(Charge.class, Bill.class).column(C_PH_NUMBER, false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForCaseInsensitiveColumn(Bill.TABLE_ALIAS, ".", C_PH_NUMBER),
				cSql.getSqlString().trim());
	}

	@Test
	public void testColumn4() {
		builder.alias(Charge.class, Bill.class).column(C_PH_NUMBER, true);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForCaseSensitiveColumn(Bill.TABLE_ALIAS, ".", C_PH_NUMBER),
				cSql.getSqlString().trim());
	}
	

	@Test
	public void testParam1() {
		builder.param(10);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals("?",
				cSql.getSqlString().trim());
		assertEquals(1, cSql.getParametersList().size());
		assertEquals(10, cSql.getParametersList().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParam2() {
		builder.param(new int[0]);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testParam3() {
		builder.param(Collections.emptyList());
	}
	

	@Test
	public void testParam4() {
		int[] testArray = new int[0];
		NumberArray testNArray = new NumberArray("test", testArray);
		builder.in(testNArray);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("test - cSql: " + cSql);
		assertEquals(sqlTemplates.getSqlForInArray().trim(),
				cSql.getSqlString().trim());
		assertEquals(1, cSql.getParametersList().size());
		assertSame(testNArray, cSql.getParametersList().get(0));
	}
	
	@Test
	public void testParamString1() {
		builder.param("test");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testParamString1 - cSql: " + cSql);
		assertEquals("?",
				cSql.getSqlString().trim());
		assertEquals(1, cSql.getParametersList().size());
		assertEquals("test", cSql.getParametersList().get(0));
	}
	
	@Test
	public void testPlainSql1() {
		builder.sql(" test ");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testPlainSql1 - cSql: " + cSql);
		assertEquals("test",
				cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}

	@Test
	public void testPlainSql2() {
		builder.sql(" test ", (Object[]) null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testPlainSql2 - cSql: " + cSql);
		assertEquals("test",
				cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}

	@Test
	public void testPlainSql3() {
		builder.sql(" test ", 10);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testPlainSql3 - cSql: " + cSql);
		assertEquals("test",
				cSql.getSqlString().trim());
		assertEquals(1, cSql.getParameters().length);
		assertEquals(10, cSql.getParameters()[0]);
	}
	
	@Test
	public void insertCompiledSql() {
		CompiledSql cSql = new CompiledSql(new SimpleNode<>(EntityDescriptor.forIntPk(Object.class, (r, n) -> new Object(), "id")));
		cSql.appendSql("abc");
		cSql.addParameter(10);
		builder.sql(cSql);
		CompiledSql cSql2 = builder.compileAsIs(Charge.TABLE_ALIAS);
		assertEquals(cSql.getSqlString(), cSql2.getSqlString().trim());
		assertArrayEquals(cSql.getParameters(), cSql2.getParameters());
	}
	
	
	@Test
	public void testUpperParam() {
		builder.upperParam("test");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testUpperParam - cSql: " + cSql);
		assertEquals("upper(?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("test", cSql.getParameters()[0]);
	}
	
	@Test
	public void testUpperColumn() {
		builder.upperColumn("column");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testUpperColumn - cSql: " + cSql);
		assertEquals("upper(" + Charge.TABLE_ALIAS + ".column)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(0, cSql.getParameters().length);
	}

	@Test
	public void testLowerParam() {
		builder.lowerParam("test");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testLowerParam - cSql: " + cSql);
		assertEquals("lower(?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("test", cSql.getParameters()[0]);
	}
	
	@Test
	public void testLowerColumn() {
		builder.lowerColumn("column");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testLowerColumn - cSql: " + cSql);
		assertEquals("lower(" + Charge.TABLE_ALIAS + ".column)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(0, cSql.getParameters().length);
	}
	
	
	@Test
	public void testApplyLike_CaseSensitive() {
		builder.applyLike("test_col", "abc");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyLike_CaseSensitive - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("abc", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyLike_CaseInsensitive() {
		builder.applyLike("test_col", "abc", false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyLike_CaseInsensitive - cSql: " + cSql);
		assertEquals("upper(" + Charge.TABLE_ALIAS + ".test_col)likeupper(?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("abc", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyLike_Null() {
		builder.applyLike("test_col", null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyLike_Null - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertNull(cSql.getParameters()[0]);
	}
	

	@Test
	public void testApplyStartsWith_CaseSensitive() {
		builder.applyStartsWith("test_col", "abc");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyStartsWith_CaseSensitive - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("abc%", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyStartsWith_CaseInsensitive() {
		builder.applyStartsWith("test_col", "abc", false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyStartsWith_CaseInsensitive - cSql: " + cSql);
		assertEquals("upper(" + Charge.TABLE_ALIAS + ".test_col)likeupper(?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("abc%", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyStartsWith_Null() {
		builder.applyStartsWith("test_col", null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyStartsWith_Null - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%", cSql.getParameters()[0]);
	}

	@Test
	public void testApplyMultipleStartsWith_CaseSensitive() {
		builder.applyMultipleStartsWith("abc", "test_col1", "test_col2");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyMultipleStartsWith_CaseSensitive - cSql: " + cSql);
		assertEquals("(" + Charge.TABLE_ALIAS + ".test_col1like?or" + Charge.TABLE_ALIAS + ".test_col2like?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(2, cSql.getParameters().length);
		assertEquals("abc%", cSql.getParameters()[0]);
		assertEquals("abc%", cSql.getParameters()[1]);
	}
	
	@Test
	public void testApplyMultipleStartsWith_CaseInsensitive() {
		builder.applyMultipleStartsWith("abc", false, "test_col1", "test_col2");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyMultipleStartsWith_CaseInsensitive - cSql: " + cSql);
		assertEquals("(upper(" + Charge.TABLE_ALIAS + ".test_col1)likeupper(?)orupper(" + Charge.TABLE_ALIAS + ".test_col2)likeupper(?))",
				cSql.getSqlString().replace(" ", ""));		
		assertEquals(2, cSql.getParameters().length);
		assertEquals("abc%", cSql.getParameters()[0]);
		assertEquals("abc%", cSql.getParameters()[1]);
	}
	
	@Test
	public void applyMultipleStartsWith_NullParam() {
		builder.applyMultipleStartsWith(null, "test_col");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("applyMultipleStartsWith_NullParam - cSql: " + cSql);
		assertEquals("(" + Charge.TABLE_ALIAS + ".test_collike?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%", cSql.getParameters()[0]);
	}
	
	@Test
	public void applyMultipleStartsWith_NoColumns() {
		builder.applyMultipleStartsWith("abc");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("applyMultipleStartsWith_NoColumns - cSql: " + cSql);
		assertEquals("", cSql.getSqlString());
		assertEquals(0, cSql.getParameters().length);		
	}
	
	@Test
	public void testApplyMultipleContains_CaseInsensitive() {
		builder.applyMultipleContains("abc", false, "test_col1", "test_col2");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyMultipleContains_CaseInsensitive - cSql: " + cSql);
		assertEquals("(upper(" + Charge.TABLE_ALIAS + ".test_col1)likeupper(?)orupper(" + Charge.TABLE_ALIAS + ".test_col2)likeupper(?))",
				cSql.getSqlString().replace(" ", ""));		
		assertEquals(2, cSql.getParameters().length);
		assertEquals("%abc%", cSql.getParameters()[0]);
		assertEquals("%abc%", cSql.getParameters()[1]);
	}
	
	@Test
	public void applyMultipleContains_NullParam() {
		builder.applyMultipleContains(null, "test_col");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("applyMultipleContains_NullParam - cSql: " + cSql);
		assertEquals("(" + Charge.TABLE_ALIAS + ".test_collike?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%%", cSql.getParameters()[0]);
	}
	
	@Test
	public void applyMultipleContains_NoColumns() {
		builder.applyMultipleContains("abc");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("applyMultipleContains_NoColumns - cSql: " + cSql);
		assertEquals("", cSql.getSqlString());
		assertEquals(0, cSql.getParameters().length);		
	}
	
	@Test
	public void testApplyContains_CaseSensitive() {
		builder.applyContains("test_col", "abc");
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyContains_CaseSensitive - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%abc%", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyContains_CaseInsensitive() {
		builder.applyContains("test_col", "abc", false);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyContains_CaseInsensitive - cSql: " + cSql);
		assertEquals("upper(" + Charge.TABLE_ALIAS + ".test_col)likeupper(?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%abc%", cSql.getParameters()[0]);
	}
	
	@Test
	public void testApplyContains_Null() {
		builder.applyContains("test_col", null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testApplyContains_Null - cSql: " + cSql);
		assertEquals(Charge.TABLE_ALIAS + ".test_collike?",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("%%", cSql.getParameters()[0]);
	}
	
	
	@Test
	public void testInWithOr_withObjects() {
		Integer[] notNullParams = {1, 2, 3}; // tests null as well
		Integer[] params  = new Integer[notNullParams.length + 1];
		System.arraycopy(notNullParams, 0, params, 0, notNullParams.length);
		
		builder.inObjects(Charge.C_ID, (Object[]) params);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withObjects - cSql: " + cSql);
		
		assertEquals(String.format("(c.%s=?orc.%s=?orc.%s=?orc.%sisnull)", Charge.C_ID, Charge.C_ID, Charge.C_ID, Charge.C_ID), 
				cSql.getSqlString().replace(" ", ""));
		assertArrayEquals(notNullParams, cSql.getParameters());
	}
	
	@Test
	public void testInWithOr_withObjects_id() {
		Integer[] notNullParams = {1, 2, 3}; // tests null as well
		Integer[] params  = new Integer[notNullParams.length + 1];
		System.arraycopy(notNullParams, 0, params, 0, notNullParams.length);
		
		builder.idIn((Object[]) params);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withObjects - cSql: " + cSql);
		
		assertEquals(String.format("(c.%s=?orc.%s=?orc.%s=?orc.%sisnull)", Charge.C_ID, Charge.C_ID, Charge.C_ID, Charge.C_ID), 
				cSql.getSqlString().replace(" ", ""));
		assertArrayEquals(notNullParams, cSql.getParameters());
	}
	

	@Test
	public void testInWithOr_withObjects_Null_Array() {
		builder.inObjects("test_col", (Object[]) null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withObjects_Null_Array - cSql: " + cSql);
		
		assertEquals(SqlBuilder.NO_RESULTS, cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}
	
	@Test
	public void testInWithOr_withObjects_Empty_Array() {
		builder.inObjects("test_col", new Object[0]);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withObjects_Empty_Array - cSql: " + cSql);
		
		assertEquals(SqlBuilder.NO_RESULTS, cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}

	@Test
	public void testInWithOr_withObjects_Null_In_Array() {
		builder.inObjects("test_col", new Object[]{null});
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withObjects_Null_In_Array - cSql: " + cSql);
		
		assertEquals("(c.test_colisnull)", cSql.getSqlString().replace(" ", ""));
		assertEquals(0, cSql.getParameters().length);
	}
	

	@Test
	public void testInWithOr_withPrimitives_ints() {
		int[] params = {1, 2, 3};
		builder.in("test_col", params);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withPrimitives_ints - cSql: " + cSql);
		
		assertEquals("(c.test_col=?orc.test_col=?orc.test_col=?)", 
				cSql.getSqlString().replace(" ", ""));
		assertEquals(params.length, cSql.getParameters().length);
	}
	
	@Test
	public void testInWithOr_withPrimitive_Null_Int_Array() {
		builder.in("test_col", (int[]) null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withPrimitive_Null_Int_Array - cSql: " + cSql);
		
		assertEquals(SqlBuilder.NO_RESULTS, cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}
	

	@Test
	public void testInWithOr_withPrimitives_longs() {
		long[] params = {1, 2, 3};
		builder.in("test_col", params);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withPrimitives_longs - cSql: " + cSql);
		
		assertEquals("(c.test_col=?orc.test_col=?orc.test_col=?)", 
				cSql.getSqlString().replace(" ", ""));
		assertEquals(params.length, cSql.getParameters().length);
	}
	
	@Test
	public void testInWithOr_withPrimitive_Null_Long_Array() {
		builder.in("test_col", (int[]) null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testInWithOr_withPrimitive_Null_Long_Array - cSql: " + cSql);
		
		assertEquals(SqlBuilder.NO_RESULTS, cSql.getSqlString().trim());
		assertEquals(0, cSql.getParameters().length);
	}
	
	@Test
	public void testBetween() {
		builder.between(1, 2);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testBetween - cSql: " + cSql);
		
		assertEquals("between?and?", cSql.getSqlString().replace(" ", ""));
		assertEquals(2, cSql.getParameters().length);
		assertEquals(1, cSql.getParameters()[0]);
		assertEquals(2, cSql.getParameters()[1]);
	}
	
	@Test
	public void testEqNotNull() {
		builder.eq(10);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testEqNotNull - cSql: " + cSql);
		assertEquals("=?", cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals(10, cSql.getParameters()[0]);
	}

	@Test
	public void testEqNull() {
		builder.eq(null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testEqNull - cSql: " + cSql);
		assertEquals("=?", cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertNull(cSql.getParameters()[0]);

	}
	
	@Test
	public void testNeNotNull() {
		builder.ne(10);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testNeNotNull - cSql: " + cSql);
		assertEquals("<>?", cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals(10, cSql.getParameters()[0]);
	}

	@Test
	public void testNeNull() {
		builder.ne(null);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("testNeNull - cSql: " + cSql);
		assertEquals("<>?", cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertNull(cSql.getParameters()[0]);
	}
	
	// --------- tests for join conditions override ---------
	
	@Test
	public void testJoinConditionsOverride() {
		Node<EntityDescriptor> root = edtr.getEntityDescriptorTree(OverrideInvoice.class);
		String expectedSql = sf.buildQuery(root);
		log.debug("testJoinConditionsOverride - expectedSql: " + expectedSql);
		
		SqlBuilder<OverrideInvoice> sb = sbf.newSqlBuilder(OverrideInvoice.class);
		CompiledSql cSql = sb.select(new EntityDescriptorNodeCallback() {
			
			@Override
			public boolean customize(Node<EntityDescriptor> node, Builder builder) {
				if (OverrideBill.class.equals(builder.getEntityClass())) {
					builder.joinConditionsOverrideParam(10);
				}
				return false;
			}
		}).where().id().eq(20)
			.compile();
		log.debug("testJoinConditionsOverride - sql: " + cSql.getSqlString());
		assertTrue(cSql.getSqlString().startsWith(expectedSql));
		assertEquals(Arrays.asList(10, 20), cSql.getParametersList());
	}
	
	@Test
	public void testAscNullsFirst() {
		assertTrue(builder.select().asc(Nulls.FIRST).compile().getSqlString().endsWith("asc nulls first"));
	}

	@Test
	public void testAscNullsLast() {
		assertTrue(builder.select().asc(Nulls.LAST).compile().getSqlString().endsWith("asc nulls last"));
	}

	@Test
	public void testDescNullsFirst() {
		assertTrue(builder.select().desc(Nulls.FIRST).compile().getSqlString().endsWith("desc nulls first"));
	}

	@Test
	public void testDescNullsLast() {
		assertTrue(builder.select().desc(Nulls.LAST).compile().getSqlString().endsWith("desc nulls last"));
	}
	
	@Test
	public void testExists() {
		builder.select().where().exists("inner select", 10);
		assertTrue(builder.compile().getSqlString().endsWith("exists ( inner select )"));
		assertEquals(Collections.singletonList(10), builder.compile().getParametersList());
	}

	@Test
	public void testCountId() {
		builder.select().where().countId();
		assertTrue(builder.compile().getSqlString().endsWith("count ( t0." + Charge.C_ID + " )"));
		
	}

	@Test
	public void testCount() {
		builder.select().where().count("TestColumn");
		assertTrue(builder.compile().getSqlString().endsWith("count ( t0.TestColumn )"));
	}
	
	@Test
	public void testTable1() {
		builder.table();
		assertEquals(TargetMembersHolder.getInstance().getTargetMembers(Charge.class).getTableAnnotation().value(),
				builder.compileAsIs("").getSqlString().trim());
	}
	
	@Test
	public void testTable2() {
		builder.alias(null, Bill.class).table();
		assertEquals(TargetMembersHolder.getInstance().getTargetMembers(Bill.class).getTableAnnotation().value(),
				builder.compileAsIs("").getSqlString().trim());
	}
	
	@Test
	public void coalesce() {
		builder.coalesce("column", 100);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("coalesce - cSql: " + cSql);
		assertEquals("coalesce(" + Charge.TABLE_ALIAS + ".column,?)",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals(100, cSql.getParameters()[0]);
	}
	
	@Test
	public void coalesceUpper() {
		builder.coalesce("column", "abc", true);
		CompiledSql cSql = builder.compileAsIs(Charge.TABLE_ALIAS);		
		log.debug("coalesce - cSql: " + cSql);
		assertEquals("upper(coalesce(" + Charge.TABLE_ALIAS + ".column,?))",
				cSql.getSqlString().replace(" ", ""));
		assertEquals(1, cSql.getParameters().length);
		assertEquals("abc", cSql.getParameters()[0]);
	}
	

	// tests for from queries
	
	@Test
	public void testInitialFromQuery() {
		builder.selectK()
			.id()
			.comma().column(Charge.C_CHARGE)
			.alias(Charge.class, Bill.class)
			.id()
			.comma().column(Bill.C_PH_NUMBER)
			.rootAlias()
			.id()
		.from();
		CompiledSql cSql = builder.compile();
		log.debug("test - cSql: " + cSql.getSqlString());
		assertEquals("select t0.c_id, t0.charge b.b_id, b.phonenumber t0.c_id from charge t0 left join bill b on t0.b_id = b.b_id left join invoice i on b.id = i.id".replace(" ", ""),
				cSql.getSqlString().replace(" ", "").toLowerCase());
		assertEquals(0, cSql.getParameters().length);
		assertEquals(0, cSql.getParametersList().size());
	}

	
	// tests for the EntityDescriptorNodeMatcher rootOnlyQuery() method

	@Test
	public void testAll() {
		CompiledSql cSql = builder.select().compile();
		assertTrue(cSql.getSqlString().toLowerCase().contains("bill"));
		assertTrue(cSql.getSqlString().toLowerCase().contains("invoice"));
	}


	@Test
	public void testRootOnlyQuery() {
		CompiledSql cSql = builder.select(rootOnlyQuery()).compile();
		assertFalse(cSql.getSqlString().toLowerCase().contains("bill"));
		assertFalse(cSql.getSqlString().toLowerCase().contains("invoice"));
		
	}
	
	
}
