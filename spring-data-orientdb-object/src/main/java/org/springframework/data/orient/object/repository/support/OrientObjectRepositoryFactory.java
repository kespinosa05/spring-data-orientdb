package org.springframework.data.orient.object.repository.support;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentEntity;
import org.springframework.data.orient.commons.core.mapping.OrientPersistentProperty;
import org.springframework.data.orient.commons.repository.SourceType;
import org.springframework.data.orient.commons.repository.annotation.Cluster;
import org.springframework.data.orient.commons.repository.annotation.Source;
import org.springframework.data.orient.commons.repository.query.OrientEntityInformation;
import org.springframework.data.orient.commons.repository.query.OrientQueryLookupStrategy;
import org.springframework.data.orient.commons.repository.support.OrientEntityInformationSupport;
import org.springframework.data.orient.commons.repository.support.OrientMetamodelEntityInformation;
import org.springframework.data.orient.commons.repository.support.QuerydslOrientPredicateExecutor;
import org.springframework.data.orient.commons.repository.support.SimpleOrientRepository;
import org.springframework.data.orient.object.OrientObjectOperations;
import org.springframework.data.orient.object.repository.OrientObjectRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Optional;

// TODO: find out why inheriting from OrientRepositoryFactory does not work; would save some code; but this here works
public class OrientObjectRepositoryFactory extends RepositoryFactorySupport {

    private final OrientObjectOperations operations;
    private final MappingContext<? extends OrientPersistentEntity<?>, OrientPersistentProperty> mappingContext;

    public OrientObjectRepositoryFactory(OrientObjectOperations operations) {
        super();
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
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
	
    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return (EntityInformation<T, ID>) new OrientMetamodelEntityInformation<>(domainClass);
    }
    
    private <T, ID> OrientEntityInformation<T, ID> getEntityInformation(Class<T> domainClass,
			@Nullable RepositoryMetadata metadata) {

		OrientPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(domainClass);
		return OrientEntityInformationSupport.<T, ID>entityInformationFor(entity,
				metadata != null ? metadata.getIdType() : null);
	} 

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked"})
    protected Object getTargetRepository(RepositoryInformation metadata) {
        EntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());
        Class<?> repositoryInterface = metadata.getRepositoryInterface();
        Class<?> javaType = entityInformation.getJavaType();
        String cluster = getCustomCluster(metadata);

        if (isObjectRepository(metadata.getRepositoryInterface())) {
            if (cluster != null) {
                return new SimpleOrientObjectRepository(operations, javaType, cluster, repositoryInterface);
            } else {
                return new SimpleOrientObjectRepository(operations, javaType, repositoryInterface);
            }
        } else {
            if (cluster != null) {
                return new SimpleOrientRepository(operations, javaType, cluster, repositoryInterface);
            } else {
                return new SimpleOrientRepository(operations, javaType, repositoryInterface);
            }
        }
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(OrientQueryLookupStrategy.create(operations, key, evaluationContextProvider));
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (isObjectRepository(metadata.getRepositoryInterface())) {
            return SimpleOrientObjectRepository.class;
        } else {
            return SimpleOrientRepository.class;
        }
    }

    private boolean isObjectRepository(Class<?>  repositoryInterface) {
        return OrientObjectRepository.class.isAssignableFrom(repositoryInterface);
    }

    /**
     * Get Custom Cluster Name.
     * Method looks for {@link org.springframework.data.orient.commons.repository.annotation.Source} and {@link org.springframework.data.orient.commons.repository.annotation.Cluster} annotation.
     *
     * If {@link org.springframework.data.orient.commons.repository.annotation.Source} is not null and {@link org.springframework.data.orient.commons.repository.annotation.Source#type()} equals to
     * {@link org.springframework.data.orient.commons.repository.SourceType#CLUSTER} then returns {@link org.springframework.data.orient.commons.repository.annotation.Source#value()}
     *
     * If {@link org.springframework.data.orient.commons.repository.annotation.Cluster} is not null then returns {@link org.springframework.data.orient.commons.repository.annotation.Cluster#value()}
     *
     * @param metadata
     * @return cluster name or null if it's not defined
     */
    private String getCustomCluster(RepositoryMetadata metadata){
        Class<?> repositoryInterface = metadata.getRepositoryInterface();

        Source source = AnnotationUtils.getAnnotation(repositoryInterface, Source.class);
        if(source != null && SourceType.CLUSTER.equals(source.type())){
            return source.value();
        }

        Cluster cluster = AnnotationUtils.getAnnotation(repositoryInterface, Cluster.class);
        if (cluster != null){
            return cluster.value();
        }
        return null;
    }
}
