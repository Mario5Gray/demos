package com.demo.chatservice

import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux
import java.util.*

interface ChatRoomRepository : ReactiveCassandraRepository<ChatRoom, UUID> {

    @AllowFiltering
    fun findByName(name: String): Flux<ChatRoom>

}
