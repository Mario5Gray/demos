package com.demo.chatservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
class UserDomainTests {

    @Test
    fun testShouldUserCreateAndReactivate() {
        val user: ChatUser = ChatUser(123456L, "John", "JohnsHandle")

        assertAll("user",
                { assertNotNull(user) },
                { assertEquals(123456L, user.id) },
                { assertEquals("John", user.name) },
                { assertEquals("JohnsHandle", user.handle) })

        StepVerifier
                .create(Flux.just(user))
                .assertNext { u ->
                    assertAll("user",
                            { assertNotNull(u) },
                            { assertEquals(123456L, u.id) }
                    )
                }
                .verifyComplete()
    }
}