package com.demo.chat.service

import com.demo.chat.ChatServiceApplication
import com.demo.chat.domain.ChatRoom
import com.demo.chat.domain.ChatUser
import com.demo.chat.repository.ChatRoomRepository
import com.demo.chat.repository.ChatUserRepository
import org.cassandraunit.spring.CassandraDataSet
import org.cassandraunit.spring.CassandraUnit
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.sql.Time
import java.time.LocalTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(CassandraConfiguration::class, ChatServiceApplication::class)
@CassandraUnit
@TestExecutionListeners(CassandraUnitDependencyInjectionTestExecutionListener::class, DependencyInjectionTestExecutionListener::class)
@CassandraDataSet("simple-room.cql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatServiceTests {

    @Autowired
    lateinit var service: ChatService

    @Autowired
    lateinit var repo: ChatRoomRepository

    @Autowired
    lateinit var userRepo: ChatUserRepository

    // TODO: WHY no @MockBean here ? ? ? because CassandraUnit is getting in the way, eh?
    @org.springframework.context.annotation.Configuration
    class Configuration {
        @Bean
        fun userRepo(): ChatUserRepository = Mockito.mock(ChatUserRepository::class.java)
    }

    @BeforeEach
    fun setUp() {
        Mockito.`when`(userRepo.findById(anyObject<UUID>()))
                .thenReturn(Mono.empty())
    }

    @Test
    fun `should fail to leave a ficticious room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val saveRoomFlux = repo
                .insert(Flux.just(
                        ChatRoom(roomId, "XYZ", Collections.emptySet(), Time.valueOf(LocalTime.now()))
                ))

        val leaveFlux = service
                .leaveRoom(userId, roomId)

        val composite = Flux
                .from(saveRoomFlux)
                .then(leaveFlux)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .verifyComplete()
    }

    @Test
    fun `should join and leave a ficticious room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        Mockito.`when`(userRepo.findById(anyObject<UUID>()))
                .thenReturn(Mono.just(
                        ChatUser(userId, "handle", "name", Time.valueOf(LocalTime.now()))
                ))

        val saveFlux = repo
                .insert(Flux.just(
                        ChatRoom(roomId, "XYZ", Collections.emptySet(), Time.valueOf(LocalTime.now()))
                ))

        val joinFlux = service
                .joinRoom(userId, roomId)

        val leaveFlux = service
                .leaveRoom(userId, roomId)

        val composite = Flux
                .from(saveFlux)
                .then(joinFlux)
                .then(leaveFlux)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .assertNext(Assertions::assertTrue)
                .verifyComplete()
    }
}