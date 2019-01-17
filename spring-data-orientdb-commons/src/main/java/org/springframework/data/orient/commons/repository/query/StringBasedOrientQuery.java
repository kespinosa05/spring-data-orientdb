package org.springframework.data.orient.commons.repository.query;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
/**
 * Query to use a plain JSON String to create the {@link Query} to actually execute.
 * 
 * @author klopez
 *
 */
public class StringBasedOrientQuery extends AbstractOrientQuery {
    
    private final String queryString;
    
    private final boolean isCountQuery;
    
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;//Not used
    
    /**
	 * Creates a new {@link StringBasedOrientQuery} for the given {@link OrientQueryMethod}, {@link OrientOperations},
	 * {@link SpelExpressionParser} and {@link QueryMethodEvaluationContextProvider}.
	 *
	 * @param method must not be {@literal null}.
	 * @param orientOperations must not be {@literal null}.
	 * @param evaluationContextProvider must not be {@literal null}.
	 */
	public StringBasedOrientQuery(OrientQueryMethod method, OrientOperations orientOperations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		this(method.getAnnotatedQuery(), method, orientOperations, evaluationContextProvider);
	}
	
	/**
	 * Creates a new {@link StringBasedOrientQuery} for the given {@link String}, {@link OrientQueryMethod},
	 * {@link OrientOperations}, {@link SpelExpressionParser} and {@link QueryMethodEvaluationContextProvider}.
	 *
	 * @param query must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param operations must not be {@literal null}.
	 * @param expressionParser must not be {@literal null}.
	 */
    public StringBasedOrientQuery(String query, OrientQueryMethod method, OrientOperations operations, QueryMethodEvaluationContextProvider evaluationContextProvider) {
        super(method, operations);
        this.queryString = query;
        this.isCountQuery = method.hasAnnotatedQuery() ? method.getQueryAnnotation().count() : false;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery<?> doCreateQuery(Object[] values) {
        OrientParameterAccessor accessor = new OrientParametersParameterAccessor(getQueryMethod().getParameters(), values);
        String sortedQuery = QueryUtils.applySorting(queryString, accessor.getSort());
        
        return new OSQLSynchQuery(sortedQuery);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery<?> doCreateCountQuery(Object[] values) {
        return new OSQLSynchQuery<ODocument>(queryString);
    }

    @Override
    protected boolean isCountQuery() {
        return this.isCountQuery;
    }
}
