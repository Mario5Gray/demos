package com.demo.makeredisgreat

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootApplication(exclude = [RedisAutoConfiguration::class])
class RingDemoApp {

    val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(RingDemoApp::class.java)

    val geoKey = "ringGeo"
    val topicKey = "ringGeo"
    val listKey = "ringInbox"

    val ringList = listOf(
            Gold,
            Silver,
            Bronze
    )

    fun titledRunner(title: String, ar: ApplicationRunner): ApplicationRunner {
        log.info(":::_" + title.toUpperCase() + "_:::")
        return ApplicationRunner { args -> ar.run(args) }
    }

    @Bean
    fun cacheSmokeDemo(ringCache: ReactiveRedisTemplate<String, Ring>,
                       idCache: ReactiveRedisTemplate<String, String>): ApplicationRunner {

        val latch = CountDownLatch(4)
        return titledRunner("REDIS-DEMO",
                ApplicationRunner {

                    val geoOps = idCache.opsForGeo()
                    val ringOps = ringCache.opsForValue()
                    val listOps = idCache.opsForList()

                    val fanoutProcessor: DirectProcessor<String> = DirectProcessor.create()

                    val flushFlux = idCache.connectionFactory
                            .reactiveConnection
                            .serverCommands()
                            .flushAll()


                    val processingFlux = fanoutProcessor
                            .onBackpressureDrop()
                            .handle<String> { r, sink ->
                                sink.next(r)
                            }
                            .flatMap { id ->
                                Flux.merge(
                                        idCache.convertAndSend(topicKey, id),
                                        listOps.leftPush(listKey, id)
                                )
                            }

                    val readTopicFlux = idCache.listenTo(ChannelTopic(topicKey))
                            .flatMap { subscriber ->
                                ringOps
                                        .get(subscriber.message)
                                        .flatMap { ring ->
                                            geoOps.position(geoKey, ring.id)
                                                    .map {
                                                        RingGeo(ring, it.x, it.y)
                                                    }
                                        }
                                        .doOnNext { ringGeo ->
                                            log.info("TOPIC( ${this.topicKey} ) == $ringGeo")
                                            latch.countDown()
                                        }
                            }

                    val readListFlux = Flux
                            .from(listOps
                                    .leftPop(listKey, Duration.ofSeconds(2))
                                    .flatMapMany { ringId ->
                                        Flux.zip(
                                                ringOps.get(ringId),
                                                geoOps.position(geoKey, ringId)
                                        )
                                                .map { RingGeo(it.t1, it.t2.x, it.t2.y) }
                                    }
                                    .doOnNext { log.info("LIST($listKey).pop == $it"); latch.countDown() }
                                    .repeat(1)
                            )

                    val storeFlux = Flux
                            .fromStream(ringList.stream())
                            .flatMap { geo ->
                                geoOps.add(geoKey, Point(geo.lat, geo.lon), geo.ring.id)
                                        .flatMap {
                                            ringOps.set(geo.ring.id, geo.ring)
                                        }
                            }

                    val searchFlux =
                            Flux.from(geoOps.radius(geoKey, Circle(RandomPlace, RandomDistance)))
                                    .doOnNext {
                                        fanoutProcessor.onNext(it.content.name)
                                    }

                    val readers = Flux
                            .merge(processingFlux, readTopicFlux, readListFlux)
                            .doOnError(Throwable::printStackTrace)
                            .doOnComplete { log.info("READER Complete") }
                    readers.subscribe()

                    Flux
                            .concat(flushFlux, storeFlux, searchFlux)
                            .doOnError(Throwable::printStackTrace)
                            .doOnComplete { log.info("WRITERS COMPLETE") }
                            .subscribe()

                    latch.await(5, TimeUnit.SECONDS)
                })
    }
}

fun main(args: Array<String>) {
    reactor.core.publisher.Hooks.onOperatorDebug()
    runApplication<RingDemoApp>(*args)
}
