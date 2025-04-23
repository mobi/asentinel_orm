package com.asentinel.common.orm;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.orm.query.SqlFactory;

/**
 * Extension of the {@link SimpleEntityDescriptor} class that adds many to many
 * information.
 *  
 * @author Razvan Popian
 */
public class ManyToManyEntityDescriptor extends SimpleEntityDescriptor implements ManyToManyQueryReady {
	
	private final String manyToManyTable;
	private final String manyToManyTableAlias;
	private final String manyToManyLeftFkName;
	private final String manyToManyRightFkName;
	
	private final String manyToManyRightJoinConditionsOverride; 
	private final List<Object> manyToManyRightJoinConditionsOverrideParams;
	
	
	/**
	 * Constructor for this class. The relation to the parent is defaulted to 
	 * {@link RelationType#MANY_TO_MANY} and left joins will be used 
	 * to join this descriptor to its link table and the link table to the parent. 
	 * Use the {@link Builder} for this class if you want to override default values.
	 *   
	 * @param clazz the class of the <code>Entity</code>.
	 * @param manyToManyTable the link table.
	 */
	public ManyToManyEntityDescriptor(Class<?> clazz, String manyToManyTable) {
		this(new Builder(clazz, manyToManyTable).preBuild());
	}
	
	
	/**
	 * Builder constructor.
	 */
	protected ManyToManyEntityDescriptor(Builder builder) {
		super(builder);
		if (!StringUtils.hasText(builder.manyToManyTable)) {
			throw new IllegalArgumentException("The parent relation is many to many, but no connecting table was specified.");
		}
		this.manyToManyTable = builder.manyToManyTable;
		if (StringUtils.hasText(builder.manyToManyTableAlias)) {
			this.manyToManyTableAlias = builder.manyToManyTableAlias;
		} else {
			// default
			this.manyToManyTableAlias = QueryUtils.nextTableAlias();
		}
		this.manyToManyLeftFkName = builder.manyToManyLeftFkName;
		this.manyToManyRightFkName = builder.manyToManyRightFkName;

		// set join conditions override and additional params for many to many right join
		// the left join overrides are set in the super class
		this.manyToManyRightJoinConditionsOverride = builder.manyToManyRightJoinConditionsOverride;
		this.manyToManyRightJoinConditionsOverrideParams = builder.manyToManyRightJoinConditionsOverrideParams;
	}
	
	/**
	 * This is only for many to many relationships.
	 * @return the name of the table that links the table
	 * 			represented by this entity to the table
	 * 			represented by the parent entity.
	 */
	@Override
	public String getManyToManyTable() {
		return manyToManyTable;
	}
	

	/**
	 * This is only for many to many relationships.
	 * @return the alias of the link table in a many to many relationship.
	 * 			If not specified on construction a default value will be used.
	 */
	@Override
	public String getManyToManyTableAlias() {
		return manyToManyTableAlias;
	}

	/**
	 * This is only for many to many relationships.
	 * @return the name of the column in the link table that connects
	 * 			the link table to the parent entity table. If this is 
	 * 			{@code null} the {@link SqlFactory#buildQuery(Node)} method will use the same 
	 * 			name as the name of the primary key in the parent entity table.
	 */
	@Override
	public String getManyToManyLeftFkName() {
		return manyToManyLeftFkName;
	}

	/**
	 * This is only for many to many relationships.
	 * @return the name of the column in the link table that connects
	 * 			the link table to this entity table. If this is {@code null}
	 * 			the {@link SqlFactory#buildQuery(Node)} method will use the same 
	 * 			name as the name of the primary key in this entity table.
	 */
	@Override
	public String getManyToManyRightFkName() {
		return manyToManyRightFkName;
	}
	
	
	@Override
	public String getManyToManyRightJoinConditionsOverride() {
		return manyToManyRightJoinConditionsOverride;
	}
	
	@Override
	public List<Object> getManyToManyRightJoinConditionsOverrideParams() {
		return manyToManyRightJoinConditionsOverrideParams;
	}
	
	@Override
	public String toString() {
		return "ManyToManyEntityDescriptor [pkName=" + getPkName() 
				+ ", class=" + getEntityClass().getName() 
				+ ", link table=" + getManyToManyTable()
				+ "]";
	}
	

	// Builder classes
	
	public static class Builder extends SimpleEntityDescriptor.Builder {
		protected String manyToManyTable;
		protected String manyToManyTableAlias;
		protected String manyToManyLeftFkName;
		protected String manyToManyRightFkName;
		
		protected String manyToManyRightJoinConditionsOverride; 
		protected List<Object> manyToManyRightJoinConditionsOverrideParams;
		

		public Builder(Class<?> entityClass, String manyToManyTable) {
			super(entityClass);
			parentRelationType(RelationType.MANY_TO_MANY);
			manyToManyTable(manyToManyTable);
		}
		
		/**
		 * @see super{@link #clone()}
		 */
		@Override
		public Builder clone() {
			return (Builder) super.clone();
		}

		
		// inherited Builder methods
		
