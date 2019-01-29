package org.springframework.data.orient.commons.repository.query;

import java.lang.reflect.Method;

import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * {@link QueryLookupStrategy} to create {@link PartTreeMongoQuery} instances.
 * 
 * @author klopez
 *
 */
public final class OrientQueryLookupStrategy implements QueryLookupStrategy {

	private final OrientOperations operations;
	private final QueryMethodEvaluationContextProvider evaluationContextProvider;
	
	public OrientQueryLookupStrategy(OrientOperations operations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		super();
		this.operations = operations;
		this.evaluationContextProvider = evaluationContextProvider;
	}

	private abstract static class AbstractQueryLookupStrategy implements QueryLookupStrategy {

		private final OrientOperations operations;
		private final QueryMethodEvaluationContextProvider evaluationContextProvider;
		
		public AbstractQueryLookupStrategy(OrientOperations template, QueryMethodEvaluationContextProvider evaluationContextProvider) {
			this.operations = template;
			this.evaluationContextProvider = evaluationContextProvider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#
		 * resolveQuery(java.lang.reflect.Method,
		 * org.springframework.data.repository.core.RepositoryMetadata,
		 * org.springframework.data.repository.core.NamedQueries)
		 */
		public final RepositoryQuery resolveQuery(java.lang.reflect.Method method, RepositoryMetadata metadata,
				ProjectionFactory factory, NamedQueries namedQueries) {
			return resolveQuery(new OrientQueryMethod(method, metadata, factory), operations, namedQueries);
		}

		protected abstract RepositoryQuery resolveQuery(OrientQueryMethod method, OrientOperations template,
				NamedQueries namedQueries);
	}

	private static class CreateQueryLookupStrategy extends AbstractQueryLookupStrategy {

		/**
		 * Instantiates a new {@link CreateQueryLookupStrategy} lookup strategy.
		 *
		 * @param db
		 *            the application database service
		 */
		public CreateQueryLookupStrategy(OrientOperations template, QueryMethodEvaluationContextProvider evaluationContextProvider) {
			super(template, evaluationContextProvider);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.epam.e3s.data.repository.query.E3sQueryLookupStrategy.
		 * AbstractQueryLookupStrategy#resolveQuery(com.epam.e3s.data.repository.query.
		 * E3sQueryMethod, com.epam.e3s.core.db.AppDbService,
		 * org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		protected RepositoryQuery resolveQuery(OrientQueryMethod method, OrientOperations operations,
				NamedQueries namedQueries) {
			try {
				return new PartTreeOrientQuery(method, operations);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						String.format("Could not create query metamodel for method %s!", method.toString()), e);
			}
		}
	}

	private static class DeclaredQueryLookupStrategy extends AbstractQueryLookupStrategy {

		/**
		 * Instantiates a new {@link DeclaredQueryLookupStrategy} lookup strategy.
		 *
		 * @param template
		 *            the application database service
		 */
		public DeclaredQueryLookupStrategy(OrientOperations template, QueryMethodEvaluationContextProvider evaluationContextProvider) {
			super(template, evaluationContextProvider);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.epam.e3s.data.repository.query.E3sQueryLookupStrategy.
		 * AbstractQueryLookupStrategy#resolveQuery(com.epam.e3s.data.repository.query.
		 * E3sQueryMethod, com.epam.e3s.core.db.AppDbService,
		 * org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		protected RepositoryQuery resolveQuery(OrientQueryMethod method, OrientOperations template,
				NamedQueries namedQueries) {
			String query = method.getAnnotatedQuery();

			if (query != null) {
				return new StringBasedOrientQuery(query, method, template, null);
			}

			throw new IllegalStateException(
					String.format("Did neither find a NamedQuery nor an annotated query for method %s!", method));
		}
	}

	private static class CreateIfNotFoundQueryLookupStrategy extends AbstractQueryLookupStrategy {

		/** The declared query strategy. */
		private final DeclaredQueryLookupStrategy strategy;

		/** The create query strategy. */
		private final CreateQueryLookupStrategy createStrategy;

		/**
		 * Instantiates a new {@link CreateIfNotFoundQueryLookupStrategy} lookup
		 * strategy.
		 *
		 * @param db
		 *            the application database service
		 */
		public CreateIfNotFoundQueryLookupStrategy(OrientOperations db, QueryMethodEvaluationContextProvider evaluationContextProvider) {
			super(db, evaluationContextProvider);
			this.strategy = new DeclaredQueryLookupStrategy(db, evaluationContextProvider);
			this.createStrategy = new CreateQueryLookupStrategy(db, evaluationContextProvider);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.epam.e3s.data.repository.query.E3sQueryLookupStrategy.
		 * AbstractQueryLookupStrategy#resolveQuery(com.epam.e3s.data.repository.query.
		 * E3sQueryMethod, com.epam.e3s.core.db.AppDbService,
		 * org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		protected RepositoryQuery resolveQuery(OrientQueryMethod method, OrientOperations template,
				NamedQueries namedQueries) {
			try {
				return strategy.resolveQuery(method, template, namedQueries);
			} catch (IllegalStateException e) {
				return createStrategy.resolveQuery(method, template, namedQueries);
			}
		}
	}

	public static QueryLookupStrategy create(OrientOperations operations, Key key, QueryMethodEvaluationContextProvider evaluationContextProvider ) {
		if (key == null) {
			return new CreateIfNotFoundQueryLookupStrategy(operations, evaluationContextProvider);
		}

		switch (key) {
		case CREATE:
			return new CreateQueryLookupStrategy(operations, evaluationContextProvider);
		case USE_DECLARED_QUERY:
			return new DeclaredQueryLookupStrategy(operations, evaluationContextProvider);
		case CREATE_IF_NOT_FOUND:
			return new CreateIfNotFoundQueryLookupStrategy(operations, evaluationContextProvider);
		default:
			throw new IllegalArgumentException(String.format("Unsupported query lookup strategy %s!", key));
		}
	}

	@Override
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
			NamedQueries namedQueries) {
		OrientQueryMethod queryMethod = new OrientQueryMethod(method, metadata, factory);
		String namedQueryName = queryMethod.getNamedQueryName();
		
		if (namedQueries.hasQuery(namedQueryName)) {
			String namedQuery = namedQueries.getQuery(namedQueryName);
			return new StringBasedOrientQuery(namedQuery, queryMethod, operations, evaluationContextProvider);
		} else if (queryMethod.hasAnnotatedQuery()) {
			return new StringBasedOrientQuery(queryMethod, operations, evaluationContextProvider);
		} else {
			return new PartTreeOrientQuery(queryMethod, operations);
		}
	}

}
