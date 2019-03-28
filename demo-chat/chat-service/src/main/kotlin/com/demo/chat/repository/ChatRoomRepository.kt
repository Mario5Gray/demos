package com.demo.chat.repository

import com.demo.chat.domain.ChatRoom
import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux
import java.util.*


interface ChatRoomRepository :
        ReactiveCassandraRepository<ChatRoom, UUID> {
    @AllowFiltering
    fun findByName(name: String): Flux<ChatRoom>
}
