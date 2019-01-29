package org.springframework.data.orient.commons.repository.support;

import org.springframework.data.orient.commons.core.OrientOperations;



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
	
	
	
}