		@Override
		public Builder name(Object name) {
			return (Builder) super.name(name);
		}

		@Override
		public Builder pkName(String pkName) {
			return (Builder) super.pkName(pkName);
		}

		@Override
		public Builder fkName(String fkName) {
			return (Builder) super.fkName(fkName);
		}

		@Override
		public Builder tableName(String tableName) {
			return (Builder) super.tableName(tableName);
		}

		@Override
		public Builder tableAlias(String tableAlias) {
			return (Builder) super.tableAlias(tableAlias);
		}

		@Override
		public Builder columnAliasSeparator(String columnAliasSeparator) {
			return (Builder) super.columnAliasSeparator(columnAliasSeparator);
		}

		@Override
		public Builder parentRelationType(RelationType parentRelationType) {
			return (Builder) super.parentRelationType(parentRelationType);
		}

		@Override
		public Builder parentJoinType(JoinType parentJoinType) {
			return (Builder) super.parentJoinType(parentJoinType);
		}

		@Override
		public Builder parentInnerJoin() {
			return (Builder) super.parentInnerJoin();
		}
		

		@Override
		public Builder entityFactory(ObjectFactory<?> entityFactory) {
			return (Builder) super.entityFactory(entityFactory);
		}

		@Override
		public Builder targetMember(Member member) {
			return (Builder) super.targetMember(member);
		}

		@Override
		public Builder lobHandler(LobHandler lobHandler) {
			return (Builder) super.lobHandler(lobHandler);
		}
		
		@Override
		public Builder conversionService(ConversionService conversionService) {
			return (Builder) super.conversionService(conversionService);
		}

				
		// specific Builder methods
				
		public Builder manyToManyTable(String manyToManyTable) {
			this.manyToManyTable = manyToManyTable;
			return this;
		}
				
		public Builder manyToManyTableAlias(String manyToManyTableAlias) {
			this.manyToManyTableAlias = manyToManyTableAlias;
			return this;
		}

		public Builder manyToManyLeftFkName(String manyToManyLeftFkName) {
			this.manyToManyLeftFkName = manyToManyLeftFkName;
			return this;
		}

		public Builder manyToManyRightFkName(String manyToManyRightFkName) {
			this.manyToManyRightFkName = manyToManyRightFkName;
			return this;
		}
		
		/**
		 * @see ManyToManyQueryReady#getManyToManyLeftJoinConditionsOverride()
		 */
		public Builder manyToManyLeftJoinConditionsOverride(String leftJoinConditionsOverride) {
			return (Builder) super.joinConditionsOverride(leftJoinConditionsOverride);
		}

		/**
		 * @see ManyToManyQueryReady#getManyToManyLeftJoinConditionsOverrideParams()
		 */
		public Builder manyToManyLeftJoinConditionsOverrideParam(Object leftJoinConditionsOverrideParam) {
			return (Builder) super.joinConditionsOverrideParam(leftJoinConditionsOverrideParam);
		}

		/**
		 * @see ManyToManyQueryReady#getManyToManyRightJoinConditionsOverride()
		 */
		public Builder manyToManyRightJoinConditionsOverride(String rightJoinConditionsOverride) {
			this.manyToManyRightJoinConditionsOverride = rightJoinConditionsOverride;
			return this;
		}
		
		/**
		 * @see ManyToManyQueryReady#getManyToManyRightJoinConditionsOverrideParams()
		 */
		public Builder manyToManyRightJoinConditionsOverrideParam(Object rightJoinConditionsOverrideParam) {
			if (manyToManyRightJoinConditionsOverrideParams == null) {
				manyToManyRightJoinConditionsOverrideParams = new ArrayList<>();
			}
			manyToManyRightJoinConditionsOverrideParams.add(rightJoinConditionsOverrideParam);
			return this;
		}
		
		@Override
		public Builder preBuild() {
			return (Builder) super.preBuild();
		}

		/**
		 * @see super{@link #build()}
		 */
		@Override
		public ManyToManyEntityDescriptor build() {
			return new ManyToManyEntityDescriptor((Builder) preBuild());
		}
		
	
		
		// getters to inspect the current status of the properties
		
		public String getManyToManyTable() {
			return manyToManyTable;
		}

		public String getManyToManyTableAlias() {
			return manyToManyTableAlias;
		}

		public String getManyToManyLeftFkName() {
			return manyToManyLeftFkName;
		}

		public String getManyToManyRightFkName() {
			return manyToManyRightFkName;
		}
		
		public String getManyToManyLeftJoinConditionsOverride() {
			return super.getJoinConditionsOverride();
		}

		public List<Object> getManyToManyLeftJoinConditionsOverrideParams() {
			return super.getJoinConditionsOverrideParams();
		}

		public String getManyToManyRightJoinConditionsOverride() {
			return manyToManyRightJoinConditionsOverride;
		}

		public List<Object> getManyToManyRightJoinConditionsOverrideParams() {
			return Collections.unmodifiableList(manyToManyRightJoinConditionsOverrideParams);
		}
	}

}
