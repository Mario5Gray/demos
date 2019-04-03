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

				})
	}

}
