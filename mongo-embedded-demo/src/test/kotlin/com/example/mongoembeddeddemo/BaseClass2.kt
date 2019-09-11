package com.example.mongoembeddeddemo

import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ExtendWith(SpringExtension::class)
//@Import(UserWebConfig::class)
class BaseClass2 {
    @LocalServerPort
    private var port: Long = 0

    @MockBean
    private lateinit var repo: UserRepository

    private val user1 = User(null, "Mario")
    private val user2 = User(null, "Lilo")
    private val user3 = User(null, "Lotte")

    @BeforeEach
    fun setUp() {
        RestAssured.baseURI = "http://localhost:" + this.port

        Mockito
                .`when`(this.repo.findAll())
                .thenReturn(Flux.just(user1, user2))

        Mockito
                .`when`(this.repo.findFavorites())
                .thenReturn(Flux.just(user1, user2, user3))
    }
}