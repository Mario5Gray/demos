package com.redisconf.demo

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.runApplication

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


}
