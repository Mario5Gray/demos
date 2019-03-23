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
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import redis.embedded.RedisServer
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

@DataRedisTest
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisDemoTests {
    val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(RedisDemoTests::class.java)

    private val port = 6777

    private lateinit var redisServer: RedisServer

    private lateinit var lettuce: LettuceConnectionFactory

    private lateinit var template: ReactiveRedisTemplate<String, String>

    @BeforeAll
    fun setupRedis() {
        redisServer = RedisServer(port)

        redisServer.start()

        lettuce = LettuceConnectionFactory(RedisStandaloneConfiguration("127.0.0.1", port))

        lettuce.afterPropertiesSet()

        template = ReactiveStringRedisTemplate(lettuce)
    }

    @AfterAll
    fun tearDown() = redisServer.stop()


    private val idMatcher: Matcher<String> = Matchers
            .allOf(
                    Matchers.notNullValue(),
                    Matchers.equalToIgnoringCase("1234")
            )

    @Test
    fun testShouldPing() {

        val ping = template.connectionFactory.reactiveConnection.ping()

        StepVerifier
                .create(ping)
                .expectSubscription()
                .expectNext("PONG")
                .verifyComplete()
    }

    @Test
    fun testShouldSetGet() {
        val cachePut: Publisher<Boolean> =
                template.opsForValue()
                        .set("TEST", "1234")

        val cacheGet: Publisher<String> =
                template.opsForValue()
                        .get("TEST")

        val setAndGet = Flux.from(cachePut).thenMany(cacheGet)

        StepVerifier
                .create(setAndGet)
                .expectSubscription()
                .assertNext { t ->
                    MatcherAssert.assertThat("Receives Value for Key", t, idMatcher)
                }
                .verifyComplete()
    }

    @Test
    fun testShouldPubSubSendReceive() {
        val topic = "TEST"

        val processor: DirectProcessor<String> = DirectProcessor.create()

        // destination for pubsubSub data
        val pubSubDataFlux = processor
                .onBackpressureBuffer()
                .handle<String> { r, sink ->
                    sink.next(r)
                }
                .publish()
                .autoConnect()
                .cache()

        // writes to channel
        val writerFlux = template.convertAndSend(topic, "1234")
                .repeat(1)
                .delaySubscription(Duration.ofSeconds(2)) // ENSURE channelListener is activated.

        val tcnt = AtomicLong(2)

        // Listen to channel
        val channelSubFlux = template
                .listenTo(ChannelTopic(topic))
                .doOnNext {
                    processor.onNext(it.message)
                }
                .handle<String> { _, sink ->
                    if (tcnt.decrementAndGet() == 0L) {
                        sink.complete()
                    }
                }

        Flux
                .merge(
                        channelSubFlux,
                        writerFlux
                )
                .delaySubscription(Duration.ofSeconds(1)) // ensure pubSubData is subscribed first
                .subscribe()


        StepVerifier
                .create(pubSubDataFlux
                        .take(2)
                        .doOnNext { log.info("N= $it") }
                )
                .expectSubscription()
                .assertNext { t ->
                    MatcherAssert.assertThat("Receives Value for Key", t, idMatcher)
                }
                .assertNext { t ->
                    MatcherAssert.assertThat("Receives Value for Key", t, idMatcher)
                }
                .expectComplete()
                .verify(Duration.ofSeconds(5))
    }
}