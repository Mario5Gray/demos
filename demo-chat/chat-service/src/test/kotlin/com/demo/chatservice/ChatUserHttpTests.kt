package com.demo.chatservice

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.sql.Time
import java.time.LocalTime
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest
class ChatUserHttpTests {

    @MockBean
    lateinit var repo: ChatUserRepository

    @BeforeEach
    fun setUp() {
        val monoUser = ChatUser(UUID.randomUUID(), "EddieVedder", "Eddie", Time.valueOf(LocalTime.now()))
        Mockito
                .`when`(repo.findByHandle(Mockito.anyString()))
                .thenReturn(Mono.just(
                        monoUser
                ))

        Mockito
                .`when`(repo.insert(Mockito.any<ChatUser>()))
                .thenReturn(Mono.just(monoUser))

    }

    @Test
    fun shouldGetAUserUUID() {
        WebTestClient
                .bindToRouterFunction(UserRouters().routes(repo))
                .build()
                .post()
                .uri("/newuser?handle=EddieVedder")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .exchange()
                .expectStatus().isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.handle").isEqualTo("EddieVedder")
                .jsonPath("$.timestamp").isNotEmpty
                .jsonPath("$.token").isNotEmpty
    }
}