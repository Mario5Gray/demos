package com.demo.chatservice

import org.cassandraunit.spring.CassandraDataSet
import org.cassandraunit.spring.CassandraUnit
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.sql.Time
import java.time.LocalTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(CassandraConfiguration::class, ChatServiceApplication::class)
@CassandraUnit
@TestExecutionListeners(CassandraUnitDependencyInjectionTestExecutionListener::class, DependencyInjectionTestExecutionListener::class)
@CassandraDataSet("simple-room.cql")
class ChatServiceTests {

    @Autowired
    lateinit var service: ChatService

    @Autowired
    lateinit var repo: ChatRoomRepository

    @Test
    fun `should join a ficticious room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val saveFlux = repo
                .insert(Flux.just(
                        ChatRoom(roomId, "XYZ", Collections.emptySet(), Time.valueOf(LocalTime.now()))
                ))

        val joinFlux = service
                .joinRoom(userId, roomId)

        val composite = Flux
                .from(saveFlux)
                .then(joinFlux)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .assertNext(Assertions::assertTrue)
                .verifyComplete()
    }
}