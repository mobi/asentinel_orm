package com.asentinel.common.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.asentinel.common.orm.mappers.Child;
import com.asentinel.common.orm.mappers.Column;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;
import com.asentinel.common.util.Assert;

/**
 * Class that stores information about members annotated with any of the following annotations:
 * <li> {@link PkColumn}
 * <li> {@link Column}
 * <li> {@link Child}
 * <br><br>
 * Instances MUST be used as effectively immutable objects.
 * <br>
 * <br>
 * 
 * <b>Important:</b><br>
 * 
 * If an annotated setter method is overridden in a subclass, the method 
 * in the super class annotations are inherited.
 * <br> 
 * This is a feature, it allows for example to override the {@link Child} annotation attributes in a subclass.
 * 
 * @see TargetMembers
 * 
 * @author Razvan Popian
 */
public class TargetMembers {
	private final static Logger log = LoggerFactory.getLogger(TargetMembers.class);
	
	private Table tableAnnotation;
	private TableAnnotationInfo firstNonViewTable;
	private TargetMember pkColumnMember;
	private final List<TargetMember> columnMembers = new ArrayList<>(20);
	private final List<TargetChildMember> childMembers = new ArrayList<>(10);
	
	public Table getTableAnnotation() {
		return tableAnnotation;
	}

	public TargetMember getPkColumnMember() {
		return pkColumnMember;
	}
	
	public List<TargetMember> getColumnMembers() {
		return Collections.unmodifiableList(columnMembers);
	}

	public List<TargetMember> getAllColumnMembers() {
		if (pkColumnMember == null) {
			return getColumnMembers();
		}
		List<TargetMember> list = new ArrayList<TargetMember>(columnMembers.size() + 1);
		list.add(pkColumnMember);
		list.addAll(getColumnMembers());
		return list;
	}

	public List<TargetMember> getInsertableColumnMembers() {
		return getWritableColumnMembers(tm -> tm.getColumnAnnotation().insertable());
	}

	
	public List<TargetMember> getUpdatableColumnMembers() {
		return getWritableColumnMembers(tm -> tm.getColumnAnnotation().updatable());
	}
	
	
	private List<TargetMember> getWritableColumnMembers(Predicate<TargetMember> predicateForColumn) {
		// TODO 1: should we cache the updatable members, precalculate them  ?
		// TODO 2: This method has more or less the same logic as the
		// method SimpleEntityDescriptor getColNamesCollection. We should have only one place for column
		// lists calculations.
		if (firstNonViewTable == null) {
			return Collections.emptyList();
		}
		// @Column fields have priority over the @Child fields for foreign keys
		Set<String> checkSet = new HashSet<String>();
		List<TargetMember> list = new ArrayList<TargetMember>(columnMembers.size() + 5);
		for (TargetMember columnMember: columnMembers) {
			if (columnMember.getMemberDeclaringClass()
					.isAssignableFrom(firstNonViewTable.getTargetClass())
					&& predicateForColumn.test(columnMember)) {
				String name = ((Column) columnMember.getAnnotation()).value().toLowerCase();
				if (checkSet.add(name)) {
					list.add(columnMember);					
				} else {
					if (log.isDebugEnabled()) {
						log.debug("getUpdatableColumnMembers - Column '" + name  + "' already mapped to another field. "
								+ "Only the first field will be considered for update.");
					}
				}
			}
		}
		for (TargetChildMember childMember: childMembers) {
			Child childAnn = (Child) childMember.getAnnotation();
			if (childAnn.parentRelationType() == RelationType.ONE_TO_MANY) {
				if (!childAnn.parentAvailableFk()) {
					continue;
				}
				String fkName = childMember.getFkNameForOneToMany();
				if (StringUtils.hasText(fkName)) {
					if (checkSet.add(fkName.toLowerCase())) {
						list.add(childMember);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("getUpdatableColumnMembers - Column '" + fkName  + "' already mapped to another field. "
									+ "Only the first field will be considered for update.");
						}
					}
				}
			}
		}
		return list;
	}
	
	public String getUpdatableTable() {
		if (firstNonViewTable == null) {
			throw new IllegalStateException("No updatable table was detected.");
		}
		return firstNonViewTable.getTableAnn().value();
	}	
	
	public List<TargetChildMember> getChildMembers() {
		return Collections.unmodifiableList(childMembers);
	}
	

	// package private mutators
	
	void setTableAnnotation(Table tableAnnotation) {
		this.tableAnnotation = tableAnnotation;
	}
	
	void setFirstNonViewTable(TableAnnotationInfo firstNonViewTable) {
		this.firstNonViewTable = firstNonViewTable;
	}
	
	void setPkColumnMember(TargetMember pkColumnMember) {
		this.pkColumnMember = pkColumnMember;
	}
	
	void addColumnMember(TargetMember member) {
		Assert.assertNotNull(member, "member");
		columnMembers.add(member);
	}
	
	void addChildMember(TargetChildMember member) {
		Assert.assertNotNull(member, "member");
		childMembers.add(member);
	}
	
}
