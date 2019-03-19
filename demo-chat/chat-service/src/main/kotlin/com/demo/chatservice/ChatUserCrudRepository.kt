package com.demo.chatservice

import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ChatUserCrudRepository : ReactiveCrudRepository<ChatUserCrudRepository, Long> {

    @Query("SELECT * FROM user WHERE handle = ?0")
    fun findByHandle(handleQuery: String): Mono<ChatUser>

    fun findById(id: String): Mono<ChatUser>

}