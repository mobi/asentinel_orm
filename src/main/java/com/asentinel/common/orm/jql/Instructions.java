package com.asentinel.common.orm.jql;

import java.util.ArrayList;
import java.util.List;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorUtils;
import com.asentinel.common.orm.ParameterizedQuery;
import com.asentinel.common.orm.QueryCriteria;
import com.asentinel.common.orm.QueryReady;
import com.asentinel.common.orm.query.SqlFactory;
import com.asentinel.common.util.Assert;

/**
 * Wrapper class around a list of instructions. There are 2 key methods:
 * <li>
 * 	{@link #compile()}
 * <li>
 * 	{@link #compile(Node)}
 * 
 *  @see Instruction
 *  @see InstructionType
 *  
 *  @author Razvan Popian
 */
class Instructions {
	final static String DEFAULT_SEPARATOR = ".";
	
	private final SqlFactory sqlFactory;
	
	private final List<Instruction> instructions = new ArrayList<Instruction>();
	
	Instructions(SqlFactory sqlFactory) {
		this.sqlFactory = sqlFactory; 
	}
	
	/**
	 * Adds a new instruction to the internal list.
	 */
	public void add(Instruction instruction) {
		Assert.assertNotNull(instruction, "instruction");
		instructions.add(instruction);
	}
	
	/**
	 * Empties the internal instructions list.
	 */
	public void clear() {
		instructions.clear();
	}
	
	/**
	 * Compiles the instructions to SQL. This method will only work if a valid {@link InstructionType#INITIAL_QUERY}
	 * instruction was added.
	 * @return a {@link CompiledSql} object holding the SQL string, any parameters and the {@link EntityDescriptor} tree that
	 * 			was used for compilation.
	 * @throws IllegalStateException if a valid {@link InstructionType#INITIAL_QUERY} can not be found.
	 */
	@SuppressWarnings("unchecked")
	public CompiledSql compile() {
		for (Instruction instruction: instructions) {
			if (InstructionType.INITIAL_QUERY.equals(instruction.getType())) {
				return compile((Node<EntityDescriptor>) instruction.getActual());		
			} else if (InstructionType.PAGED_INITIAL_QUERY.equals(instruction.getType())) {
				Object[] params = (Object[]) instruction.getActual();
				Node<EntityDescriptor> root = (Node<EntityDescriptor>) params[0]; 
				return compile(root);				
			} else if (InstructionType.FROM_QUERY.equals(instruction.getType())) {
				return compile((Node<EntityDescriptor>) instruction.getActual());
			}

		}
		throw new IllegalStateException("No query initialization instruction found.");
	}
	
	
	private String getIdColumn(EntityDescriptor activeEd) {
		if (activeEd instanceof QueryReady) {
			return ((QueryReady) activeEd).getPkName();
		} else {
			throw new IllegalArgumentException("The active EntityDescriptor is not a QueryReady, "
					+ "so the PK name can not be determined.");
		}
	}
	
