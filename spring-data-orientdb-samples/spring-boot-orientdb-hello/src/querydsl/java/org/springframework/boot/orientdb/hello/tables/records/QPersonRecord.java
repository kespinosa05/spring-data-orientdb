package org.springframework.boot.orientdb.hello.tables.records;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPersonRecord is a Querydsl query type for PersonRecord
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPersonRecord extends EntityPathBase<PersonRecord> {

    private static final long serialVersionUID = -1474660983L;

    public static final QPersonRecord personRecord = new QPersonRecord("personRecord");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath firstname = createString("firstname");

    public final StringPath id = createString("id");

    public final StringPath lastname = createString("lastname");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QPersonRecord(String variable) {
        super(PersonRecord.class, forVariable(variable));
    }

    public QPersonRecord(Path<? extends PersonRecord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPersonRecord(PathMetadata metadata) {
        super(PersonRecord.class, metadata);
    }

}

