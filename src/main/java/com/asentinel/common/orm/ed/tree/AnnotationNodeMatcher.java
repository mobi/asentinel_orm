package com.asentinel.common.orm.ed.tree;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.asentinel.common.collections.tree.Node;
import com.asentinel.common.collections.tree.TreeUtils.NodeMatcher;
import com.asentinel.common.orm.EntityDescriptor;
import com.asentinel.common.orm.EntityDescriptorNodeMatcher;
import com.asentinel.common.util.Assert;

/**
 * {@code NodeMatcher} implementation that looks for one of the annotations
 * received by the constructor on the target member (field or method) in the
 * entity descriptor. If it finds at least one of the annotations it considers
 * the target node and its entity descriptor a match.
 * 
 * @see EntityDescriptor
 * @see NodeMatcher
 * @see EntityDescriptorNodeMatcher
 * 
 * @author Razvan.Popian
 */
public class AnnotationNodeMatcher implements NodeMatcher<EntityDescriptor> {
	private static final Logger log = LoggerFactory.getLogger(AnnotationNodeMatcher.class);
	
	private final Class<? extends Annotation>[] annotationTypes;

	@SuppressWarnings("unchecked")
	public AnnotationNodeMatcher(Class<? extends Annotation> annotationType) {
		this(new Class[] {annotationType});
	}
	
	@SafeVarargs
	public AnnotationNodeMatcher(Class<? extends Annotation> ... annotationTypes) {
		Assert.assertNotNull(annotationTypes, "annotationTypes");
		if (annotationTypes.length == 0) {
			throw new IllegalArgumentException("At least one annotation type must be specified.");
		}
		this.annotationTypes = annotationTypes;
	}

	@Override
	public boolean match(Node<? extends EntityDescriptor> node) {
		EntityDescriptor ed = node.getValue();	
		// Field or Method
		AnnotatedElement member = (AnnotatedElement) ed.getTargetMember();
		if (member == null) {
			return false;
		}
		for (Class<? extends Annotation> annotationType: annotationTypes) {
			if (annotationType == null) {
				log.error("match - Found a null element in the annotation types array. This will be ignored.");
				continue;
			}
			if (AnnotationUtils.getAnnotation(member, annotationType) != null) {
				if (log.isTraceEnabled()) {
					log.trace("match - Found annotation " + annotationType + " on member " + member);
				}
				return true;
			}		
		}
		return false;
	}
	
	public Class<? extends Annotation>[] getAnnotationTypes() {
		return annotationTypes;
	}

	@Override
	public String toString() {
		return "AnnotationNodeMatcher [annotationTypes=" + Arrays.toString(annotationTypes) + "]";
	}

	
}
