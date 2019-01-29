package org.springframework.boot.orientdb.hello.repository;

import java.util.List;

import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.data.domain.Sort;
import org.springframework.data.orient.commons.repository.DefaultSource;
import org.springframework.data.orient.commons.repository.query.JooqUtils;
import org.springframework.data.orient.commons.repository.query.QueryUtils;
import org.springframework.data.orient.object.repository.support.BaseOrientObjectRepository;
import org.springframework.stereotype.Repository;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Repository("personRepository")
public class PersonRepositoryImpl  extends BaseOrientObjectRepository<Person>  {
 
	public List<Person> custom(){
		Query query = JooqUtils.context().select(org.springframework.boot.orientdb.hello.tables.Person.PERSON.FIRST_NAME).from(org.springframework.boot.orientdb.hello.tables.Person.PERSON).where(org.springframework.boot.orientdb.hello.tables.Person.PERSON.FIRST_NAME.eq("Kenny")).orderBy(org.springframework.boot.orientdb.hello.tables.Person.PERSON.FIRST_NAME.asc());
		
		String sql = JooqUtils.from(Person.class).getSQL(ParamType.INLINED);
		
		
		//return (List<Person>) JooqUtils.query(operations, query);
		return null;
	}
    
}
