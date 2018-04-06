package com.example.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.java.Log;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;

@RestController
@EnableZipkinStreamServer
@SpringBootApplication
@Log
public class BookApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }

    @GetMapping("/available")
    public String available() {
        log.info("available is called");
        return "Spring is available";
    }

    @GetMapping("checked-out")
    public String checkedOut() {
        log.info("checked out is called");
        return "Spring boot in action";
    }
}


