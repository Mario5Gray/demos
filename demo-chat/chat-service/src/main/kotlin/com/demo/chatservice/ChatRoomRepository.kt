package com.demo.chatservice

import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.ColumnName
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.where
import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation
import org.springframework.data.cassandra.repository.support.SimpleReactiveCassandraRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


interface ChatRoomRepository : ReactiveCassandraRepository<ChatRoom, UUID> {

    @AllowFiltering
    fun findByName(name: String): Flux<ChatRoom>
}

class ChatRoomRepositoryImpl( metadata: CassandraEntityInformation<ChatRoom, UUID>,
                              operations: ReactiveCassandraOperations,
                             val roomRepository: ChatRoomRepository,
                             val template: ReactiveCassandraTemplate) :
        SimpleReactiveCassandraRepository<ChatRoom, UUID>(metadata, operations) {

    fun joinRoom(uid: UUID, roomId: UUID): Mono<Boolean> =
            roomRepository.findById(roomId)
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

    fun leaveRoom(uid: UUID, roomId: UUID): Mono<Boolean> =
            roomRepository.findById(roomId)
                    .flatMap {
                        template
                                .update(Query.query(where("id").`is`(roomId)),
                                        Update.of(listOf(Update.RemoveOp(
                                                ColumnName.from("members"),
                                                listOf(uid)))),
                                        ChatRoom::class.java
                                        )
                    }
}