package com.demo.makeredisgreat

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import redis.embedded.RedisExecProvider
import redis.embedded.RedisServer
import redis.embedded.util.Architecture
import redis.embedded.util.OS

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = ["classpath:application.properties"])
class RedisDemoTests {

    private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(RedisDemoTests::class.java)

    private val port = 6777

    @Value("\${redis.test.server.exec}")
    private lateinit var redisPath: String

    private lateinit var redisServer: RedisServer

    private lateinit var lettuce: LettuceConnectionFactory

    @BeforeAll
    fun setupRedis() {
        val customProvider: RedisExecProvider = RedisExecProvider
                .defaultProvider()
                .override(OS.UNIX, redisPath)
                .override(OS.WINDOWS, Architecture.x86, redisPath)
                .override(OS.WINDOWS, Architecture.x86_64, redisPath)
                .override(OS.MAC_OS_X, Architecture.x86, redisPath)
                .override(OS.MAC_OS_X, Architecture.x86_64, redisPath)

        redisServer = RedisServer(customProvider, port)

        log.info("Starting Redis server on ::$port")

        redisServer.start()

        lettuce = LettuceConnectionFactory(
                RedisStandaloneConfiguration("127.0.0.1", port))
        lettuce.afterPropertiesSet()


    }

    @AfterAll
    fun tearDown() {
        redisServer.stop()
    }

    private val idMatcher: Matcher<String> = Matchers
            .allOf(
                    Matchers.notNullValue(),
                    Matchers.equalToIgnoringCase("1234")
            )

    @Test
    fun testRedisConnection() {
        val template = ReactiveStringRedisTemplate(lettuce)

        lettuce.validateConnection()

        val ping = template
                .connectionFactory
                .reactiveConnection
                .ping()

        StepVerifier
                .create(ping)
                .expectSubscription()
                .expectNext("PONG")
                .verifyComplete()
    }

    @Test
    fun redisExecuteGet() {
        val idCache = ReactiveStringRedisTemplate(lettuce)

        val cachePut: Publisher<Boolean> =
                idCache.opsForValue()
                        .set("TEST", "1234")

        val cacheGet: Publisher<String> =
                idCache.opsForValue()
                        .get("TEST")

        val setAndGet = Flux.from(cachePut)
                .thenMany(cacheGet)

        StepVerifier
                .create(setAndGet)
                .expectSubscription()
                .assertNext { t ->
                    MatcherAssert.assertThat("Receives Value for Key",
                            t, idMatcher)
                }
                .verifyComplete()
    }

}
