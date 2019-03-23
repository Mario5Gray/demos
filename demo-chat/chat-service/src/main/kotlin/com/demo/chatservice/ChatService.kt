package com.demo.chatservice

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.sql.Time
import java.time.LocalTime
import java.util.*

@Component
class ChatService(val userRepo: ChatUserRepository,
                  val roomRepo: ChatRoomRepositoryImpl) {


    fun newUser(handle: String, name: String): Mono<ChatUser> =
            userRepo
                    .insert(ChatUser(UUID.randomUUID(),
                            handle,
                            name,
                            Time.valueOf(LocalTime.now())
                    ))

    fun joinRoom(uid: String, roomId: String): Mono<Boolean> = roomRepo
            .joinRoom(UUID.fromString(uid), UUID.fromString(roomId))

    fun leaveRoom(uid: String, roomId: String): Mono<Boolean> = roomRepo
            .leaveRoom(UUID.fromString(uid), UUID.fromString(roomId))

    fun sendMessage(uid: String, roomId: String, messageText: String): Mono<ChatMessage> =
            Mono
                    .just(ChatMessage(UUID.randomUUID(),
                            UUID.fromString(uid),
                            UUID.fromString(roomId),
                            messageText,
                            Time.valueOf(LocalTime.now()),
                            true)
                    )

}