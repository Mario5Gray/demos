package com.demo.chatservice

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
    fun testShouldSaveFindById() {
        val userId = UUID.randomUUID()
        val roomId = UUID.randomUUID()
        val msgId = UUID.randomUUID()

        val saveMsg = repo.insert(ChatMessage(msgId, userId, roomId, "Welcome", true))
        val findMsg = repo.findByRoomId(roomId)

        val composite = Flux
                .from(saveMsg)
                .thenMany(findMsg)

        StepVerifier
                .create(composite)
                .assertNext(this::chatMessageAssertion)
                .verifyComplete()
    }

    fun chatMessageAssertion(msg: ChatMessage) {
        assertAll("message contents in tact",
                { Assertions.assertNotNull(msg) },
                { Assertions.assertNotNull(msg.id) },
                { Assertions.assertNotNull(msg.userId) },
                { Assertions.assertNotNull(msg.roomId) },
                { Assertions.assertNotNull(msg.text) },
                { Assertions.assertEquals(msg.text, "Welcome") },
                { Assertions.assertTrue(msg.visible) }
        )
    }
}