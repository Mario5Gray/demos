package com.demo.chatservice

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table("chat_user")
data class ChatUser(
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
        var id: UUID? ,
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 3)
        val handle: String,
        val name: String
)
