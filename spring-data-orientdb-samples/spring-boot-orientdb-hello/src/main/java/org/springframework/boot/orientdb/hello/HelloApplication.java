package org.springframework.boot.orientdb.hello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.boot.orientdb.hello.repository.PersonRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages =  {"org.springframework.boot.orientdb.hello"} )
public class HelloApplication implements CommandLineRunner {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private OrientObjectDatabaseFactory factory;

    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
    	
        OObjectDatabaseTx db = null;
   
        try {
            db = factory.openDatabase();
            db.getEntityManager().registerEntityClass(Person.class);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        
        //Create Persons if required
        if (repository.count() < 1) {
            List<Person> persons = new ArrayList<>();
            
            Person graham = new Person();
            graham.setFirstName("Graham");
            graham.setLastName("Jacobson");
            graham.setAge(25);
            
            persons.add(graham);
            
            Person ebony = new Person();
            ebony.setFirstName("Ebony");
            ebony.setLastName("Irwin");
            ebony.setAge(21);
            
            persons.add(ebony);
            
            Person benedict = new Person();
            benedict.setFirstName("Benedict");
            benedict.setLastName("Preston");
            benedict.setAge(25);
            
            persons.add(benedict);
            
            Person zorita = new Person();
            zorita.setFirstName("Zorita");
            zorita.setLastName("Clements");
            zorita.setAge(23);
            
            persons.add(zorita);
            
            Person kaitlin = new Person();
            kaitlin.setFirstName("Kaitlin");
            kaitlin.setLastName("Walter");
            kaitlin.setAge(22);
            
            persons.add(kaitlin);
            
            repository.saveAll(persons);
        }
    }
    
    /**
     * commandLineRunner.
     * 
     * @param ctx
     *            the ctx
     * @return CommandLineRunner instance
     */
    @Bean
    public CommandLineRunner commandLineRunner(ListableBeanFactory ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Beans Loaded by Spring Boot:{}", ctx.getBeanDefinitionCount());
        }
        return args -> {
            if (log.isDebugEnabled()) {
                String[] beanNames = ctx.getBeanDefinitionNames();
                Arrays.sort(beanNames);
                for (String beanName : beanNames) {
                    log.debug("Bean:{}", beanName);
                }
            }
        };
    }
}
