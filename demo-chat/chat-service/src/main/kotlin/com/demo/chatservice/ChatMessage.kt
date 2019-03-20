package com.demo.chatservice

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.sql.Time
import java.util.*

@Table("chat_message")
data class ChatMessage(
        var id: UUID?,
        @Column("user_id")
        val userId: UUID,
        @PrimaryKeyColumn(name="room_id", type = PrimaryKeyType.CLUSTERED, ordinal = 1)
        val roomId: UUID,
        val text: String,
        val timestamp: Date,
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 2)
        val visible: Boolean
)
