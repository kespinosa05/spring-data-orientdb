package org.springframework.boot.orientdb.hello.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.boot.orientdb.hello.data.QPerson;
import org.springframework.boot.orientdb.hello.repository.IPersonRepository;
import org.springframework.boot.orientdb.hello.repository.PersonRepositoryImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/persons")
public class PersonController {

    @Autowired
    private PersonRepositoryImpl repository;
    @Autowired
    private IPersonRepository iRepository;
    
    @RequestMapping(method = RequestMethod.GET)
    public List<Person> findAllPersons() {
        return repository.findAll();
    }
    
    @RequestMapping("/findAll")
    public Iterable<Person> findAll() {
        return null;// iRepository.findAll(QPerson.person.firstName.eq("Graham"));
    }
    
    @RequestMapping("/custom")
    public Iterable<Person> custom() {
    	return  repository.custom();
    }
    
    
    @RequestMapping("/findByFirstName")
    public List<Person> findByFirstName(@RequestParam String firstName) {
        return iRepository.findByFirstName(firstName);
    }
    
    @RequestMapping("/findByLastName")
    public List<Person> findByLastName(@RequestParam String lastName) {
        return iRepository.findByLastName(lastName);
    }
    
    @RequestMapping("/findByAge")
    public List<Person> findByAge(@RequestParam Integer age) {
        return iRepository.findByAge(age);
    }
      
}
