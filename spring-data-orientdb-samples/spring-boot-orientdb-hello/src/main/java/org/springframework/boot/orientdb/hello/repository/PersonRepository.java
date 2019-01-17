package org.springframework.boot.orientdb.hello.repository;

import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.data.orient.object.repository.support.BaseOrientObjectRepository;
import org.springframework.stereotype.Repository;

@Repository("personRepository2")
public class PersonRepository  extends BaseOrientObjectRepository<Person>  {
    
    
}
