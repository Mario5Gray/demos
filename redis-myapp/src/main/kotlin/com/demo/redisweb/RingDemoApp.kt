package com.demo.redisweb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [RedisAutoConfiguration::class])
class RingDemoApp

fun main(args: Array<String>) {
    reactor.core.publisher.Hooks.onOperatorDebug()
    runApplication<RingDemoApp>(*args)
}