	/**
	 * Compiles the instructions to sql.
	 * @param root the {@link EntityDescriptor} tree to use for compilation. 
	 * @return a {@link CompiledSql} object holding the sql string, any parameters and the {@link EntityDescriptor} tree that
	 * 			was used for compilation.
	 */
	public CompiledSql compile(Node<EntityDescriptor> root) {
		Assert.assertNotNull(root, "root");
		
		CompiledSql cMainSql = new CompiledSql(root);
		CompiledSql cMainOrderBySql = new CompiledSql(root);
		CompiledSql cMainHavingSql = new CompiledSql(root);
		CompiledSql cSecondarySql = new CompiledSql(root);
		
		long beginIndex = 0;
		long endIndex = 0;
		boolean paginated = false;
		boolean pagedUseGroupBy = false;
		String[] pagedAdditionalColumns = null;
		String[] pagedGroupByAdditionalColumns = null;
		QueryReady activeEd = (QueryReady) root.getValue();
		String activeSep = DEFAULT_SEPARATOR;
		CompiledSql cCurrentSql = cMainSql;
		
		
		for (Instruction instr: instructions) {
			Object[] params;
			boolean caseSensitive;
			switch (instr.getType()) {
				case INITIAL_QUERY:
					ParameterizedQuery snippet = sqlFactory.buildParameterizedQuery(root);
					cCurrentSql.appendSql(snippet.getSql());
					cCurrentSql.addParametersStrict(snippet.getMainParameters());
					break;
				case FROM_QUERY:
					ParameterizedQuery snippetFrom = sqlFactory.buildParameterizedFromQuery(root);
					cCurrentSql.appendSql(snippetFrom.getSql());
					cCurrentSql.addParametersStrict(snippetFrom.getMainParameters());
					break;
				case PAGED_INITIAL_QUERY:
					params = (Object[]) instr.getActual();
					beginIndex = (Long) params[1];
					endIndex = (Long) params[2];
					paginated = true;
					break;
				case PAGED_MAIN_WHERE:
					cCurrentSql = cMainSql;
					pagedAdditionalColumns = (String[]) instr.getActual();
					break;
				case PAGED_MAIN_ORDER_BY:
					cCurrentSql = cMainOrderBySql;
					break;
				case PAGED_SECONDARY_WHERE:
					cCurrentSql = cSecondarySql;
					break;
				case PAGED_USE_GROUP_BY:
					pagedUseGroupBy = true;
					pagedGroupByAdditionalColumns = (String[]) instr.getActual();
					break;
				case PAGED_HAVING:
					cCurrentSql = cMainHavingSql;
					break;
				case STRING:
					cCurrentSql.appendSql(instr.getActual());
					break;
				case PATH:
					EntityDescriptor temp = EntityDescriptorUtils.getEntityDescriptor(root, (Object[]) instr.getActual());
					if (!(temp instanceof QueryReady)) {
						throw new ClassCastException("It looks like you are trying to access an entity table that is not part of the generated query, "
								+ "possibly because it is lazy loaded. The offending entity type is " + temp.getEntityClass() + " .");
					}
					activeEd = (QueryReady) temp;
					break;
				case PATH_ROOT:
					activeEd = (QueryReady) root.getValue();
					break;
				case ID_COLUMN:
					cCurrentSql.appendSql(" ");
					if (instr.getActual() == null) {
						// render plain id column
						cCurrentSql.appendSql(activeEd.getTableAlias());
						cCurrentSql.appendSql(activeSep);
						cCurrentSql.appendSql(getIdColumn((EntityDescriptor) activeEd));
					} else {
						 // render case sensitive/non sensitive column for order by
						caseSensitive = (Boolean) instr.getActual();
						if (caseSensitive) {
							cCurrentSql.appendSql(
									sqlFactory.getSqlTemplates().getSqlForCaseSensitiveColumn(
										activeEd.getTableAlias(), activeSep, getIdColumn((EntityDescriptor) activeEd))
							);
						} else {
							cCurrentSql.appendSql(
									sqlFactory.getSqlTemplates().getSqlForCaseInsensitiveColumn(
										activeEd.getTableAlias(), activeSep, getIdColumn((EntityDescriptor) activeEd))
							);
						}
					}
					break;
				case ID_ALIAS:
					cCurrentSql.appendSql(" ");
					cCurrentSql.appendSql(activeEd.getTableAlias());
					cCurrentSql.appendSql(activeEd.getColumnAliasSeparator());
					cCurrentSql.appendSql(getIdColumn((EntityDescriptor) activeEd));	
					break;					
				case COLUMN:
					cCurrentSql.appendSql(" ");
					params = (Object[]) instr.getActual();
					String col = (String) params[0];
					if (params[1] == null) {
						// render plain column
						cCurrentSql.appendSql(activeEd.getTableAlias());
						cCurrentSql.appendSql(activeSep);
						cCurrentSql.appendSql(col);						
					} else {
						// render case sensitive/non sensitive column for order by
						caseSensitive = (Boolean) params[1];
						if (caseSensitive) {
							cCurrentSql.appendSql(
									sqlFactory.getSqlTemplates().getSqlForCaseSensitiveColumn(
										activeEd.getTableAlias(), activeSep, col)
							);
						} else {
							cCurrentSql.appendSql(
									sqlFactory.getSqlTemplates().getSqlForCaseInsensitiveColumn(
										activeEd.getTableAlias(), activeSep, col)
							);
						}
					}
					break;
				case COLUMN_ALIAS:
					String colToAlias = (String) instr.getActual();
					cCurrentSql.appendSql(" ");
					cCurrentSql.appendSql(activeEd.getTableAlias());
					cCurrentSql.appendSql(activeEd.getColumnAliasSeparator());
					cCurrentSql.appendSql(colToAlias);	
					break;
				case PARAM:
				case STRING_PARAM:
				case ARRAY_PARAM:
					cCurrentSql.addParameter(instr.getActual());
					break;
				case SEPARATOR:
					activeSep = instr.getActual().toString();
					break;
				case SQL:
					Object[] iParams = (Object[]) instr.getActual();
					cCurrentSql.appendSql(iParams[0]);
					cCurrentSql.addParametersStrict((Object[]) iParams[1]);
					break;
				case TABLE:
					cCurrentSql.appendSql(" ");
					cCurrentSql.appendSql(activeEd.getTableName());
					break;
				default:
					throw new IllegalStateException("Unknown instruction:" + instr + " .");
			}
		}
		if (paginated) {
			QueryCriteria criteria = new QueryCriteria.Builder(root)
								.mainAdditionalColumns(pagedAdditionalColumns)
								.mainWhereClause(cMainSql.getSqlString().trim())
								.useGroupByOnMainQuery(pagedUseGroupBy)
								.mainGroupByAdditionalColumns(pagedGroupByAdditionalColumns)
								.mainHavingClause(cMainHavingSql.getSqlString().trim())
								.mainOrderByClause(cMainOrderBySql.getSqlString().trim())
								.secondaryWhereClause(cSecondarySql.getSqlString().trim())
								.build();
			ParameterizedQuery pageSnippet = sqlFactory.buildPaginatedParameterizedQuery(criteria);
			ParameterizedQuery countSnippet = sqlFactory.buildCountParameterizedQuery(criteria);
			PagedCompiledSql finalCSql = new PagedCompiledSql(root);
			finalCSql.appendSql(pageSnippet.getSql());
			finalCSql
				.addParametersStrict(pageSnippet.getMainParameters())
				.addParametersStrict(cMainSql.getParameters())
				.addParametersStrict(cMainHavingSql.getParameters())
				.addParametersStrict(
						(Object[]) sqlFactory.getSqlTemplates().applyRangeTransformation(beginIndex, endIndex)
				)
				.addParametersStrict(pageSnippet.getSecondaryParameters())
				.addParametersStrict(cSecondarySql.getParameters());
			finalCSql.setSqlCountString(countSnippet.getSql());
			finalCSql
				.addCountParametersStrict(countSnippet.getMainParameters())
				.addCountParametersStrict(cMainSql.getParameters())
				.addCountParametersStrict(cMainHavingSql.getParameters());
			return finalCSql;
		} else {
			return cMainSql;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (Instruction instr: instructions) {
			sb.append(instr.toString()).append("\n");
		}
		return sb.toString();
	}

}
