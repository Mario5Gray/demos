package com.demo.chatservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class UserDomainTests {

    @Test
    fun testShouldUserCreateAndReactivate() {
        val uuid = UUID.randomUUID()
        val user = ChatUser(uuid, "Eddie", "EddiesHandle", Time.valueOf(LocalTime.now()))

        assertAll("user",
                { assertNotNull(user) },
                { assertEquals(uuid, user.id) },
                { assertEquals("Eddie", user.name) },
                { assertEquals("EddiesHandle", user.handle) })

        StepVerifier
                .create(Flux.just(user))
                .assertNext { u ->
                    assertAll("simple user assertion",
                            { assertNotNull(u) },
                            { assertEquals(uuid, u.id) }
                    )
                }
                .verifyComplete()
    }
}