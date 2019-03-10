package com.demo.redisweb

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

open class IDGen(val serverId: Long) {

    val idCursor: AtomicLong = AtomicLong()

    public fun generateId(): String {
        val tmp = ( Instant.now().epochSecond shl 64)+
                (serverId.toLong() shl 32 ) +
                (idCursor.incrementAndGet() shl 16 )
        return Math.max(tmp, tmp * -1).toString()
    }
}

object ID: IDGen(1)