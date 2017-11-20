package com.example.mongodb;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@SpringBootApplication
@Log
public class MongodbApplication implements CommandLineRunner {


    @Autowired
    private CustomerRepostory repository;

    public static void main(String[] args) {
        SpringApplication.run(MongodbApplication.class, args);
    }

    public void run(String... args) throws Exception {
        repository.deleteAll();

        repository.save(new Customer("Mario", "Smith"));
        repository.save(new Customer("Josh", "Smith"));

        log.info("Customers found with findAll()");
        log.info("------------------------------");
        for (Customer customer : repository.findAll()) {
            log.info(customer.toString());
        }
        log.info("/////////////////////////////");

        log.info("Customer foudn with findByFirstName");
        log.info(repository.findByFirstName("Mario").toString());

        log.info("Customer found with findByLastName");
        for (Customer c : repository.findByLastName("Smith")) {
            log.info(c.toString());
        }
    }
}

interface CustomerRepostory extends MongoRepository<Customer, String> {
    Customer findByFirstName(String firstName);

    List<Customer> findByLastName(String lastName);
}

@Data
@ToString
@NoArgsConstructor
class Customer {
    @Id
    String id;

    public Customer(String fn, String ln) {
        this.firstName = fn;
        this.lastName = ln;
    }

    String firstName;
    String lastName;
}