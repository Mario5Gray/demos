package com.demo.chat.service

import com.demo.chat.domain.ChatMessage
import com.demo.chat.domain.ChatRoom
import com.demo.chat.domain.ChatUser
import com.demo.chat.repository.ChatMessageRepository
import com.demo.chat.repository.ChatRoomRepository
import com.demo.chat.repository.ChatUserRepository
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.ColumnName
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import reactor.core.publisher.toFlux
import java.sql.Time
import java.time.LocalTime
import java.util.*

open class ChatException(msg: String) : Exception(msg)
object UserNotFoundException : ChatException("User not Found")
object RoomNotFoundException : ChatException("Room not Found")

@Component
class ChatService(val userRepo: ChatUserRepository,
                  val roomRepo: ChatRoomRepository,
                  val messageRepo: ChatMessageRepository) {

    fun newUser(handle: String, name: String): Mono<ChatUser> =
            userRepo
                    .insert(ChatUser(UUID.randomUUID(),
                            handle,
                            name,
                            Time.valueOf(LocalTime.now())
                    ))

    fun newRoom(uid: UUID, name: String): Mono<ChatRoom> = userRepo.findById(uid)
            .switchIfEmpty { Mono.error(ChatException("user not found")) }
            .flatMap {
                roomRepo
                        .insert(ChatRoom(UUID.randomUUID(), name, emptySet(), Date()))
            }

    fun joinRoom(uid: UUID, roomId: UUID): Mono<Boolean> = Mono
            .from(findRoomAndUser(uid, roomId))
            .flatMap {
                roomRepo.joinRoom(uid, roomId)
            }
            .defaultIfEmpty(false)


    fun leaveRoom(uid: UUID, roomId: UUID): Mono<Boolean> = Mono
            .from(findRoomAndUser(uid, roomId))
            .flatMap {
                roomRepo.leaveRoom(uid, roomId)
            }
            .defaultIfEmpty(false)

    fun sendMessage(uid: UUID, roomId: UUID, messageText: String): Mono<ChatMessage> = Mono
            .from(findRoomAndUser(uid, roomId))
            .flatMap {
                messageRepo
                        .insert(ChatMessage(UUID.randomUUID(),
                                uid,
                                roomId,
                                messageText,
                                Time.valueOf(LocalTime.now()),
                                true))
            }

    fun getMessagesForRoom(uid: UUID, roomId: UUID): Flux<ChatMessage> = Mono
            .from(findRoomAndUser(uid, roomId))
            .toFlux()
            .flatMap {
                messageRepo.findByRoomId(roomId)
            }

    private fun findRoomAndUser(uid: UUID, roomId: UUID) = Flux.zip(
            userRepo.findById(uid).switchIfEmpty { System.out.println("UserNotFoundException"); Mono.error(UserNotFoundException) },
            roomRepo.findById(roomId).switchIfEmpty { Mono.error(RoomNotFoundException) })

}