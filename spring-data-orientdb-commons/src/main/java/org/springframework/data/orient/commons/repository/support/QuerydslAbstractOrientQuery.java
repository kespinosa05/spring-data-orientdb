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

	@Override
	public Q where(Predicate... o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q limit(long limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q offset(long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q restrict(QueryModifiers modifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q orderBy(OrderSpecifier<?>... o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Q set(ParamExpression<T> param, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Q distinct() {
		// TODO Auto-generated method stub
		return null;
	}

}
