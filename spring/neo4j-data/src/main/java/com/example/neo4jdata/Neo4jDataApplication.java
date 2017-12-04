package com.example.neo4jdata;

import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableNeo4jRepositories
@Log
public class Neo4jDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(Neo4jDataApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(PersonRepository repo) {
        return args -> {
            repo.deleteAll();
            List<Person> team = Arrays.asList(
                    new Person("Greg"),
                    new Person("Mike"),
                    new Person("Mark")
            );

            log.info("Before linking with Neo4j...");

            team.stream().forEach(p -> log.info("\t" + p.getName()));

            team.stream().forEach(repo::save);
            Person greg = repo.findByName("Greg");
            Person mike = repo.findByName("Mike");

            greg.worksWith(mike);
            repo.save(greg);

            log.info("Lookup each peep by name");
            team.forEach(p -> log.info(repo.findByName(p.getName()).toString()));


        };
    }
}

