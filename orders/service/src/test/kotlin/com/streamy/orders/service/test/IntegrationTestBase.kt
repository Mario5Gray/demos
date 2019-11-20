package com.streamy.orders.service.test

import org.junit.jupiter.api.BeforeAll
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import reactor.core.publisher.Hooks
import redis.embedded.RedisServer


open class IntegrationTestBase {
    private lateinit var redisServer: RedisServer
    private lateinit var lettuce: LettuceConnectionFactory

    @BeforeAll
    fun setupRedisEmbedded() {
        //redisServer = RedisServer(File("/usr/local/bin/redis-server"), redisPort)
        //redisServer.start()

        Hooks.onOperatorDebug()
    }

}