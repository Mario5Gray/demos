package com.demo.chatservice

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table("chat_message")
data class ChatMessage(
        var id: UUID?,
        val user_id: UUID,
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
        val room_id: UUID,
        val text: String,
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 2)
        val visible: Boolean

)
