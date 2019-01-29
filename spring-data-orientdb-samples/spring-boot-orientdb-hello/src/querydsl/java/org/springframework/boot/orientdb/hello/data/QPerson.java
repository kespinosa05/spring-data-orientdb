package org.springframework.boot.orientdb.hello.data;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QPerson is a Querydsl query type for Person
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QPerson extends EntityPathBase<Person> {

    private static final long serialVersionUID = 2001682311L;

    public static final QPerson person = new QPerson("person");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath firstName = createString("firstName");

    public final StringPath id = createString("id");

    public final StringPath lastName = createString("lastName");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QPerson(String variable) {
        super(Person.class, forVariable(variable));
    }

    public QPerson(Path<? extends Person> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPerson(PathMetadata metadata) {
        super(Person.class, metadata);
    }

}

