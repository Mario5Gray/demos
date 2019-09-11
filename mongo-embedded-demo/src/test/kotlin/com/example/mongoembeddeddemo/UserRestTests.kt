package com.example.mongoembeddeddemo

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier

@WebFluxTest
@ExtendWith(SpringExtension::class)
class UserRestTests(@Autowired private val webTestClient: WebTestClient) {

    @MockBean
    private lateinit var repo: UserRepository

    private val generalUserMatcher = Matchers.allOf(
            Matchers.notNullValue(),
            Matchers.hasProperty("name", Matchers.notNullValue()),
            Matchers.hasProperty("id", Matchers.notNullValue())
    )!!

    private val user1 = User("1234", "Mario")
    private val user2 = User("2345", "Lilo")
    private val user3 = User("3456", "Lotte")

    @BeforeEach
    fun setUp() {
        Mockito
                .`when`(repo.findFavorites())
                .thenReturn(Flux.just(user1, user2))

        Mockito
                .`when`(repo.findAll())
                .thenReturn(Flux.just(user1, user2, user3))
    }

    @Test
    fun `test should retrieve all users from web`() {
        Hooks.onOperatorDebug()
        val allUsersFlux = WebTestClient.bindToRouterFunction(UserWebConfig().restRoutes(repo)).build()
                .get()
                .uri("/all")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .returnResult<User>()
                .responseBody

        StepVerifier
                .create(allUsersFlux)
                .expectSubscription()
                .assertNext { MatcherAssert.assertThat(it, generalUserMatcher) }
                .assertNext { MatcherAssert.assertThat(it, generalUserMatcher) }
                .assertNext { MatcherAssert.assertThat(it, generalUserMatcher) }
                .verifyComplete()
    }

    @Test
    fun `test should retrieve favorites from web`() {
        Hooks.onOperatorDebug()
        WebTestClient
                .bindToRouterFunction(UserWebConfig().restRoutes(repo))
                .build()
                .get()
                .uri("/favorites")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("\$.[0].name").isEqualTo("Mario")
                .jsonPath("\$.[1].name").isEqualTo("Lilo")
    }

}