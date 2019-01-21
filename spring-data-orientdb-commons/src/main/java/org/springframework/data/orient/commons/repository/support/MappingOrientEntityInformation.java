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
package org.springframework.data.orient.commons.repository.support;

import org.springframework.data.orient.commons.core.mapping.OrientPersistentEntity;
import org.springframework.data.orient.commons.repository.query.OrientEntityInformation;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.lang.Nullable;

import com.sun.corba.se.spi.ior.ObjectId;

/**
 * {@link OrientEntityInformation} implementation using a {@link OrientPersistentEntity} instance to lookup the necessary
 * information. Can be configured with a custom collection to be returned which will trump the one returned by the
 * {@link OrientPersistentEntity} if given.
 *
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class MappingOrientEntityInformation<T, ID> extends PersistentEntityInformation<T, ID>
		implements OrientEntityInformation<T, ID> {

	private final OrientPersistentEntity<T> entityMetadata;
	private final @Nullable String customCollectionName;
	private final Class<ID> fallbackIdType;

	/**
	 * Creates a new {@link MappingOrientEntityInformation} for the given {@link OrientPersistentEntity}.
	 *
	 * @param entity must not be {@literal null}.
	 */
	public MappingOrientEntityInformation(OrientPersistentEntity<T> entity) {
		this(entity, null, null);
	}

	/**
	 * Creates a new {@link MappingOrientEntityInformation} for the given {@link OrientPersistentEntity} and fallback
	 * identifier type.
	 *
	 * @param entity must not be {@literal null}.
	 * @param fallbackIdType can be {@literal null}.
	 */
	public MappingOrientEntityInformation(OrientPersistentEntity<T> entity, @Nullable Class<ID> fallbackIdType) {
		this(entity, null, fallbackIdType);
	}

	/**
	 * Creates a new {@link MappingOrientEntityInformation} for the given {@link OrientPersistentEntity} and custom
	 * collection name.
	 *
	 * @param entity must not be {@literal null}.
	 * @param customCollectionName can be {@literal null}.
	 */
	public MappingOrientEntityInformation(OrientPersistentEntity<T> entity, String customCollectionName) {
		this(entity, customCollectionName, null);
	}

	/**
	 * Creates a new {@link MappingOrientEntityInformation} for the given {@link OrientPersistentEntity}, collection name
	 * and identifier type.
	 *
	 * @param entity must not be {@literal null}.
	 * @param customCollectionName can be {@literal null}.
	 * @param idType can be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	private MappingOrientEntityInformation(OrientPersistentEntity<T> entity, @Nullable String customCollectionName,
			@Nullable Class<ID> idType) {

		super(entity);

		this.entityMetadata = entity;
		this.customCollectionName = customCollectionName;
		this.fallbackIdType = idType != null ? idType : (Class<ID>) ObjectId.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.Orientdb.repository.OrientEntityInformation#getCollectionName()
	 */
	public String getCollectionName() {
		return customCollectionName == null ? entityMetadata.getCollection() : customCollectionName;
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.Orientdb.repository.OrientEntityInformation#getIdAttribute()
	 */
	public String getIdAttribute() {
		return entityMetadata.getRequiredIdProperty().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.core.support.PersistentEntityInformation#getIdType()
	 */
	@Override
	public Class<ID> getIdType() {

		if (this.entityMetadata.hasIdProperty()) {
			return super.getIdType();
		}

		return fallbackIdType;
	}
}
