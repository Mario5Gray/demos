package com.demo.chat.service

import com.demo.chat.domain.ChatMessage
import com.demo.chat.domain.ChatRoom
import com.demo.chat.domain.ChatUser
import com.demo.chat.repository.ChatMessageRepository
import com.demo.chat.repository.ChatRoomRepository
import com.demo.chat.repository.ChatUserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.sql.Time
import java.time.LocalTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [ChatService::class])
@OverrideAutoConfiguration(enabled = true)
@ImportAutoConfiguration(classes = [ChatService::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
class ChatServiceTests {

    @Autowired
    lateinit var service: ChatService

    @MockBean
    lateinit var msgRepo: ChatMessageRepository

    @MockBean
    lateinit var roomRepo: ChatRoomRepository

    @MockBean
    lateinit var userRepo: ChatUserRepository


    @BeforeEach
    fun setUp() {
        val uid = UUID.randomUUID()
        val rid = UUID.randomUUID()

        val newUser = ChatUser(uid, "test-handle", "test-name", Date())
        val newRoom = ChatRoom(rid, "test-room", emptySet(), Date())
        val newMessage = ChatMessage(UUID.randomUUID(), uid, rid, "SUP TEST", Date(), true)

        Mockito.`when`(userRepo.findById(anyObject<UUID>()))
                .thenReturn(Mono.just(newUser))

        Mockito.`when`(userRepo.insert(anyObject<ChatUser>()))
                .thenReturn(Mono.just(newUser))

        Mockito.`when`(roomRepo.insert(anyObject<ChatRoom>()))
                .thenReturn(Mono.just(newRoom))

        Mockito.`when`(roomRepo.joinRoom(anyObject<UUID>(), anyObject<UUID>()))
                .thenReturn(Mono.just(true))

        Mockito.`when`(roomRepo.findById(anyObject<UUID>()))
                .thenReturn(Mono.just(newRoom))

        Mockito.`when`(roomRepo.leaveRoom(anyObject<UUID>(), anyObject<UUID>()))
                .thenReturn(Mono.just(true))

        Mockito.`when`(msgRepo.insert(anyObject<ChatMessage>()))
                .thenReturn(Mono.just(newMessage))

        Mockito.`when`(msgRepo.findByRoomId(anyObject<UUID>()))
                .thenReturn(Flux.just(newMessage))
    }

    @Test
    fun `should send and receive messages from room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val saveRoomFlux = service.newUser("Handle", "test")
                .doOnNext {
                    System.out.println("USER CREATED: ${it.id}")
                }

        val sendMessageFlux = service
                .sendMessage(userId, roomId, "Hello there")

        val saveAndSend = Mono
                .from(saveRoomFlux)
                .then(sendMessageFlux)

        StepVerifier
                .create(saveAndSend)
                .expectSubscription()
                .assertNext {
                    assertAll("message",
                            { assertEquals(it.userId, userId) },
                            { assertEquals(it.roomId, roomId) }
                    )
                }
    }

    @Test
    fun `should send message to room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val saveRoomFlux =
                roomRepo.save(
                        ChatRoom(UUID.randomUUID(), "TEST", emptySet(), Date())
                )

        val sendMessageFlux = service
                .sendMessage(userId, roomId, "Hello there")

        val saveAndSend = Mono
                .from(saveRoomFlux)
                .then(sendMessageFlux)

        StepVerifier
                .create(saveAndSend)
                .expectSubscription()
                .assertNext {
                    assertAll("message",
                            { assertEquals(it.userId, userId) },
                            { assertEquals(it.roomId, roomId) }
                    )
                }
    }

    @Test
    fun `should fail to leave a ficticious room`() {
        val roomId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val saveRoomFlux = roomRepo
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
                        ChatUser(userId, "handle", "name", Date())
                ))

        val saveFlux = roomRepo
                .insert(Flux.just(
                        ChatRoom(roomId, "XYZ", Collections.emptySet(), Date())
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