package com.demo.redisweb

import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Range
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.GeoResult
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.connection.RedisZSetCommands
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

/**
 * entites stored by ID
 *
 * geo ID -> Lat, Lon
 * KV  ID -> Ring
 *
 */
@Component
class RingService(@Autowired val ringCache: ReactiveRedisTemplate<String, Ring>,
                  @Autowired val idCache: ReactiveRedisTemplate<String, String>,
                  @Value("\${my.key.geo}") val keyGeo: String,
                  @Value("\${my.key.list}") val keyList: String,
                  @Value("\${my.key.topic}") val keyTopic: String) {

    fun findAll(): Flux<RingGeo> {
        val setOps = idCache.opsForZSet()
        val ringOps = ringCache.opsForValue()

        return setOps.range(keyGeo, Range.of(Range.Bound.unbounded(), Range.Bound.unbounded()))
                .flatMap { id ->
                    ringOps.get(id)
                            .map { ring -> RingGeo(ring, 0.0, 0.0) }
                }

    }

    fun findByPoint(lat: Double, lon: Double, dist: Double): Flux<RingGeo> {
        val geoOps = idCache.opsForGeo()
        val ringOps = ringCache.opsForValue()

        val circle = Circle(Point(lat, lon), Distance(dist, RedisGeoCommands.DistanceUnit.KILOMETERS))
        val geoSearch: Flux<GeoResult<RedisGeoCommands.GeoLocation<String>>> = geoOps.radius(keyGeo, circle)

        return Flux.from(geoSearch)
                .flatMap { geo ->
                    ringOps.get(geo.content.name)
                            .flatMap { ring ->
                                geoOps.position(keyGeo, ring.id)
                                        .map { point ->
                                            RingGeo(ring, point.x, point.y)
                                        }
                            }
                }
    }

    fun save(r: Publisher<RingGeo>): Flux<RingGeo> {
        val geoOps = idCache.opsForGeo()
        val valueOps = ringCache.opsForValue()
        val listOps = idCache.opsForList()

        return r.toFlux()
                .flatMap { ringGeo ->
                    val ring = ringGeo.ring
                    geoOps.add(keyGeo, Point(ringGeo.lat, ringGeo.lon), ring.id)
                            .thenMany(valueOps.set(ring.id, ring))
                            .thenMany(Flux.merge(idCache.convertAndSend(keyTopic, ring.id),
                                    listOps.leftPush(keyList, ring.id)))
                            .map { RingGeo(ring, ringGeo.lat, ringGeo.lon) }
                }
    }

}