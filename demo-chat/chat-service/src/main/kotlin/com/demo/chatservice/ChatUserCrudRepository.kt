package com.demo.chatservice

import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import reactor.core.publisher.Mono

interface ChatUserCrudRepository : ReactiveCassandraRepository<ChatUser, Long> {

    @Query("SELECT * FROM chat_user WHERE handle = ?0")
    fun findByHandle(handleQuery: String): Mono<ChatUser>

    fun findById(id: String): Mono<ChatUser>

}