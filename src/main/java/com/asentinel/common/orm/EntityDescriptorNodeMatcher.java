package com.asentinel.common.orm;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.orm.ed.tree.AnnotationNodeMatcher;
import com.asentinel.common.util.Assert;

/**
 * {@link NodeMatcher} implementation for {@link EntityDescriptor} nodes that
 * behaves in the following ways:
 * <li>if only the type is specified on construction any
 * {@code EntityDescriptor} node that has that entity type will be matched.
 * <li>if a foreign key name is also specified any {@code EntityDescriptor} node
 * that is also a {@link QueryReady} that has the specified entity type and
 * foreign key name will be matched. For {@code EntityDescriptors} derived from
 * a {@code Child} annotation <b>the foreign key name must be EXPLICITLY</b>
 * specified in the annotation. You may prefer annotation matching
 * ({@code AnnotationNodeMatcher}) instead of type and fk name matching.
 * 
 * @see #match(Node)
 * @see AnnotationNodeMatcher
 * @see #EntityDescriptorNodeMatcher(Class)
 * @see #forTypeAndFkName(Class, String)
 * @see #forTypeAndEmptyFkName(Class)
 * 
 * @author Razvan Popian
 */
public class EntityDescriptorNodeMatcher implements NodeMatcher<EntityDescriptor> {
	
	private final Class<?> type;
	private final String tableAlias;
	private final String fkName;
	
	private final boolean strict;

	/**
	 * @see #EntityDescriptorNodeMatcher(Class, boolean)
	 */
	public EntityDescriptorNodeMatcher(Class<?> type) {
		this(type, null);
	}

	
	/**
	 * @deprecated table alias based matching is strongly discouraged. Use other {@code Child} properties
	 * 				when multiple children of an entity have the same type, like the foreign key (see {@link #forTypeAndFkName(Class, String)}).
	 * 				Alternatively you can create your own {@link NodeMatcher} implementation.
	 * 
	 * @see #forTypeAndFkName(Class, String)
	 * @see #forTypeAndEmptyFkName(Class)
	 */
	@Deprecated
	public EntityDescriptorNodeMatcher(Class<?> type, String tableAlias) {
		this(type, tableAlias, false);
	}

	/**
	 * Constructor.
	 * @param type the entity class to match against.
	 * @param strict whether to match the class exactly or check the class hierarchy.
	 * 
	 * @see #forTypeAndFkName(Class, String)
	 * @see #forTypeAndEmptyFkName(Class)
	 */
	public EntityDescriptorNodeMatcher(Class<?> type, boolean strict) {
		this(type, null, strict);
	}

	/**
	 * @deprecated table alias based matching is strongly discouraged. Use other {@code Child} properties
	 * 				when multiple children of an entity have the same type, like the foreign key (see {@link #forTypeAndFkName(Class, String, boolean)}).
	 * 				Alternatively you can create your own {@link NodeMatcher} implementation.
	 * 
	 * @see #forTypeAndFkName(Class, String)
	 * @see #forTypeAndEmptyFkName(Class)
	 */
	@Deprecated
	public EntityDescriptorNodeMatcher(Class<?> type, String tableAlias, boolean strict) {
		this(type, tableAlias, null, strict);
	}

