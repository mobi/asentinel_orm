package com.asentinel.common.orm.mappers;

import static com.asentinel.common.orm.mappers.SqlParameterTypeDescriptor.isCustomConversion;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.asentinel.common.jdbc.AbstractReflectionRowMapper;
import com.asentinel.common.jdbc.ColumnMetadata;
import com.asentinel.common.jdbc.ObjectFactory;
import com.asentinel.common.jdbc.TypedObjectFactory;
import com.asentinel.common.orm.EntityBuilder;
import com.asentinel.common.orm.SimpleEntityDescriptor;
import com.asentinel.common.orm.TargetMember;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;

// TODO: add support for creating immutable entities (all fields final).
// See the TODO in the ObjectFactory interface for some ideas

/**
 * <code>RowMapper</code> implementation that converts resultset rows based
 * on <code>PkColumn</code> and <code>Column</code> annotations found in the target
 * object class. A prefix for the columns can be specified on construction, in this
 * case the mapper will add this prefix in front of each annotated column name before
 * attempting to extract the column from the resultset.<br>
 * The client can choose to ignore the primary key annotated columns, ie. the mapper will not
 * set any value for the primary key fields. This is to accomodate the {@code EntityBuilder} entity
 * creation logic.
 * <br><br>
 * 
 * <b>Important:</b><br>
 * 
 * If an annotated setter method is overridden in a subclass, any <code>Column</code> or <code>PkColumn</code>
 * annotation on the method in the super class is inherited unless the subclass method is also annotated with 
 * <code>Column</code> or <code>PkColumn</code>.
 * <br> 
 * This is a feature, it allows for example to override the <code>Column</code> or <code>PkColumn</code> 
 * annotation attributes in a subclass.
 * 
 * <br><br>
 * Instances of this class should be used as effectively immutable objects.
 * 
 * 
 * @see PkColumn
 * @see Column
 * @see TargetMembersHolder
 * @see TargetMembers
 * @see SimpleEntityDescriptor
 * @see EntityBuilder
 * 
 * @author Razvan Popian
 */
public class AnnotationRowMapper<T> extends AbstractReflectionRowMapper<T> {
	private final static Logger log = LoggerFactory.getLogger(AnnotationRowMapper.class);
	
	private final String columnPrefix;
	
	private final Map<ColumnMetadata, TargetMember> elementsMap = new HashMap<>();

	/**
	 * Constructor, takes the target object class as parameter.
	 * @param clasz the target object class.
	 */
	public AnnotationRowMapper(Class<T> clasz) {
		this(clasz, false);
	}
	
	
	/**
	 * Constructor, takes the target object class as parameter and a boolean
	 * flag that determines if the entities primary key member will be mapped.
	 * @param clasz the target object class.
	 * @param ignorePkColumn {@code true} if this mapper will ignore the
	 * 			<code>PkColumn</annotation>, {@code false}
	 * 			otherwise.
	 */
	public AnnotationRowMapper(Class<T> clasz, boolean ignorePkColumn) {
		this(clasz, ignorePkColumn, null);
	}
	
	/**
	 * Constructor.
	 * @param clasz the target object class.
	 * @param columnPrefix the prefix expected in front of each column name in the resultset.
	 */
	public AnnotationRowMapper(Class<T> clasz, String columnPrefix) {
		this(clasz, false, columnPrefix);
	}
	
	
	/**
	 * Constructor.
	 * @param clasz the target object class.
	 * @param ignorePkColumn {@code true} if this mapper will ignore the
	 * 			<code>PkColumn</annotation>, {@code false}
	 * 			otherwise.
	 * @param columnPrefix the prefix expected in front of each column name in the resultset.
	 */
	public AnnotationRowMapper(Class<T> clasz, boolean ignorePkColumn, String columnPrefix) {
		super(clasz);
		this.columnPrefix = columnPrefix;
		initialize(clasz, ignorePkColumn);
	}

	/**
	 * Constructor, each target object will be created by a 
	 * {@link ObjectFactory} implementation.
	 * @param objectFactory the object factory.
	 */
	public AnnotationRowMapper(ObjectFactory<T> objectFactory) {
		this(objectFactory, false);
	}

	/**
	 * Constructor, each target object will be created by a 
	 * {@link ObjectFactory} implementation.
	 * @param objectFactory the object factory.
	 * @param ignorePkColumn {@code true} if this mapper will ignore the
	 * 			<code>PkColumn</annotation>, {@code false}
	 * 			otherwise.
	 */
	public AnnotationRowMapper(ObjectFactory<T> objectFactory, boolean ignorePkColumn) {
		this(objectFactory, ignorePkColumn, null);
	}

