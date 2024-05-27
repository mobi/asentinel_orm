package com.asentinel.common.orm.jql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.util.Assert;

/**
 * @author Razvan Popian
 */
public class PagedCompiledSql extends CompiledSql {
	
	private String countSql;
	private final List<Object> countParams = new ArrayList<Object>();
	

	PagedCompiledSql(Node<EntityDescriptor> root) {
		super(root);
	}
	
	void setSqlCountString(String countSql) {
		Assert.assertNotNull(countSql, "countSql");
		this.countSql = countSql;
	}
	
	public String getSqlCountString() {
		return countSql;
	}
	

	PagedCompiledSql addCountParametersStrict(Object ... params) {
		if (params == null) {
			return this;
		}
		this.countParams.addAll(Arrays.asList(params));
		return this;
	}
	
	PagedCompiledSql addCountParametersStrict(List<Object> params) {
		if (params == null) {
			return this;
		}
		this.countParams.addAll(params);
		return this;
	}
	

	public Object[] getCountParameters() {
		return countParams.toArray(new Object[countParams.size()]);
	}

	public List<Object> getCountParametersList() {
		return countParams;
	}
	
}
