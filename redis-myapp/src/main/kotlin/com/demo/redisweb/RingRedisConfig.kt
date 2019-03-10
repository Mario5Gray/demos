package com.demo.redisweb

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * entites stored by ID
 *
 * geo ID -> Lat, Lon
 * KV  ID -> Ring
 *
 */
@Configuration
class RingRedisConfig {

    @Bean
    fun redisConnectionFactory(): ReactiveRedisConnectionFactory = LettuceConnectionFactory()

    @Bean
    fun ringCache(cf: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Ring> {
        val keys = StringRedisSerializer()
        val values = Jackson2JsonRedisSerializer(Ring::class.java)
        values.setObjectMapper(jacksonObjectMapper())           // KOTLIN USERS : use setObjectMapper!

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Ring> =
                RedisSerializationContext.newSerializationContext(keys)

        builder.key(keys)
        builder.value(values)
        builder.hashKey(keys)
        builder.hashValue(values)

        return ReactiveRedisTemplate(cf, builder.build())
    }


}