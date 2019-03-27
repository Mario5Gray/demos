package com.demo.chatservice

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.cassandra.repository.query.CassandraEntityMetadata


@SpringBootApplication
class ChatServiceApplication {
    @Bean
    fun appRun(context: ApplicationContext) = ApplicationRunner {
        val metadata = context.getBeanNamesForType(CassandraEntityMetadata::class.java)
        System.out.println("IAMRUN")
        metadata.forEach { System.out.println("METADATA: $it") }
    }
}

fun main(args: Array<String>) {
    runApplication<ChatServiceApplication>(*args)
}

