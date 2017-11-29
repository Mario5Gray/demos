package com.example.vaadinui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@Log
@SpringBootApplication
public class VaadinUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaadinUiApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(CustomerRepository repository) {
        return (args) -> {
            Stream.of(
                    "Jack,Bauer",
                    "Chloe,OBrian",
                    "Kim,Bauer",
                    "David,Palmer",
                    "Michelle,Dessler")
                    .map(x -> x.split(","))
                    .map(x -> new Customer(null, x[0], x[1]))
                    .forEach(repository::save);
            repository.findAll()
                    .forEach(c -> log.info(c.toString()));
            Assert.notNull(repository.findOne(1L), "empty db!");
        };
    }
}

