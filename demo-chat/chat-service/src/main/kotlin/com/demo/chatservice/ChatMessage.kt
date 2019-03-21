package com.demo.chatservice

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table("chat_message")
data class ChatMessage(
        var id: UUID,
        @Column("user_id") val userId: UUID,
        @PrimaryKey("room_id")
        @PrimaryKeyColumn(name = "room_id", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
        val roomId: UUID,
        val text: String,
        val timestamp: Date,
        val visible: Boolean
)
