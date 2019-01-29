package org.springframework.boot.orientdb.hello.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.data.orient.commons.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "PERSON", schema = "")
@JsonIgnoreProperties(value = {"handler"})
public class Person {

    @Id
    @Column(name="ID")
    private String id;
    
    @Version
    @JsonIgnore
    private Long version;
    
    @Field("first_Name")
    @Column(name="first_Name")
    private String firstName;
    
    @Field("last_Name")
    @Column(name="last_Name")
    private String lastName;
    
    @Field("age")
    @Column(name="age")
    private Integer age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
