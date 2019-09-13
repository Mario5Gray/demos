package com.example.denver

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier
import redis.embedded.RedisServer
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DenverPersistenceTests {

    private val port = 9090
    private val hostname = "localhost"

    private lateinit var template: ReactiveStringRedisTemplate

    private lateinit var service: MessageService

    private  lateinit var redisServer: RedisServer

    @BeforeEach
    fun setUp() {
        redisServer = RedisServer(File("/usr/local/bin/redis-server"), port)

        redisServer.start()

        val lettuce = LettuceConnectionFactory(RedisStandaloneConfiguration(hostname, port))

        lettuce.afterPropertiesSet()

        ReactiveStringRedisTemplate(lettuce)
                .connectionFactory.reactiveConnection
                .serverCommands().flushAll()
                .block()

        template = ReactiveStringRedisTemplate(lettuce)

        service = MessageService(template)

        Hooks.onOperatorDebug()
    }

    @AfterAll
    fun tearDown() = redisServer.stop()

    @Test
    fun `should put a message`() {
        val putPublisher = service.put("demo", "Mario", "Demo Time")

        StepVerifier
                .create(putPublisher)
                .expectSubscription()
                .assertNext {
                    Assertions
                            .assertThat(it.mostSignificantBits)
                            .isGreaterThan(0)
                }
                .expectComplete()
                .verify()
    }
}