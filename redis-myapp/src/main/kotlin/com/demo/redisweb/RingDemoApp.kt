package com.demo.redisweb

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.geo.Point
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.publisher.Flux

@SpringBootApplication(exclude = [RedisAutoConfiguration::class])
class RingDemoApp {

    @ConditionalOnProperty(prefix = "my", value = "app.init", havingValue = "true")
    @Bean
    fun init(ringCache: ReactiveRedisTemplate<String, Ring>,
             idCache: ReactiveRedisTemplate<String, String>,
             @Value("\${my.key.geo}") keyGeo: String
    ): ApplicationRunner {
        return ApplicationRunner {

            val initFlux = Flux.concat(
                    idCache.connectionFactory.reactiveConnection.serverCommands()
                            .flushAll(),
                    Flux.fromStream(InitRings.stream())
                            .flatMap { geo ->
                                val id = ID.generateId()

                                ringCache.opsForValue()
                                        .set(id, Ring(id, geo.ring.alloy, geo.ring.size))
                                        .flatMap {
                                            idCache.opsForGeo()
                                                    .add(keyGeo, Point(geo.lat, geo.lon), id)
                                        }
                            }
            )

            initFlux
                    .doOnError(Throwable::printStackTrace)
                    .subscribe()
        }
    }

}

fun main(args: Array<String>) {
    reactor.core.publisher.Hooks.onOperatorDebug()
    runApplication<RingDemoApp>(*args)
}

