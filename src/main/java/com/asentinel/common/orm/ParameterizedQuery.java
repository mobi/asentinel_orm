package com.asentinel.common.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.orm.query.SqlFactory;

/**
 * Class that stores a SQL query and its associated parameters. The parameters are stored in
 * a main list and a secondary list to accommodate pagination query building methods in {@link SqlFactory}.<br>
 * The query building methods in {@code SqlFactory} collect the parameters associated with each node in this class
 * if join conditions overriding with parameters is used.
 * 
 * @see SqlFactory#buildParameterizedQuery(com.asentinel.common.collections.tree.Node)
 * @see SqlFactory#buildPaginatedParameterizedQuery(QueryCriteria)
 * @see SqlFactory#buildCountParameterizedQuery(QueryCriteria)
 *  
 * @author Razvan Popian
 */
public class ParameterizedQuery {
	private String sql;
	private List<Object> mainParameters;
	private List<Object> secondaryParameters;
	
	/**
	 * @return the SQL string. Should contain one question mark placeholder for each parameter
	 * 			found in the main and secodary parameters lists. It never returns <code>null</code>.
	 */
	public String getSql() {
		if (!StringUtils.hasLength(sql)) {
			return "";
		}
		return sql;
	}
	
	/**
	 * @see #getSql()
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	/**
	 * @return the main parameters list.
	 */
	public List<Object> getMainParameters() {
		if (mainParameters == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(mainParameters);
	}
	
	/**
	 * Add the parameters to the main parameters list.
	 * @param parameters the parameters to add.
	 */
	public void addMainParameters(List<Object> parameters) {
		if (CollectionUtils.isEmpty(parameters)) {
			return;
		}
		if (this.mainParameters == null) {
			this.mainParameters = new ArrayList<>();
		}
		this.mainParameters.addAll(parameters);
	}

	/**
	 * @return the secondary parameters list.
	 */
	public List<Object> getSecondaryParameters() {
		if (secondaryParameters == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(secondaryParameters);
	}

	/**
	 * Add the parameters to the secondary parameters list.
	 * @param parameters the parameters to add.
	 */
	public void addSecondaryParameters(List<Object> parameters) {
		if (CollectionUtils.isEmpty(parameters)) {
			return;
		}
		if (this.secondaryParameters == null) {
			this.secondaryParameters = new ArrayList<>();
		}
		this.secondaryParameters.addAll(parameters);
	}

	@Override
	public String toString() {
		return "ParameterizedQuery [sql=" + sql + ", mainParameters="
				+ mainParameters + ", secondaryParameters="
				+ secondaryParameters + "]";
	}

}

