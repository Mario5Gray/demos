package com.demo.makeredisgreat

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootApplication(exclude = [RedisAutoConfiguration::class])
class MakeRedisGreatApplication {

    val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(MakeRedisGreatApplication::class.java)

    val geoKey = "Murica"
    val topicKey = "geoTopic"
    val queueKey = "geoQueue"

    val ringList = listOf(
            RingGeo(Ring("1", "Gold", "3+"), 13.361389, 38.115556),
            RingGeo(Ring("2", "Silver", "3+"), 15.087269, 37.502669),
            RingGeo(Ring("3", "Bronze", "3+"), 13.583333, 37.316667)
    )

    fun titledRunner(title: String, ar: ApplicationRunner): ApplicationRunner {
        log.info(":::_" + title.toUpperCase() + "_:::")
        return ApplicationRunner { args -> ar.run(args) }
    }

    @Bean
    fun cacheOpsDemo(ringCache: ReactiveRedisTemplate<String, Ring>,
                     idCache: ReactiveRedisTemplate<String, String>): ApplicationRunner {
        val latch = CountDownLatch(1)
        return titledRunner("GEO-PUSH",
                ApplicationRunner {
                    val geoOps = idCache.opsForGeo()
                    val ringOps = ringCache.opsForValue()

                    val circle = Circle(Point(13.583333, 37.316667), Distance(100.toDouble(), RedisGeoCommands.DistanceUnit.KILOMETERS))
                    val pubsubProcessor: DirectProcessor<String> = DirectProcessor.create()

                    // TODO what is the keyword 'out'?
                    Flux.merge(
                            idCache.listenTo(ChannelTopic(topicKey))
                                    .flatMap {
                                        ringOps
                                                .get(it.message)
                                                .flatMap { ring ->
                                                    geoOps
                                                            .position(geoKey, ring.id)
                                                            .map { point -> RingGeo(ring, point.x, point.y) }
                                                }
                                                .doOnNext { ringGeo ->
                                                    log.info("TOPIC_RECV( ${this.topicKey} ) == $ringGeo")
                                                }
                                    }
                                    .doOnError { ex -> ex.printStackTrace() },

                            Flux
                                    .fromStream(ringList.stream())
                                    .flatMap { geo ->
                                        geoOps.add(geoKey, Point(geo.lat, geo.lon), geo.ring.id)
                                                .flatMap { ringOps.set(geo.ring.id, geo.ring) }
                                    }
                                    .thenMany(geoOps.radius(geoKey, circle))
                                    .doOnNext {
                                        pubsubProcessor.onNext(it.content.name)
                                    }
                                    .doOnComplete { latch.countDown() },

                            pubsubProcessor
                                    .publishOn(Schedulers.elastic())
                                    .onBackpressureDrop()
                                    .handle<String> { r, sink ->
                                        sink.next(r)
                                    }
                                    .flatMap { id ->
                                        Flux.merge(
                                                idCache.convertAndSend(topicKey, id),
                                                idCache.opsForList().leftPush(queueKey, id)
                                        )
                                    }
                    )
                            .doOnError { ex -> ex.printStackTrace() }
                            .subscribe()

                    latch.await(5, TimeUnit.SECONDS)
                })
    }

    @Bean
    fun blPopDemo(ringCache: ReactiveRedisTemplate<String, Ring>,
                  idCache: ReactiveRedisTemplate<String, String>): ApplicationRunner {
        val latch = CountDownLatch(1)

        val geoOps = idCache.opsForGeo()
        val ringOps = ringCache.opsForValue()

        return titledRunner("GEO-RECV", ApplicationRunner {

            idCache.opsForList()
                    .leftPop(queueKey, Duration.ofSeconds(60))
                    .doOnError { ex -> ex.printStackTrace() }
                    .flatMapMany { ringId ->
                        Flux
                                .zip(ringOps.get(ringId), geoOps.position(geoKey, ringId))
                                .map { RingGeo(it.t1, it.t2.x, it.t2.y) }
                    }
                    .doOnNext { log.info("BLPOP: $it") }
                    .doOnComplete { latch.countDown() }
                    .subscribe()

            latch.await(60, TimeUnit.SECONDS)
        })
    }
}

fun main(args: Array<String>) {
    runApplication<MakeRedisGreatApplication>(*args)
}
