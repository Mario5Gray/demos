package com.redisconf.demo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisDemoConfig {
    @Bean
    fun redisConnectionFactory(): ReactiveRedisConnectionFactory = LettuceConnectionFactory()

    @Bean
    fun someCache(cf: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Message> {
        val keys = StringRedisSerializer()
        val values = Jackson2JsonRedisSerializer(Message::class.java)
        values.setObjectMapper(jacksonObjectMapper())           // KOTLIN USERS : use setObjectMapper!

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Message> =
                RedisSerializationContext.newSerializationContext(keys)

        builder.key(keys)
        builder.value(values)
        builder.hashKey(keys)
        builder.hashValue(values)

        return ReactiveRedisTemplate(cf, builder.build())
    }

    @Bean
    fun stringCache(cf: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val keys = StringRedisSerializer()
        val values = StringRedisSerializer()

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, String> =
                RedisSerializationContext.newSerializationContext(keys)

        builder.key(keys)
        builder.value(values)
        builder.hashKey(keys)
        builder.hashValue(values)

        return ReactiveRedisTemplate(cf, builder.build())
    }
}