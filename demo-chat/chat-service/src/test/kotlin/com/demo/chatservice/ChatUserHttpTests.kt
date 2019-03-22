package com.demo.chatservice

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@WebFluxTest
class ChatUserHttpTests {

    @Autowired
    lateinit var repo: ChatUserCrudRepository

    @Test
    fun shouldGetAUserUUID() {
        WebTestClient
                .bindToRouterFunction(UserRouters().routes(repo))
                .build()
                .get()
                .uri("/newuser?handle=EddieVedder")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.[0].handle").isEqualTo("EddieVedder")
                .jsonPath("$.[0].timestamp").isNotEmpty
                .jsonPath("$.[0].token").isNotEmpty
    }
}