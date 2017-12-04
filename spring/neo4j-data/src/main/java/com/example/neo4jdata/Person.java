package com.example.neo4jdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @GraphId
    private Long id;

    private String name;

    public Person(String name) {
        this.name = name;
    }

    @Relationship(type = "TEAMMATE", direction = Relationship.UNDIRECTED)
    Set<Person> teammates;

    public void worksWith(Person p) {
        if (teammates == null) {
            teammates = new HashSet<>();
        }
        teammates.add(p);
    }

    public String toString() {

        return this.name + "'s teammates => "
                + Optional.ofNullable(this.teammates).orElse(
                Collections.emptySet()).stream().map(
                person -> person.getName()).collect(Collectors.toList());
    }
}
