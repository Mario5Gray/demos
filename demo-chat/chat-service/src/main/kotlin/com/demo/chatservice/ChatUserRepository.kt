package com.demo.chatservice

import org.springframework.data.cassandra.repository.AllowFiltering
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface ChatUserRepository : ReactiveCassandraRepository<ChatUser, UUID> {

    @AllowFiltering
    fun findByHandle(handleQuery: String): Mono<ChatUser>

    @AllowFiltering
    fun findByName(handleQuery: String): Flux<ChatUser>

    fun findById(id: String): Mono<ChatUser>

}