package com.example.mongoembeddeddemo

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import java.util.function.BiFunction
import org.assertj.core.api.Assertions as Assertj

@ExtendWith(SpringExtension::class)
class UserStateTests {

    @Test
    fun `test Never to test Getters and Setters`() {
        val user = User("1234", "Mario")

        Assertions.assertNotNull(user)
        Assertions.assertNotSame(user, User("1234", "Mario"))

        val userMatcher = allOf(
                Matchers.notNullValue(),
                hasProperty("name", equalTo("Mario")),
                hasProperty("id", equalTo("1234"))
        )

        MatcherAssert.assertThat(user, userMatcher)

        Assertj.assertThat(user).`as`("contains all properties")
                .isNotNull
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", "Mario")
                .hasFieldOrPropertyWithValue("id", "1234")
    }

    private val generalUserMatcher = Matchers.allOf(
            Matchers.notNullValue(),
            Matchers.hasProperty("name", Matchers.notNullValue()),
            Matchers.hasProperty("id", Matchers.notNullValue())
    )!!

    @Test
    fun `test never should fail StepVerify tests`() {

        val userFlux: Flux<User> = Flux
                .just("mario", "seb")
                .zipWith(Flux.just(1L, 2L),
                        BiFunction { n: String, id: Long -> User(id.toString(), n) })

        reactor.test.StepVerifier.create(userFlux)
                .expectSubscription()
                .assertNext { user -> MatcherAssert.assertThat(user, generalUserMatcher) }
                .assertNext { user -> MatcherAssert.assertThat(user, generalUserMatcher) }
                .verifyComplete()
    }

}
