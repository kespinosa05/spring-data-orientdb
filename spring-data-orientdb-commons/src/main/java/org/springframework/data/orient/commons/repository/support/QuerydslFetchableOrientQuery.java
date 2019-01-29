package org.springframework.data.orient.commons.repository.support;

import java.util.List;

import org.springframework.data.orient.commons.core.OrientOperations;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Fetchable;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;

public abstract class QuerydslFetchableOrientQuery<K, Q extends QuerydslFetchableOrientQuery<K, Q>>
		extends QuerydslAbstractOrientQuery<K, Q> implements Fetchable<K> {

	private OrientOperations<K> operations;
	private Class<? extends K> entityClass;

	public QuerydslFetchableOrientQuery(OrientOperations<K> operations, Class<? extends K> entityClass) {
		this.operations = operations;
		this.entityClass = entityClass;
	}
	
	@Override
	public List<K> fetch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public K fetchFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public K fetchOne() throws NonUniqueResultException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseableIterator<K> iterate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResults<K> fetchResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long fetchCount() {
		// TODO Auto-generated method stub
		return 0;
	}


}
