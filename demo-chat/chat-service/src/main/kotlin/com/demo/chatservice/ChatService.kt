package com.demo.chatservice

import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.ColumnName
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.sql.Time
import java.time.LocalTime
import java.util.*

@Component
class ChatService(val userRepo: ChatUserRepository,
                  val roomRepo: ChatRoomRepository,
                  val template: ReactiveCassandraTemplate) {
    fun newUser(handle: String, name: String): Mono<ChatUser> =
            userRepo
                    .insert(ChatUser(UUID.randomUUID(),
                            handle,
                            name,
                            Time.valueOf(LocalTime.now())
                    ))

    fun joinRoom(uid: UUID, roomId: UUID): Mono<Boolean> = Mono
            .zip(roomRepo.findById(roomId), userRepo.findById(uid))
            .filter {
                it.t1 != null && it.t2 != null
            }
            .flatMap {
                template
                        .update(Query.query(where("id").`is`(roomId)),
                                Update.of(listOf(Update.AddToOp(
                                        ColumnName.from("members"),
                                        listOf(uid),
                                        Update.AddToOp.Mode.APPEND))),
                                ChatRoom::class.java
                        )
            }
            .defaultIfEmpty(false)


    fun leaveRoom(uid: UUID, roomId: UUID): Mono<Boolean> = Mono
            .zip(roomRepo.findById(roomId), userRepo.findById(uid))
            .filter {
                it.t1 != null && it.t2 != null
            }
            .flatMap {
                template
                        .update(Query.query(where("id").`is`(roomId)),
                                Update.of(listOf(Update.RemoveOp(
                                        ColumnName.from("members"),
                                        listOf(uid)))),
                                ChatRoom::class.java
                        )
            }

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