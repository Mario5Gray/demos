package com.example.mongoembeddeddemo

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration
import java.util.function.Supplier

@ExtendWith(SpringExtension::class)
@DataMongoTest
class UserPersistenceTests(@Autowired private val repo: UserRepository) {

    private val generalUserMatcher = Matchers.allOf(
            Matchers.notNullValue(),
            Matchers.hasProperty("name", Matchers.notNullValue()),
            Matchers.hasProperty("id", Matchers.notNullValue())
    )!!

    private val user1 = User(null, "Mario")
    private val user2 = User(null, "Lilo")

    @Test
    fun `test should save and retrieve periodic Mongo Entities`() {
        val compositeSupplier = Supplier {
            val setup = repo
                    .deleteAll()
                    .thenMany(repo.saveAll(Flux.just(user1, user2)))

            val search = repo
                    .findAll()
                    .delayElements(Duration.ofSeconds(10))
                    .take(2)

            Flux
                    .from(setup)
                    .thenMany(search)
        }

        StepVerifier
                .withVirtualTime(compositeSupplier)
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(10))
                .assertNext { user -> MatcherAssert.assertThat(user, generalUserMatcher) }
                .thenAwait(Duration.ofSeconds(10))
                .assertNext { user -> MatcherAssert.assertThat(user, generalUserMatcher) }
                .verifyComplete()
    }

    @Test
    fun `test should save and retrieve these Mongo Entities`() {
        val setup = repo
                .deleteAll()
                .thenMany(repo.saveAll(Flux.just(user1, user2)))

        val search = repo.findAll()

        val composite = Flux
                .from(setup)
                .thenMany(search)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .assertNext { MatcherAssert.assertThat(it, generalUserMatcher) }
                .assertNext { MatcherAssert.assertThat(it, generalUserMatcher) }
                .verifyComplete()
    }

    @Test
    fun `Should find all favorites`() {
        val setup = repo
                .deleteAll()
                .thenMany(repo.saveAll(Flux.just(user1, user2)))

        val search = repo.findFavorites()

        val composite = Flux
                .from(setup)
                .thenMany(search)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .assertNext { MatcherAssert.assertThat(it.name, Matchers.equalTo("Mario")) }
                .assertNext { MatcherAssert.assertThat(it.name, Matchers.equalTo("Lilo")) }
                .verifyComplete()
    }
}