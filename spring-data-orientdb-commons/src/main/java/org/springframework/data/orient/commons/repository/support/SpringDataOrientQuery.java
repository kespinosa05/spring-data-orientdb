package org.springframework.data.orient.commons.repository.support;

import java.util.List;

import org.springframework.data.orient.commons.core.OrientOperations;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Fetchable;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.SimpleQuery;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Predicate;



public class SpringDataOrientQuery<T> extends QuerydslFetchableOrientQuery<T, SpringDataOrientQuery<T>>{
	
	/**
	 * Creates a new {@link SpringDataMongodbQuery}.
	 *
	 * @param operations must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 */
	public SpringDataOrientQuery(final OrientOperations<T> operations, final Class<? extends T> type) {
		super(operations,type);
	}
	
	@Override
	public List<T> fetch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T fetchFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T fetchOne() throws NonUniqueResultException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseableIterator<T> iterate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResults<T> fetchResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long fetchCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
