package com.example.buoy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.boot.actuate.info.EnvironmentInfoContributor
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.LivenessState
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootApplication
class BuoyApplication {
    private val latch = CountDownLatch(2)

    @Value("\${demo.app.message:Hello}")
    private lateinit var message: String

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Bean
    fun route() = router {
        GET("/count") {
            latch.countDown()
            ServerResponse
                    .ok()
                    .body(Mono.just(latch.count), Long::class.java)
        }
    }

    @Bean
    fun infoContributor() = InfoContributor { builder ->
        builder.withDetail("example", mapOf(Pair("key", "value")))
    }

    @Bean
    fun myHealthIndicator() = ReactiveHealthIndicator {
        when (latch.count) {
            1L -> Mono.just(Health.down().build())
            else -> Mono.just(Health.up().build())
        }

    }

    @Bean
    fun latchDepletion() = CommandLineRunner {
        Executors.newSingleThreadExecutor()
                .execute {
                    latch.await()
                    AvailabilityChangeEvent.publish(this.eventPublisher, Exception("Countdown latch depletion"), LivenessState.BROKEN)
                }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<BuoyApplication>(*args)
        }
    }
}