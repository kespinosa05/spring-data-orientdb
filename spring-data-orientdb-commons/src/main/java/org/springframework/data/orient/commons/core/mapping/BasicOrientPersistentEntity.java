/*
 * Copyright 2011-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.orient.commons.core.mapping;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.AssociationHandler;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * OrientDB specific {@link OrientPersistentEntity} implementation that adds Orient specific meta-data such as the
 * collection name and the like.
 *
 * @author Jon Brisbin
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class BasicOrientPersistentEntity<T> extends BasicPersistentEntity<T, OrientPersistentProperty>
		implements OrientPersistentEntity<T> {

	private static final String AMBIGUOUS_FIELD_MAPPING = "Ambiguous field mapping detected! Both %s and %s map to the same field name %s! Disambiguate using @Field annotation!";
	private static final SpelExpressionParser PARSER = new SpelExpressionParser();

	private final String collection;
	private final String language;

	private final @Nullable Expression expression;

	/**
	 * Creates a new {@link BasicOrientPersistentEntity} with the given {@link TypeInformation}. Will default the
	 * collection name to the entities simple type name.
	 *
	 * @param typeInformation must not be {@literal null}.
	 */
	public BasicOrientPersistentEntity(TypeInformation<T> typeInformation) {

		super(typeInformation, OrientPersistentPropertyComparator.INSTANCE);

		Class<?> rawType = typeInformation.getType();
		String fallback = StringUtils.uncapitalize(rawType.getSimpleName());

		if (this.isAnnotationPresent(Document.class)) {
			Document document = this.getRequiredAnnotation(Document.class);

			this.collection = StringUtils.hasText(document.collection()) ? document.collection() : fallback;
			this.language = StringUtils.hasText(document.language()) ? document.language() : "";
			this.expression = detectExpression(document);
		} else {

			this.collection = fallback;
			this.language = "";
			this.expression = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.Orientdb.core.mapping.OrientPersistentEntity#getCollection()
	 */
	public String getCollection() {

		return expression == null //
				? collection //
				: expression.getValue(getEvaluationContext(null), String.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.Orientdb.core.mapping.OrientPersistentEntity#getLanguage()
	 */
	@Override
	public String getLanguage() {
		return this.language;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.Orientdb.core.mapping.OrientPersistentEntity#getTextScoreProperty()
	 */
	@Nullable
	@Override
	public OrientPersistentProperty getTextScoreProperty() {
		return getPersistentProperty(TextScore.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.Orientdb.core.mapping.OrientPersistentEntity#hasTextScoreProperty()
	 */
	@Override
	public boolean hasTextScoreProperty() {
		return getTextScoreProperty() != null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.BasicPersistentEntity#verify()
	 */
	@Override
	public void verify() {

		super.verify();

		verifyFieldUniqueness();
		verifyFieldTypes();
	}

	private void verifyFieldUniqueness() {

		AssertFieldNameUniquenessHandler handler = new AssertFieldNameUniquenessHandler();

		doWithProperties(handler);
		doWithAssociations(handler);
	}

	private void verifyFieldTypes() {
		doWithProperties(new PropertyTypeAssertionHandler());
	}

	/**
	 * {@link Comparator} implementation inspecting the {@link OrientPersistentProperty}'s order.
	 *
	 * @author Oliver Gierke
	 */
	enum OrientPersistentPropertyComparator implements Comparator<OrientPersistentProperty> {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(@Nullable OrientPersistentProperty o1, @Nullable OrientPersistentProperty o2) {

			if (o1 != null && o1.getFieldOrder() == Integer.MAX_VALUE) {
				return 1;
			}

			if (o2 != null && o2.getFieldOrder() == Integer.MAX_VALUE) {
				return -1;
			}

			if (o1 == null && o2 == null) {
				return -1;
			}

			return o1.getFieldOrder() - o2.getFieldOrder();
		}
	}

	/**
	 * As a general note: An implicit id property has a name that matches "id" or "_id". An explicit id property is one
	 * that is annotated with @see {@link Id}. The property id is updated according to the following rules: 1) An id
	 * property which is defined explicitly takes precedence over an implicitly defined id property. 2) In case of any
	 * ambiguity a @see {@link MappingException} is thrown.
	 *
	 * @param property - the new id property candidate
	 * @return
	 */
	@Override
	protected OrientPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(OrientPersistentProperty property) {

		Assert.notNull(property, "OrientPersistentProperty must not be null!");

		if (!property.isIdProperty()) {
			return null;
		}

		OrientPersistentProperty currentIdProperty = getIdProperty();

		boolean currentIdPropertyIsSet = currentIdProperty != null;
		@SuppressWarnings("null")
		boolean currentIdPropertyIsExplicit = currentIdPropertyIsSet ? currentIdProperty.isExplicitIdProperty() : false;
		boolean newIdPropertyIsExplicit = property.isExplicitIdProperty();

		if (!currentIdPropertyIsSet) {
			return property;

		}

		@SuppressWarnings("null")
		Field currentIdPropertyField = currentIdProperty.getField();

		if (newIdPropertyIsExplicit && currentIdPropertyIsExplicit) {
			throw new MappingException(
					String.format("Attempt to add explicit id property %s but already have an property %s registered "
							+ "as explicit id. Check your mapping configuration!", property.getField(), currentIdPropertyField));

		} else if (newIdPropertyIsExplicit && !currentIdPropertyIsExplicit) {
			// explicit id property takes precedence over implicit id property
			return property;

		} else if (!newIdPropertyIsExplicit && currentIdPropertyIsExplicit) {
			// no id property override - current property is explicitly defined

		} else {
			throw new MappingException(
					String.format("Attempt to add id property %s but already have an property %s registered "
							+ "as id. Check your mapping configuration!", property.getField(), currentIdPropertyField));
		}

		return null;
	}

	/**
	 * Returns a SpEL {@link Expression} frór the collection String expressed in the given {@link Document} annotation if
	 * present or {@literal null} otherwise. Will also return {@literal null} it the collection {@link String} evaluates
	 * to a {@link LiteralExpression} (indicating that no subsequent evaluation is necessary).
	 *
	 * @param document can be {@literal null}
	 * @return
	 */
	@Nullable
	private static Expression detectExpression(Document document) {

		String collection = document.collection();

		if (!StringUtils.hasText(collection)) {
			return null;
		}

		Expression expression = PARSER.parseExpression(document.collection(), ParserContext.TEMPLATE_EXPRESSION);

		return expression instanceof LiteralExpression ? null : expression;
	}

	/**
	 * Handler to collect {@link OrientPersistentProperty} instances and check that each of them is mapped to a distinct
	 * field name.
	 *
	 * @author Oliver Gierke
	 */
	private static class AssertFieldNameUniquenessHandler
			implements PropertyHandler<OrientPersistentProperty>, AssociationHandler<OrientPersistentProperty> {

		private final Map<String, OrientPersistentProperty> properties = new HashMap<String, OrientPersistentProperty>();

		public void doWithPersistentProperty(OrientPersistentProperty persistentProperty) {
			assertUniqueness(persistentProperty);
		}

		public void doWithAssociation(Association<OrientPersistentProperty> association) {
			assertUniqueness(association.getInverse());
		}

		private void assertUniqueness(OrientPersistentProperty property) {

			String fieldName = property.getFieldName();
			OrientPersistentProperty existingProperty = properties.get(fieldName);

			if (existingProperty != null) {
				throw new MappingException(
						String.format(AMBIGUOUS_FIELD_MAPPING, property.toString(), existingProperty.toString(), fieldName));
			}

			properties.put(fieldName, property);
		}
	}

	/**
	 * @author Christoph Strobl
	 * @since 1.6
	 */
	private static class PropertyTypeAssertionHandler implements PropertyHandler<OrientPersistentProperty> {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.mapping.PropertyHandler#doWithPersistentProperty(org.springframework.data.mapping.PersistentProperty)
		 */
		@Override
		public void doWithPersistentProperty(OrientPersistentProperty persistentProperty) {

			//potentiallyAssertTextScoreType(persistentProperty);
			//potentiallyAssertLanguageType(persistentProperty);
		}

		

		private static void assertPropertyType(OrientPersistentProperty persistentProperty, Class<?>... validMatches) {

			for (Class<?> potentialMatch : validMatches) {
				if (ClassUtils.isAssignable(potentialMatch, persistentProperty.getActualType())) {
					return;
				}
			}

			throw new MappingException(
					String.format("Missmatching types for %s. Found %s expected one of %s.", persistentProperty.getField(),
							persistentProperty.getActualType(), StringUtils.arrayToCommaDelimitedString(validMatches)));
		}
	}
}
