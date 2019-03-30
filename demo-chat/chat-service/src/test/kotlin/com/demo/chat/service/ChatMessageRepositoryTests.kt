package com.demo.chat.service

import com.demo.chat.ChatServiceApplication
import com.demo.chat.domain.ChatMessage
import com.demo.chat.domain.ChatMessageKey
import com.demo.chat.repository.ChatMessageRepository
import org.cassandraunit.spring.CassandraDataSet
import org.cassandraunit.spring.CassandraUnit
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(CassandraConfiguration::class, ChatServiceApplication::class)
@CassandraUnit
@TestExecutionListeners(CassandraUnitDependencyInjectionTestExecutionListener::class, DependencyInjectionTestExecutionListener::class)
@CassandraDataSet("simple-message.cql")
class ChatMessageRepositoryTests {

    @Autowired
    lateinit var repo: ChatMessageRepository

    @Test
    fun testShouldSaveFindByRoomId() {
        val userId = UUID.randomUUID()
        val roomId = UUID.randomUUID()
        val msgId = UUID.randomUUID()

        val saveMsg = repo.insert(ChatMessage(ChatMessageKey(msgId, userId, roomId, Date()), "Welcome", true))
        val findMsg = repo.findByRoomId(roomId)

        val composite = Flux
                .from(saveMsg)
                .thenMany(findMsg)

        StepVerifier
                .create(composite)
                .assertNext(this::chatMessageAssertion)
                .verifyComplete()
    }

    @Test
    fun testShouldSaveFindMessagesByUserId() {
        val userId = UUID.randomUUID()

        val chatMessageFlux = Flux
                .just(
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), userId, UUID.randomUUID(), Date()), "Welcome", true),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Date()), "Welcome", true),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), userId, UUID.randomUUID(), Date()), "Welcome", true),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Date()), "Welcome", false),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), userId, UUID.randomUUID(), Date()), "Welcome", true),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Date()), "Welcome", true),
                        ChatMessage(ChatMessageKey(UUID.randomUUID(), userId, UUID.randomUUID(), Date()), "Welcome", false)
                ).delayElements(Duration.ofSeconds(2))

        val saveMessages = repo.insert(chatMessageFlux)
        val findMessages = repo.findByUserId(userId)
        val composite = Flux
                .from(saveMessages)
                .thenMany(findMessages)

        StepVerifier
                .create(composite)
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete()
    }

    fun chatMessageAssertion(msg: ChatMessage) {
        assertAll("message contents in tact",
                { Assertions.assertNotNull(msg) },
                { Assertions.assertNotNull(msg.key.id) },
                { Assertions.assertNotNull(msg.key.userId) },
                { Assertions.assertNotNull(msg.key.roomId) },
                { Assertions.assertNotNull(msg.text) },
                { Assertions.assertEquals(msg.text, "Welcome") },
                { Assertions.assertTrue(msg.visible) }
        )
    }
}