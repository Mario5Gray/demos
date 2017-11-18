package com.example.restfulservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Log
@SpringBootApplication
public class RestfulServiceApplication {

    public static void main(String args[]) {
        SpringApplication.run(RestfulServiceApplication.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner run(RestTemplate rt) throws Exception {
        return args -> {
            Quote quote = rt.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
            log.info(quote.toString());
        };
    }
}

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class Quote {
    private String type;
    private Value value;
}

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class Value {
    private Long id;
    private String quote;
}