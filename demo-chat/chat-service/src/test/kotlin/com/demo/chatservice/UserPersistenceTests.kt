package com.demo.chatservice

import org.apache.cassandra.config.DatabaseDescriptor
import org.cassandraunit.spring.CassandraDataSet
import org.cassandraunit.spring.CassandraUnit
import org.cassandraunit.spring.CassandraUnitTestExecutionListener
import org.cassandraunit.spring.EmbeddedCassandra
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@SpringBootTest
@ExtendWith(SpringExtension::class)
@TestExecutionListeners(CassandraUnitTestExecutionListener::class)
@CassandraDataSet("simple.cql" )
@EmbeddedCassandra
class UserPersistenceTests {

    @Autowired
    lateinit var template: ReactiveCassandraTemplate

    @Test
    fun shouldPerformSaveCrudFind() {
        val chatUser = ChatUser(null, "john", "vedder")

        val truncateAndSave = template
                .truncate(ChatUser::class.java)
                .thenMany(Flux.just(chatUser))
                .flatMap(template::insert)

        val find = template
                .query(ChatUser::class.java)
                .one()

        val composed = Flux
                .from(truncateAndSave)
                .then(find)

        StepVerifier
                .create(composed)
                .expectSubscription()
                .assertNext(this::userAssertions)
                .verifyComplete()
    }

    @Test
    fun shouldPerformTruncateAndSave() {
        val chatUser = ChatUser(null, "john", "vedder")

        val truncateAndSave = template
                .truncate(ChatUser::class.java)
                .thenMany(
                        Flux.just(chatUser)
                )
                .flatMap(template::insert)

        StepVerifier
                .create(truncateAndSave)
                .expectSubscription()
                .assertNext(this::userAssertions)
                .verifyComplete()

    }

    // helper function to verify user state
    fun userAssertions(user: ChatUser) {
        assertAll("User Assertion",
                { assertNotNull(user) },
                { assertNotNull(user.id) },
                { assertNotNull(user.handle) },
                { assertEquals("vedder", user.handle) },
                { assertEquals("john", user.name) }
        )
    }
}

@BeforeAll
fun setUp() {
    try {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(10000)
    } catch (t :NullPointerException) {
        DatabaseDescriptor.daemonInitialization()
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(10000)
    }
}