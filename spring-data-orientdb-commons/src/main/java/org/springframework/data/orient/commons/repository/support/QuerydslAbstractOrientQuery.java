package org.springframework.data.orient.commons.repository.support;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.SimpleQuery;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Predicate;

public abstract class QuerydslAbstractOrientQuery<K, Q extends QuerydslAbstractOrientQuery<K, Q>>
		implements SimpleQuery<Q> {
	
	private final QueryMixin<Q> queryMixin;

	public QuerydslAbstractOrientQuery() {
		super();
		this.queryMixin = new QueryMixin<>((Q) this, new DefaultQueryMetadata(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#distinct()
	 */
	@Override
	public Q distinct() {
		return queryMixin.distinct();
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.FilteredClause#where(com.querydsl.core.types.Predicate[])
	 */
	@Override
	public Q where(Predicate... e) {
		return queryMixin.where(e);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#limit(long)
	 */
	@Override
	public Q limit(long limit) {
		return queryMixin.limit(limit);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#offset()
	 */
	@Override
	public Q offset(long offset) {
		return queryMixin.offset(offset);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#restrict(com.querydsl.core.QueryModifiers)
	 */
	@Override
	public Q restrict(QueryModifiers modifiers) {
		return queryMixin.restrict(modifiers);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#orderBy(com.querydsl.core.types.OrderSpecifier)
	 */
	@Override
	public Q orderBy(OrderSpecifier<?>... o) {
		return queryMixin.orderBy(o);
	}

	/*
	 * (non-Javadoc)
	 * @see com.querydsl.core.SimpleQuery#set(com.querydsl.core.types.ParamExpression, Object)
	 */
	@Override
	public <T> Q set(ParamExpression<T> param, T value) {
		return queryMixin.set(param, value);
	}

}
