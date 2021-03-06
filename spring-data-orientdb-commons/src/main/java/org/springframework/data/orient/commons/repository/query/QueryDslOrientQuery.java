package org.springframework.data.orient.commons.repository.query;

import com.orientechnologies.orient.core.sql.query.OSQLQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * A {@link AbstractOrientQuery} implementation based on a {@link org.springframework.data.repository.query.parser.PartTree}.
 *
 * @author Dzmitry_Naskou
 */
public class QueryDslOrientQuery extends AbstractOrientQuery {

    /** The domain class. */
    private final Class<?> domainClass;

    /** The tree. */
    private final PartTree tree;

    /** The parameters. */
    private final OrientParameters parameters;

    private final OrientQueryMethod method;

    /**
     * Instantiates a new {@link QueryDslOrientQuery} from given {@link OrientQueryMethod} and {@link OrientOperations}.
     *
     * @param method the query method
     * @param operations the orient object template
     */
    public QueryDslOrientQuery(OrientQueryMethod method, OrientOperations operations) {
        super(method, operations);

        this.method = method;
        this.domainClass = method.getEntityInformation().getJavaType();
        this.tree = new PartTree(method.getName(), domainClass);
        this.parameters = method.getParameters();
    }

    /* (non-Javadoc)
     * @see org.springframework.data.orient.repository.object.query.AbstractOrientQuery#doCreateQuery(java.lang.Object[])
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery<?> doCreateQuery(Object[] values) {
        OrientParameterAccessor accessor = new OrientParametersParameterAccessor(parameters, values);
        
        OrientQueryCreator creator = new OrientQueryCreator(tree, method, accessor);
        
        return new OSQLSynchQuery(creator.createQuery());
    }

    /* (non-Javadoc)
     * @see org.springframework.data.orient.repository.object.query.AbstractOrientQuery#doCreateCountQuery(java.lang.Object[])
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected OSQLQuery<?> doCreateCountQuery(Object[] values) {
        OrientParameterAccessor accessor = new OrientParametersParameterAccessor(parameters, values);
        
        OrientQueryCreator creator = new OrientCountQueryCreator(tree, method, accessor);
        
        return new OSQLSynchQuery(creator.createQuery());
    }

    /* (non-Javadoc)
     * @see org.springframework.data.orient.repository.object.query.AbstractOrientQuery#isCountQuery()
     */
    @Override
    protected boolean isCountQuery() {
        return tree.isCountProjection();
    }
}
