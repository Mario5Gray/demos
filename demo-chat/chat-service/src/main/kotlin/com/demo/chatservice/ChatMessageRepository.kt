package com.demo.chatservice

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux
import java.util.*

interface ChatMessageRepository : ReactiveCassandraRepository<ChatMessage, UUID> {

    fun findByRoomId(roomId: UUID): Flux<ChatMessage>

    fun findByUserId(userId: UUID): Flux<ChatMessage>
}
