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

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.PersistentProperty;

/**
 * Orient specific {@link org.springframework.data.mapping.PersistentProperty} extension.
 *
 * @author Oliver Gierke
 * @author Patryk Wasik
 * @author Thomas Darimont
 * @author Christoph Strobl
 */
public interface OrientPersistentProperty extends PersistentProperty<OrientPersistentProperty> {

	/**
	 * Returns the name of the field a property is persisted to.
	 *
	 * @return
	 */
	String getFieldName();

	/**
	 * Returns the {@link Class Java FieldType} of the field a property is persisted to.
	 *
	 * @return
	 * @since 2.2
	 * @see FieldType
	 */
	Class<?> getFieldType();


	/**
	 * Simple {@link Converter} implementation to transform a {@link OrientPersistentProperty} into its field name.
	 *
	 * @author Oliver Gierke
	 */
	public enum PropertyToFieldNameConverter implements Converter<OrientPersistentProperty, String> {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
		 */
		public String convert(OrientPersistentProperty source) {
			return source.getFieldName();
		}
	}

	/**
	 * Returns whether property access shall be used for reading the property value. This means it will use the getter
	 * instead of field access.
	 *
	 * @return
	 */
	boolean usePropertyAccess();

	/**
	 * Returns the order of the field if defined. Will return -1 if undefined.
	 *
	 * @return
	 */
	int getFieldOrder();
	
	/**
	 * Returns whether the property is explicitly marked as an identifier property of the owning {@link PersistentEntity}.
	 * A property is an explicit id property if it is annotated with @see {@link Id}.
	 *
	 * @return
	 */
	boolean isExplicitIdProperty();
}
