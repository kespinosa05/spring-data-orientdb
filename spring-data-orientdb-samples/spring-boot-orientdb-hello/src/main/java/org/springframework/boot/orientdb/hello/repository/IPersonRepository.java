package org.springframework.boot.orientdb.hello.repository;

import java.util.List;

import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.data.orient.commons.repository.annotation.Query;
import org.springframework.data.orient.object.repository.OrientObjectRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface IPersonRepository  extends OrientObjectRepository<Person>,QuerydslPredicateExecutor<Person>   {
    
	List<Person> findByFirstName(String firstName);

    @Query("select from person where lastName = ?")
    List<Person> findByLastName(String lastName);

    List<Person> findByAge(Integer age);
}
