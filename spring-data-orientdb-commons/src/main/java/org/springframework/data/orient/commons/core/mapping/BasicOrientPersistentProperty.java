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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * OrientDB specific {@link org.springframework.data.mapping.PersistentProperty}
 * implementation.
 *
 * @author Oliver Gierke
 * @author Patryk Wasik
 * @author Thomas Darimont
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class BasicOrientPersistentProperty extends AnnotationBasedPersistentProperty<OrientPersistentProperty>
		implements OrientPersistentProperty {

	private static final Logger LOG = LoggerFactory.getLogger(BasicOrientPersistentProperty.class);

	private static final String ID_FIELD_NAME = "_id";
	private static final String LANGUAGE_FIELD_NAME = "language";
	private static final Set<Class<?>> SUPPORTED_ID_TYPES = new HashSet<Class<?>>();
	private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<String>();

	static {

		SUPPORTED_ID_TYPES.add(String.class);
		SUPPORTED_ID_TYPES.add(BigInteger.class);

		SUPPORTED_ID_PROPERTY_NAMES.add("id");
		SUPPORTED_ID_PROPERTY_NAMES.add("_id");
	}

	private final FieldNamingStrategy fieldNamingStrategy;

	/**
	 * Creates a new {@link BasicOrientPersistentProperty}.
	 *
	 * @param property
	 * @param owner
	 * @param simpleTypeHolder
	 * @param fieldNamingStrategy
	 */
	public BasicOrientPersistentProperty(Property property, OrientPersistentEntity<?> owner,
			SimpleTypeHolder simpleTypeHolder, @Nullable FieldNamingStrategy fieldNamingStrategy) {

		super(property, owner, simpleTypeHolder);
		this.fieldNamingStrategy = fieldNamingStrategy == null ? PropertyNameFieldNamingStrategy.INSTANCE
				: fieldNamingStrategy;

		if (isIdProperty() && getFieldName() != ID_FIELD_NAME) {
			LOG.warn("Customizing field name for id property not allowed! Custom name will not be considered!");
		}
	}

	/**
	 * Also considers fields as id that are of supported id type and name.
	 *
	 * @see #SUPPORTED_ID_PROPERTY_NAMES
	 * @see #SUPPORTED_ID_TYPES
	 */
	@Override
	public boolean isIdProperty() {

		if (super.isIdProperty()) {
			return true;
		}

		// We need to support a wider range of ID types than just the ones that can be
		// converted to an ObjectId
		// but still we need to check if there happens to be an explicit name set
		return SUPPORTED_ID_PROPERTY_NAMES.contains(getName()) && !hasExplicitFieldName();
	}

	/**
	 * Returns the key to be used to store the value of the property inside a Orient
	 * {@link org.bson.Document}.
	 *
	 * @return
	 */
	public String getFieldName() {

		if (isIdProperty()) {

			if (getOwner().getIdProperty() == null) {
				return ID_FIELD_NAME;
			}

			if (getOwner().isIdProperty(this)) {
				return ID_FIELD_NAME;
			}
		}

		if (hasExplicitFieldName()) {
			return getAnnotatedFieldName();
		}

		String fieldName = fieldNamingStrategy.getFieldName(this);

		if (!StringUtils.hasText(fieldName)) {
			throw new MappingException(
					String.format("Invalid (null or empty) field name returned for property %s by %s!", this,
							fieldNamingStrategy.getClass()));
		}

		return fieldName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.core.mapping.MongoPersistentProperty#
	 * getFieldOrder()
	 */
	public int getFieldOrder() {
		org.springframework.data.orient.commons.core.mapping.Field annotation = findAnnotation(
				org.springframework.data.orient.commons.core.mapping.Field.class);

		return annotation != null ? annotation.order() : Integer.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.core.mapping.MongoPersistentProperty#
	 * isExplicitIdProperty()
	 */
	@Override
	public boolean isExplicitIdProperty() {
		return isAnnotationPresent(Id.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.Orientdb.core.mapping.OrientPersistentProperty#
	 * getFieldType()
	 */
	@Override
	public Class<?> getFieldType() {

		if (!isIdProperty()) {
			return getType();
		}

		Id idAnnotation = findAnnotation(Id.class);

		if (idAnnotation == null) {
			return String.class;
		}

		return idAnnotation.annotationType();
	}

	/**
	 * @return true if {@link org.springframework.data.Orientdb.core.mapping.Field}
	 *         having non blank
	 *         {@link org.springframework.data.Orientdb.core.mapping.Field#value()}
	 *         present.
	 * @since 1.7
	 */
	protected boolean hasExplicitFieldName() {
		return StringUtils.hasText(getAnnotatedFieldName());
	}

	@Nullable
	private String getAnnotatedFieldName() {

		Column annotation = findAnnotation(Column.class);

		return annotation != null ? annotation.toString() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mapping.model.AbstractPersistentProperty#
	 * createAssociation()
	 */
	@Override
	protected Association<OrientPersistentProperty> createAssociation() {
		return new Association<OrientPersistentProperty>(this, null);
	}

}