	/**
	 * Constructor, each target object will be created by a 
	 * {@link ObjectFactory} implementation.
	 * @param objectFactory the object factory.
	 * @param columnPrefix the prefix expected in front of each column name in the resultset.
	 */
	public AnnotationRowMapper(ObjectFactory<T> objectFactory, String columnPrefix) {
		this(objectFactory, false, columnPrefix);
	}
	

	/**
	 * Constructor, each target object will be created by a 
	 * {@link ObjectFactory} implementation.
	 * @param objectFactory the object factory.
	 * @param ignorePkColumn {@code true} if this mapper will ignore the
	 * 			<code>PkColumn</annotation>, {@code false}
	 * 			otherwise.
	 * @param columnPrefix the prefix expected in front of each column name in the resultset.
	 */
	@SuppressWarnings("unchecked")
	public AnnotationRowMapper(ObjectFactory<T> objectFactory, boolean ignorePkColumn, String columnPrefix) {
		super(objectFactory);
		this.columnPrefix = columnPrefix;
		
		Class<T> type;
		if (objectFactory instanceof TypedObjectFactory) {
			type = ((TypedObjectFactory<T>) objectFactory).getType();
		} else {
			type = (Class<T>) objectFactory.newObject().getClass();
		}
		initialize(type, ignorePkColumn);
	}
	
	private void initialize(Class<T> clasz, boolean ignorePkColumn) {
		List<TargetMember> targetMembers;
		if (ignorePkColumn) {
			targetMembers = TargetMembersHolder.getInstance().getTargetMembers(clasz).getColumnMembers();
		} else {
			targetMembers = TargetMembersHolder.getInstance().getTargetMembers(clasz).getAllColumnMembers();			
		}
		for (TargetMember targetMember: targetMembers) {
			Annotation ann = targetMember.getAnnotation();
			String columnName = null;
			boolean allowNull = false;
			if (ann instanceof Column) {
				columnName = ((Column) ann).value();
				allowNull = ((Column) ann).allowNull();
			} else if (ann instanceof PkColumn) {
				columnName = ((PkColumn) ann).value();
			}
			if (columnName == null) {
				continue;
			}

			if (elementsMap.putIfAbsent(new ColumnMetadata(columnPrefix, columnName, allowNull), targetMember) != null) {
				log.warn("initialize - Column '" + columnName + "' is already assigned to a field or method. "
						+ "This means that there are multiple column annotations for this name.");
			}
		}
	}
	
	@Override
	protected void populateTarget(T object, ResultSet rs) throws SQLException {
		for (Entry<ColumnMetadata, TargetMember> entry: elementsMap.entrySet()) {
			AnnotatedElement element = entry.getValue().getAnnotatedElement();
			if (element instanceof Field) {
				Field field = (Field) element; 
				Object value = getValue(object, entry.getValue().getTypeDescriptor(), rs, entry.getKey());
				setValue(object, field, value);
			} else if (element instanceof Method) {
				Method method = (Method) element; 
				Object value = getValue(object, entry.getValue().getTypeDescriptor(), rs, entry.getKey());
				setValue(object, method, value);
			} else {
				throw new IllegalStateException("Expected Field or Method. Found " + element.getClass().getName() + ".");
			}
		}
	}
	
	@Override
	protected Object getValueInternal(Object parentObject, TypeDescriptor targetDescriptor, ResultSet rs, ColumnMetadata columnMetadata) throws SQLException {
		Column column = targetDescriptor.getAnnotation(Column.class);
		if (column == null) {
			return super.getValueInternal(parentObject, targetDescriptor, rs, columnMetadata);
		}
		
		ConversionService conversionService = getConversionService();
		if (conversionService != null
				&& isCustomConversion(column)) {
			// we are dealing with a custom type, we call the conversion service
			return customConvert(targetDescriptor, rs, columnMetadata.getResultsetName());
		}
		
		// let the super class code attempt to convert, but it will likely fail
		return super.getValueInternal(parentObject, targetDescriptor, rs, columnMetadata);
	}
	
	/**
	 * @return the column prefix, if not null
	 * 			this will be added in front of
	 * 			each annotated column name.
	 */
	public String getColumnPrefix() {
		return columnPrefix;
	}
	
	@Override
	public String toString() {
		return "AnnotationRowMapper [objectFactory=" + objectFactory + "]";
	}

}
