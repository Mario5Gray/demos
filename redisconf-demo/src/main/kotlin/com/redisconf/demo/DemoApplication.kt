package com.redisconf.demo

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
class DemoApplication {

	fun main(args: Array<String>) {
		runApplication<DemoApplication>(*args)
	}


	val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DemoApplication::class.java)

	val geoKey = "messsageGeo"
	val topicKey = "messageTopic"
	val listKey = "messageInbox"

	val messageList = listOf(
			Mario,
			Luigi,
			Peach
	)

	fun titledRunner(title: String, ar: ApplicationRunner): ApplicationRunner {
		log.info(":::_" + title.toUpperCase() + "_:::")
		return ApplicationRunner { args -> ar.run(args) }
	}
	@Bean
	fun cacheSmokeDemo(messageCache: ReactiveRedisTemplate<String, Message>,
					   idCache: ReactiveRedisTemplate<String, String>): ApplicationRunner {

		val latch = CountDownLatch(4)       // we expect just 4 events to flow through processor
		return titledRunner("REDIS-DEMO",
				ApplicationRunner {

					val geoOps = idCache.opsForGeo()
					val ringOps = messageCache.opsForValue()
					val listOps = idCache.opsForList()

					val fanoutProcessor: DirectProcessor<String> = DirectProcessor.create()

					val flushFlux = idCache.connectionFactory
							.reactiveConnection
							.serverCommands()
							.flushAll()

					val processingFlux = fanoutProcessor
							.onBackpressureDrop()
							.handle<String> { m, sink ->
								sink.next(m)
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
														MessageGeo(ring, it.x, it.y)
													}
										}
										.doOnNext { messageGeo ->
											log.info("TOPIC( ${this.topicKey} ) == $messageGeo")
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
												.map { MessageGeo(it.t1, it.t2.x, it.t2.y) }
									}
									.doOnNext {
										log.info("LIST($listKey).pop == $it")
										latch.countDown()
									}
									.repeat(1)
							)

					val storeFlux = Flux
							.fromStream(messageList.stream())
							.flatMap { geo ->
								geoOps.add(geoKey, Point(geo.lat, geo.lon), geo.ring.id)
										.flatMap {
											ringOps.set(geo.ring.id, geo.ring)
										}
							}

					val searchFlux = Flux
							.from(geoOps.radius(geoKey, Circle(RandomPlace, RandomDistance)))
							.doOnNext {
								fanoutProcessor.onNext(it.content.name)
							}

					Flux
							.merge(processingFlux, readTopicFlux, readListFlux)
							.doOnError(Throwable::printStackTrace)
							.doOnComplete { log.info("READER Complete") }
							.subscribe()

					Flux
							.concat(flushFlux, storeFlux, searchFlux)
							.doOnError(Throwable::printStackTrace)
							.doOnComplete { log.info("WRITERS COMPLETE") }
							.subscribe()

					latch.await(5, TimeUnit.SECONDS)
				})
	}

}
