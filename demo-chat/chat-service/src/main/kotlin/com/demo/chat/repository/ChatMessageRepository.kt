package com.demo.chat.repository

import com.demo.chat.domain.ChatMessage
import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux
import java.util.*

interface ChatMessageRepository : ReactiveCassandraRepository<ChatMessage, UUID> {

    fun findByRoomId(roomId: UUID): Flux<ChatMessage>

    @AllowFiltering
    fun findByUserId(userId: UUID): Flux<ChatMessage>
}