	/**
	 * Constructor.
	 * @param type the entity class to match against.
	 * @param tableAlias the table alias used for matching. If <code>null</code>
	 * 			it is not used.
	 * @param fkName the foreign key name. Note that if FK name matching is used,
	 * 			the FK name value must be explicitly specified in the {@code QueryReady}
	 * 			implementation. In essence, this means that {@link QueryReady#getFkName()} should not return
	 * 			{@code null} for the target.
	 * @param strict whether to match the class exactly or check the class hierarchy.
	 * 
	 * @deprecated table alias based matching is strongly discouraged. Use other {@code Child} properties
	 * 				when multiple children of an entity have the same type, like the foreign key (see {@link #forTypeAndFkName(Class, String, boolean)}).
	 * 				Alternatively you can create your own {@link NodeMatcher} implementation.
	 * 
	 * @see #forTypeAndFkName(Class, String)
	 * @see #forTypeAndEmptyFkName(Class)
	 */
	@Deprecated
	public EntityDescriptorNodeMatcher(Class<?> type, String tableAlias, String fkName, boolean strict) {
		Assert.assertNotNull(type, "type");
		this.type = type;
		this.tableAlias = tableAlias;
		this.fkName = fkName;
		this.strict = strict;
	}
	
	
	/**
	 * Factory method, defaults {@code strict} to {@code false}. <bt>
	 * Note that if FK name matching is used,
	 * the FK name value must be explicitly specified in the {@code QueryReady}
	 * implementation. In essence, this means that {@link QueryReady#getFkName()} should not return
	 * {@code null} for the target.
	 * 
	 * @see EntityDescriptorNodeMatcher#EntityDescriptorNodeMatcher(Class)
	 */
	public static EntityDescriptorNodeMatcher forTypeAndFkName(Class<?> type, String fkName) {
		return new EntityDescriptorNodeMatcher(type, null, fkName, false);
	}

	/**
	 * Factory method. <br>
	 * Note that if FK name matching is used,
	 * the FK name value must be explicitly specified in the {@code QueryReady}
	 * implementation. In essence, this means that {@link QueryReady#getFkName()} should not return
	 * {@code null} for the target.
	 * 
	 * @see EntityDescriptorNodeMatcher#EntityDescriptorNodeMatcher(Class)
	 */
	public static EntityDescriptorNodeMatcher forTypeAndFkName(Class<?> type, String fkName, boolean strict) {
		return new EntityDescriptorNodeMatcher(type, null, fkName, strict);
	}


	/**
	 * Factory method. <br>
	 * Should be used when the fk name is {@code null} in the target (usually this
	 * occurs when no fk is specified in the {@code Child} annotation).
	 * 
	 * @see EntityDescriptorNodeMatcher#EntityDescriptorNodeMatcher(Class)
	 */
	public static NodeMatcher<EntityDescriptor> forTypeAndEmptyFkName(Class<?> type) {
		return forTypeAndEmptyFkName(type, false);
	}


	/**
	 * Factory method. <br>
	 * Should be used when the fk name is {@code null} in the target (usually this
	 * occurs when no fk is specified in the {@code Child} annotation).
	 * 
	 * @see EntityDescriptorNodeMatcher#EntityDescriptorNodeMatcher(Class)
	 */
	public static NodeMatcher<EntityDescriptor> forTypeAndEmptyFkName(Class<?> type, boolean strict) {
		return node -> {
			EntityDescriptor ed = node.getValue();
			if ( !testClass(ed, type, strict) ) {
				return false;
			}
			if (!(ed instanceof QueryReady)) {
				return false;
			}
			return !StringUtils.hasLength(((QueryReady) ed).getFkName()); 
		};
	}

	
	
	
	private static boolean testClass(EntityDescriptor ed, Class<?> type, boolean strict) {
		if (strict) {
			return type.equals(ed.getEntityClass());
		} else {
			return type.isAssignableFrom(ed.getEntityClass())
					|| ed.getEntityClass().isAssignableFrom(type);
		}
	}
	

	@Override
	public boolean match(Node<? extends EntityDescriptor> node) {
		EntityDescriptor ed = node.getValue();
		if ( !testClass(ed, type, strict) ) {
			return false;
		}

		// the tableAlias matching is deprecated
		if (tableAlias != null) { 
			if (!(ed instanceof QueryReady)) {
				return false;
			}
			return ObjectUtils.nullSafeEquals(tableAlias, ((QueryReady) ed).getTableAlias());
		}
		
		if (fkName != null) {
			if (!(ed instanceof QueryReady)) {
				return false;
			}
			return fkName.equalsIgnoreCase(((QueryReady) ed).getFkName());
		}
		
		return true;
	}

	
	Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return "EntityDescriptorNodeMatcher [type=" 
				+ type 
				+ ", tableAlias=" + tableAlias 
				+ ", fkName=" + fkName
				+ "]";
	}

}
