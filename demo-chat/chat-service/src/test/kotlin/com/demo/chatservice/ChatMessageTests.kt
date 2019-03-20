package com.demo.chatservice

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.sql.Time
import java.time.LocalTime
import java.util.*

@ExtendWith(SpringExtension::class)
class ChatMessageTests {

    @Test
    fun testShouldHoldState() {
        val userUUID = UUID.randomUUID()
        val msgUUID = UUID.randomUUID()
        val roomUUID = UUID.randomUUID()

        val message = ChatMessage(msgUUID, userUUID, roomUUID, "Welcome", Time.valueOf(LocalTime.now()), true)

        StepVerifier
                .create(Flux.just(message))
                .assertNext(this::chatMessageAssertion)
                .verifyComplete()
    }

    fun chatMessageAssertion(msg: ChatMessage) {
        assertAll("message contents in tact",
                { assertNotNull(msg) },
                { assertNotNull(msg.id) },
                { assertNotNull(msg.userId) },
                { assertNotNull(msg.roomId) },
                { assertNotNull(msg.timestamp) },
                { assertNotNull(msg.text) },
                { assertEquals(msg.text, "Welcome") },
                { assertTrue(msg.visible) }
        )
    }
}