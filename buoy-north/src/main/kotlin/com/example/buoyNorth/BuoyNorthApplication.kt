package com.example.buoyNorth

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@SpringBootApplication
class BuoyNorthApplication {

    @Bean
    fun routers(@Value("\${message:default message}") message: String,
                @Value("\${countof:0}") countof: Integer) = router {
        GET("/message") {
            ServerResponse
                    .ok()
                    .body(Mono.just("${message} - ${countof}"), String::class.java)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<BuoyNorthApplication>(*args)
        }
    }
}