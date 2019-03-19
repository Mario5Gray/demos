package com.demo.chatservice

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

@Table
class ChatUser(@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2) var id: Long? , @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3) val name: String, val handle: String)
