package com.demo.chatservice

import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.core.query.ColumnName
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.where
import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation
import org.springframework.data.cassandra.repository.support.SimpleReactiveCassandraRepository
import org.springframework.data.repository.NoRepositoryBean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


interface ChatRoomRepository : ReactiveCassandraRepository<ChatRoom, UUID> {
    @AllowFiltering
    fun findByName(name: String): Flux<ChatRoom>
}

@NoRepositoryBean
interface ChatRoomRepositoryBase<T> {

    fun leaveRoom(uid: UUID, roomId: UUID): Mono<Boolean>
    fun joinRoom(uid: UUID, roomId: UUID): Mono<Boolean>
}

class ChatRoomRepositoryBaseImpl<T>(
        private val metadata: CassandraEntityInformation<T, UUID>,
        private val operations: ReactiveCassandraOperations,
        private val template: ReactiveCassandraTemplate) :
        SimpleReactiveCassandraRepository<T, UUID>(metadata, operations),
        ChatRoomRepositoryBase<T> {

    override fun joinRoom(uid: UUID, roomId: UUID): Mono<Boolean> =
            findById(roomId)
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

    override fun leaveRoom(uid: UUID, roomId: UUID): Mono<Boolean> =
            findById(roomId)
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