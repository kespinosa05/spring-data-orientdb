package org.springframework.data.orient.commons.repository.support;

import org.springframework.data.orient.commons.core.OrientOperations;

import com.querydsl.core.Fetchable;

public abstract class QuerydslFetchableOrientQuery<K, Q extends QuerydslFetchableOrientQuery<K, Q>>
		extends QuerydslAbstractOrientQuery<K, Q> implements Fetchable<K> {

	private OrientOperations<K> operations;
	private Class<? extends K> type;

	public QuerydslFetchableOrientQuery(OrientOperations<K> operations, Class<? extends K> type) {
		this.operations = operations;
		this.type = type;
	}

}
