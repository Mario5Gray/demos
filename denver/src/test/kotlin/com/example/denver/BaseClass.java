package com.example.denver;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseClass {
    @LocalServerPort
    private int localPort;

    @MockBean
    private MessageService service;

    @BeforeEach
    void setUp() {
        Mockito
                .when(service.get(any()))
                .thenReturn(Flux.just(
                        new Message(new UUID(123456L, 0L), "Mario", "Demo Time")
                ));

        RestAssured.baseURI = "http://localhost:" + localPort + "/";
    }
}
