/*
 * Copyright 2017-2019 the original author or authors.
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
package org.springframework.data.orient.commons.repository.support;

import org.springframework.data.orient.commons.core.mapping.OrientPersistentEntity;
import org.springframework.data.orient.commons.repository.query.OrientEntityInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Support class responsible for creating {@link OrientEntityInformation} instances for a given
 * {@link OrientPersistentEntity}.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 1.10
 */
final class OrientEntityInformationSupport {

	private OrientEntityInformationSupport() {}

	/**
	 * Factory method for creating {@link OrientEntityInformation}.
	 *
	 * @param entity must not be {@literal null}.
	 * @param idType can be {@literal null}.
	 * @return never {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	static <T, ID> OrientEntityInformation<T, ID> entityInformationFor(OrientPersistentEntity<?> entity,
			@Nullable Class<?> idType) {

		Assert.notNull(entity, "Entity must not be null!");

		return new MappingOrientEntityInformation<>((OrientPersistentEntity<T>) entity, (Class<ID>) idType);
	}
}
