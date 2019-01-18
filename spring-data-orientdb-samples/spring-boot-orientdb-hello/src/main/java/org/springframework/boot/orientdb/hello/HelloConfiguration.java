package org.springframework.boot.orientdb.hello;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orientdb.hello.data.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.orient.commons.core.OrientTransactionManager;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.data.orient.object.OrientObjectTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableOrientRepositories(basePackages = "org.springframework.boot.orientdb.hello.repository")
@EnableAutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HelloConfiguration {

    @Bean
    public OrientObjectDatabaseFactory factory() {
        OrientObjectDatabaseFactory factory =  new OrientObjectDatabaseFactory();

        factory.setUrl("remote:10.200.101.223:2424/inventory");
        factory.setUsername("root");
        factory.setPassword("root");

        return factory;
    }

    @Bean
    public OrientTransactionManager transactionManager() {
        return new OrientTransactionManager(factory());
    }

    @Bean
    public OrientObjectTemplate objectTemplate() {
        return new OrientObjectTemplate(factory());
    }


    @PostConstruct
    public void registerEntities() {
        factory().db().getEntityManager().registerEntityClass(Person.class);
    }
}
