package com.example.configcentralclient;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Log
public class ConfigCentralClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigCentralClientApplication.class, args);
    }

    @Bean
    public CommandLineRunner startBoot(@Value("${message:Hello default}") String message) {
        return args -> log.info("message: " + message);
    }
}
