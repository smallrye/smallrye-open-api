package io.smallrye.openapi.testdata.java.panache.reactiveclient;

import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "PersonForEntity")
public class Person extends PanacheEntity {
    String name;
}
