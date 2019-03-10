package com.demo.redisweb

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.*

@Configuration
class RingWebConfig {

    @Bean
    fun routes(repo: RingService): RouterFunction<ServerResponse> = router {
        GET("/loc") {
            ServerResponse
                    .ok()
                    .body(
                            repo.findByPoint(
                                    it.queryParam("lat").map { f -> f.toDouble() }.orElse(13.583333),
                                    it.queryParam("lon").map { f -> f.toDouble() }.orElse(37.316667),
                                    it.queryParam("dist").map { f -> f.toDouble() }.orElse(100.0)
                            ).map { r -> r.ring },
                            Ring::class.java)
        }

        POST("/new") { req ->
            req.bodyToMono(RingGeo::class.java)
                    .flatMap {
                        ServerResponse
                                .ok()
                                .body(repo
                                        .save(Mono.just(RingGeo(
                                                Ring(generateId(), it.ring.alloy, it.ring.size),
                                                it.lat,
                                                it.lon))),
                                        RingGeo::class.java
                                )
                    }
        }
    }

    private fun generateId(): String {
        val tmp = Random().nextLong()
        return Math.max(tmp, tmp * -1).toString()
    }

}