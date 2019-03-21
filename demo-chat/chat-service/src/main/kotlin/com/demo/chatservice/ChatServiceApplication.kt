package com.demo.chatservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories


@SpringBootApplication
class ChatServiceApplication

fun main(args: Array<String>) {
    runApplication<ChatServiceApplication>(*args)
}

//@Configuration
//@EnableBinding(Sink::class)
//class StreamConfiguration(val messageRepo: ChatMessageRepository) {
//    @StreamListener(Sink.INPUT)
//    fun handleMessage(message: ChatMessage) {
//        messageRepo.insert(message)
//
//    }
//}

@Configuration
@EnableReactiveCassandraRepositories( basePackageClasses = [ChatUser::class])
class ReactiveCassandraConfiguration

@Configuration
class CassandraConfiguration : AbstractReactiveCassandraConfiguration() {
    @Value("\${cassandra.contactpoints:127.0.0.1}")
    private lateinit var contactPoints: String
    @Value("\${cassandra.port:9142}")
    private lateinit var port: Integer
    @Value("\${cassandra.keyspace:chat}")
    private lateinit var keyspace: String
    @Value("\${cassandra.basepackages:com.demo.chatservice}")
    private lateinit var basePackages: String


    override fun getKeyspaceName(): String {
        return keyspace
    }

    override fun getContactPoints(): String {
        return contactPoints
    }

    override fun getPort(): Int {
        return port.toInt()
    }

    override fun getSchemaAction(): SchemaAction {
        return SchemaAction.NONE
    }

    override fun getEntityBasePackages(): Array<String> {
        return arrayOf(basePackages)
    }

    override
    fun cluster(): CassandraClusterFactoryBean {
        val cluster = super.cluster()
        cluster.setJmxReportingEnabled(false)
        return cluster
    }
}