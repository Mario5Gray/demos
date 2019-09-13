package com.example.denver

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.util.*

fun <T> anyObject(): T {
    Mockito.anyObject<T>()
    return uninitialized()
}

fun <T> uninitialized(): T = null as T

@WebFluxTest
class DenverWebTests {

    @Autowired
    private lateinit var testClient: WebTestClient

    @MockBean
    private lateinit var service: MessageService

    @Configuration
    class WebConfig(svc: MessageService) : DenverWebController(svc)

    @BeforeEach
    fun setUp() {
        BDDMockito
                .given(service.get(anyObject()))
                .willReturn(Flux.just(
                        Message(UUID(123456L, 0L), "Mario", "Just a Demo!")
                ))
    }

    @Test
    fun `should GET a message`() {
        testClient
                .get()
                .uri("/get")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.[0].from").isEqualTo("Mario")
    }

}