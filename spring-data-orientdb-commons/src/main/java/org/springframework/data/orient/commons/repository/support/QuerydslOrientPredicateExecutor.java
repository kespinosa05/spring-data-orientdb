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

import java.util.List;
import java.util.Optional;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.repository.query.OrientEntityInformation;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;

/**
 * OrientDB-specific {@link QuerydslPredicateExecutor} that allows execution {@link Predicate}s in various forms.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Mark Paluch
 * @since 2.0
 */
public class QuerydslOrientPredicateExecutor<T> implements QuerydslPredicateExecutor<T> {

	private final PathBuilder<T> builder;
	private final EntityInformation<T, ?> entityInformation;
	private final OrientOperations<T> orientOperations;

	/**
	 * Creates a new {@link QuerydslOrientPredicateExecutor} for the given {@link OrientEntityInformation} and
	 * {@link OrientOperations}. Uses the {@link SimpleEntityPathResolver} to create an {@link EntityPath} for the given
	 * domain class.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param OrientOperations must not be {@literal null}.
	 */
	public QuerydslOrientPredicateExecutor(OrientEntityInformation<T, ?> entityInformation,
			OrientOperations OrientOperations) {
		this(entityInformation, OrientOperations, SimpleEntityPathResolver.INSTANCE);
	}

	/**
	 * Creates a new {@link QuerydslOrientPredicateExecutor} for the given {@link OrientEntityInformation},
	 * {@link OrientOperations} and {@link EntityPathResolver}.
	 *
	 * @param entityInformation must not be {@literal null}.
	 * @param OrientOperations must not be {@literal null}.
	 * @param resolver must not be {@literal null}.
	 */
	public QuerydslOrientPredicateExecutor(OrientEntityInformation<T, ?> entityInformation, OrientOperations OrientOperations,
			EntityPathResolver resolver) {

		Assert.notNull(resolver, "EntityPathResolver must not be null!");

		EntityPath<T> path = resolver.createPath(entityInformation.getJavaType());

		this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
		this.entityInformation = entityInformation;
		this.orientOperations = OrientOperations;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findById(com.querydsl.core.types.Predicate)
	 */
	@Override
	public Optional<T> findOne(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		try {
			return Optional.ofNullable(createQueryFor(predicate).fetchOne());
		} catch (NonUniqueResultException ex) {
			throw new IncorrectResultSizeDataAccessException(ex.getMessage(), 1, ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findAll(com.querydsl.core.types.Predicate)
	 */
	@Override
	public List<T> findAll(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return createQueryFor(predicate).fetch();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, com.querydsl.core.types.OrderSpecifier<?>[])
	 */
	@Override
	public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(orders, "Order specifiers must not be null!");

		return createQueryFor(predicate).orderBy(orders).fetch();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, org.springframework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Predicate predicate, Sort sort) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(sort, "Sort must not be null!");

		return applySorting(createQueryFor(predicate), sort).fetch();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findAll(com.querydsl.core.types.OrderSpecifier[])
	 */
	@Override
	public Iterable<T> findAll(OrderSpecifier<?>... orders) {

		Assert.notNull(orders, "Order specifiers must not be null!");

		return createQuery().orderBy(orders).fetch();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#findAll(com.querydsl.core.types.Predicate, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

		Assert.notNull(predicate, "Predicate must not be null!");
		Assert.notNull(pageable, "Pageable must not be null!");

		SpringDataOrientQuery<T> query = createQueryFor(predicate);

		return PageableExecutionUtils.getPage(applyPagination(query, pageable).fetch(), pageable, query::fetchCount);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#count(com.querydsl.core.types.Predicate)
	 */
	@Override
	public long count(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return createQueryFor(predicate).fetchCount();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QuerydslPredicateExecutor#exists(com.querydsl.core.types.Predicate)
	 */
	@Override
	public boolean exists(Predicate predicate) {

		Assert.notNull(predicate, "Predicate must not be null!");

		return createQueryFor(predicate).fetchCount() > 0;
	}

	/**
	 * Creates a {@link SpringDataOrientQuery} for the given {@link Predicate}.
	 *
	 * @param predicate
	 * @return
	 */
	private SpringDataOrientQuery<T> createQueryFor(Predicate predicate) {
		return createQuery().where(predicate);
	}

	/**
	 * Creates a {@link SpringDataOrientQuery}.
	 *
	 * @return
	 */
	private SpringDataOrientQuery<T> createQuery() {
		return new SpringDataOrientQuery<>(orientOperations, entityInformation.getJavaType());
	}

	/**
	 * Applies the given {@link Pageable} to the given {@link SpringDataOrientQuery}.
	 *
	 * @param query
	 * @param pageable
	 * @return
	 */
	private SpringDataOrientQuery<T> applyPagination(SpringDataOrientQuery<T> query, Pageable pageable) {

		query = query.offset(pageable.getOffset()).limit(pageable.getPageSize());
		return applySorting(query, pageable.getSort());
	}

	/**
	 * Applies the given {@link Sort} to the given {@link SpringDataOrientQuery}.
	 *
	 * @param query
	 * @param sort
	 * @return
	 */
	private SpringDataOrientQuery<T> applySorting(SpringDataOrientQuery<T> query, Sort sort) {

		// TODO: find better solution than instanceof check
		if (sort instanceof QSort) {

			List<OrderSpecifier<?>> orderSpecifiers = ((QSort) sort).getOrderSpecifiers();
			query.orderBy(orderSpecifiers.toArray(new OrderSpecifier<?>[orderSpecifiers.size()]));

			return query;
		}

		sort.stream().map(this::toOrder).forEach(query::orderBy);

		return query;
	}

	/**
	 * Transforms a plain {@link Order} into a Querydsl specific {@link OrderSpecifier}.
	 *
	 * @param order
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private OrderSpecifier<?> toOrder(Order order) {

		Expression<Object> property = builder.get(order.getProperty());

		return new OrderSpecifier(
				order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC, property);
	}
}
