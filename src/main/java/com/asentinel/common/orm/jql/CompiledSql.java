package com.asentinel.common.orm.jql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorUtils;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class CompiledSql {

	final static String QUESTION_MARK = " ?";
	
	
	private final Node<EntityDescriptor> root;
	private final StringBuilder sql = new StringBuilder();
	private final List<Object> params = new ArrayList<Object>();
	
	public CompiledSql(Node<EntityDescriptor> root) {
		Assert.assertNotNull(root, "root");
		this.root = root;
	}
	
	public Node<EntityDescriptor> getRootNode() {
		return root;
	}
	
	CompiledSql appendSql(Object sql) {
		if (sql == null) {
			return this;
		}
		this.sql.append(sql);
		return this;
	}
	
	public String getSqlString() {
		return sql.toString();
	}
	
	/**
	 * @see EntityDescriptorUtils#getEntityDescriptor(Node, Object...)
	 */
	public String getAliasedColumn(String column, Object ... path) throws IllegalArgumentException {
		Assert.assertNotEmpty(column, "column");
		QueryReady qr = (QueryReady) EntityDescriptorUtils.getEntityDescriptor(root, path);
		return new StringBuilder().append(qr.getTableAlias())
				.append(qr.getColumnAliasSeparator()).append(column)
				.toString();
	}
	
	CompiledSql addParameter(Object param) {
		appendSql(QUESTION_MARK);
		params.add(param);
		return this;
	}
	
	CompiledSql addParametersStrict(Object ... params) {
		if (params == null) {
			return this;
		}
		this.params.addAll(Arrays.asList(params));
		return this;
	}

	CompiledSql addParametersStrict(List<Object> params) {
		if (params == null) {
			return this;
		}
		this.params.addAll(params);
		return this;
	}
	
	
	public Object[] getParameters() {
		return params.toArray(new Object[params.size()]);
	}
	
	public List<Object> getParametersList() {
		return params;
	}
	
	@Override
	public String toString() {
		return "CompiledSql [sql=" + sql + ", params=" + params + "]";
	}
}
