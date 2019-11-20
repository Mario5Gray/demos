package com.streamy.orders.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class OrderController(template: ReactiveRedisTemplate<String, OrderEvent>) : OrderServiceXStream(template) {
    @MessageMapping("all")
    override fun allOrders(): Flux<OrderEvent> = super.allOrders()

    @MessageMapping("save")
    override fun saveOrder(order: OrderEvent): Mono<OrderEvent> = super.saveOrder(order)
}

@Configuration
class ServiceConfiguration {
    @Bean
    fun lettuce() = LettuceConnectionFactory(RedisStandaloneConfiguration("localhost", 6379))

    @Bean
    fun template(lettuce: LettuceConnectionFactory): ReactiveRedisTemplate<String, OrderEvent> {
        val keySer = StringRedisSerializer()

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, OrderEvent> =
                RedisSerializationContext.newSerializationContext(keySer)

        builder.value(Jackson2JsonRedisSerializer(OrderEvent::class.java))
        return ReactiveRedisTemplate(lettuce, builder.build())
    }
}