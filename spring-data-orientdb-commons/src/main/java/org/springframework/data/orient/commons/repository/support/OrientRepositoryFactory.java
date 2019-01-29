package org.springframework.data.orient.commons.repository.support;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentEntity;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentProperty;
import org.springframework.data.orient.commons.repository.SourceType;
import org.springframework.data.orient.commons.repository.annotation.Cluster;
import org.springframework.data.orient.commons.repository.annotation.Source;
import org.springframework.data.orient.commons.repository.query.OrientEntityInformation;
import org.springframework.data.orient.commons.repository.query.OrientQueryLookupStrategy;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Orient specific generic repository factory.
 *
 * @author Dzmitry_Naskou
 */
public class OrientRepositoryFactory extends RepositoryFactorySupport {

	/**
	 * The orient template.
	 */
	protected final OrientOperations operations;
	private final MappingContext<? extends OrientPersistentEntity<?>, OrientPersistentProperty> mappingContext;

	/**
	 * Instantiates a new {@link OrientRepositoryFactory}.
	 *
	 * @param operations
	 *            the orient object template
	 */
	public OrientRepositoryFactory(OrientOperations operations) {
		super();
		Assert.notNull(operations, "MongoOperations must not be null!");
		this.operations = operations;
		this.mappingContext = operations.getConverter().getMappingContext();
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation metadata) {
		EntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());
		Class<?> repositoryInterface = metadata.getRepositoryInterface();
		Class<?> javaType = entityInformation.getJavaType();
		String cluster = getCustomCluster(metadata);

		if (cluster != null) {
			return new SimpleOrientRepository(operations, javaType, cluster, repositoryInterface);
		} else {
			return new SimpleOrientRepository(operations, javaType, repositoryInterface);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactorySupport#
	 * getRepositoryFragments(org.springframework.data.repository.core.
	 * RepositoryMetadata)
	 */
	@Override
	protected RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {

		RepositoryFragments fragments = RepositoryFragments.empty();

		boolean isQueryDslRepository = QuerydslUtils.QUERY_DSL_PRESENT
				&& QuerydslPredicateExecutor.class.isAssignableFrom(metadata.getRepositoryInterface());

		if (isQueryDslRepository) {

			if (metadata.isReactiveRepository()) {
				throw new InvalidDataAccessApiUsageException(
						"Cannot combine Querydsl and reactive repository support in a single interface");
			}

			OrientEntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType(),
					metadata);

			fragments = fragments.append(RepositoryFragment.implemented(getTargetRepositoryViaReflection(
					QuerydslOrientPredicateExecutor.class, entityInformation, operations)));
		}

		return fragments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactorySupport#
	 * getEntityInformation(java.lang.Class)
	*/
	public <T, ID> OrientEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return getEntityInformation(domainClass, null);
	}

	private <T, ID> OrientEntityInformation<T, ID> getEntityInformation(Class<T> domainClass,
			@Nullable RepositoryMetadata metadata) {

		OrientPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
		return OrientEntityInformationSupport.<T, ID>entityInformationFor(entity,
				metadata != null ? metadata.getIdType() : null);
	} 
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactorySupport#
	 * getRepositoryBaseClass(org.springframework.data.repository.core.
	 * RepositoryMetadata)
	 */
	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleOrientRepository.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactorySupport#
	 * getQueryLookupStrategy(org.springframework.data.repository.query.
	 * QueryLookupStrategy.Key)
	 */
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		QueryLookupStrategy orientQueryLookupStrategy = new OrientQueryLookupStrategy(operations,
				evaluationContextProvider);
		return Optional.of(orientQueryLookupStrategy);
	}

	/**
	 * Get Custom Cluster Name. Method looks for {@link Source} and {@link Cluster}
	 * annotation.
	 * <p>
	 * If {@link Source} is not null and
	 * {@link org.springframework.data.orient.commons.repository.annotation.Source#type()}
	 * equals to
	 * {@link org.springframework.data.orient.commons.repository.SourceType#CLUSTER}
	 * then returns
	 * {@link org.springframework.data.orient.commons.repository.annotation.Source#value()}
	 * <p>
	 * If {@link Cluster} is not null then returns
	 * {@link org.springframework.data.orient.commons.repository.annotation.Cluster#value()}
	 *
	 * @param metadata
	 * @return cluster name or null if it's not defined
	 */
	protected String getCustomCluster(RepositoryMetadata metadata) {
		Class<?> repositoryInterface = metadata.getRepositoryInterface();

		Source source = AnnotationUtils.getAnnotation(repositoryInterface, Source.class);
		if (source != null && SourceType.CLUSTER.equals(source.type())) {
			return source.value();
		}

		Cluster cluster = AnnotationUtils.getAnnotation(repositoryInterface, Cluster.class);
		if (cluster != null) {
			return cluster.value();
		}
		return null;
	}

}
