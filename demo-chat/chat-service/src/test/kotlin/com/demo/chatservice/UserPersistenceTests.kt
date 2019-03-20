package com.demo.chatservice

import org.apache.cassandra.config.DatabaseDescriptor
import org.cassandraunit.spring.*
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(CassandraConfiguration::class, ChatServiceApplication::class)
@CassandraUnit
@TestExecutionListeners(CassandraUnitDependencyInjectionTestExecutionListener::class, DependencyInjectionTestExecutionListener::class )
@CassandraDataSet("simple.cql")
class UserPersistenceTests {


    @Autowired
    lateinit var template: ReactiveCassandraTemplate

    @Test
    fun shouldContextLoad() {
        assertAll("Reactive Template Exists",
                { assertNotNull(template) } )
    }

    @Test
    fun shouldPerformSaveCrudFind() {
        val chatUser = ChatUser(null, "eddie", "vedder")

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

    //@Test
    fun shouldPerformTruncateAndSave(template: ReactiveCassandraTemplate) {
        val chatUser = ChatUser(null, "eddie", "vedder")

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
                { assertEquals("eddie", user.name) }
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