package org.springframework.data.orient.commons.repository.query;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.conf.ParamType;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.data.orient.commons.core.OrientOperations;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public final class JooqUtils {

    private JooqUtils() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    public static DSLContext context() {
    	Settings settings = new Settings();
    	settings.setRenderSchema(false);
    	settings.setRenderCatalog(false);
    	settings.setRenderNameStyle(RenderNameStyle.AS_IS);
    	settings.setRenderKeywordStyle(RenderKeywordStyle.PASCAL);
    	 
        return DSL.using(SQLDialect.MYSQL, settings);
    }
    
    public static SelectJoinStep<? extends Record> from(Class<?> domainClass) {
        return context().select().from(QueryUtils.toSourceSimple(domainClass));
    }
    
    public static SelectJoinStep<? extends Record> from(String source) {
        return context().select().from(source);
    }
    
    public  static List<?> query(OrientOperations<?> operations,  Query query) {
    	return operations.query(new OSQLSynchQuery<>(query.getSQL(ParamType.INLINED)));
    }
}
