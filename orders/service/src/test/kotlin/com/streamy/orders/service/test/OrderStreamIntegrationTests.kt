package com.streamy.orders.service.test

import com.streamy.orders.service.OrderEvent
import com.streamy.orders.service.OrderService
import com.streamy.orders.service.OrderServiceXStream
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(OrderStreamIntegrationTests.StreamIntegrationConfiguration::class)
class OrderStreamIntegrationTests : IntegrationTestBase() {
    @Autowired
    private lateinit var service: OrderService

    @Test
    fun `should save an order and receive an order`() {
        val order = OrderEvent(UUID.randomUUID(), "BEANS", 1)

        val svcStream = service
                .saveOrder(order)
                .thenMany(
                        service.allOrders()
                )
                .groupBy {
                    it.item
                }
                .flatMap {
                    it.collectList()
                }


        StepVerifier
                .create(svcStream)
                .assertNext { ordersList ->
                    ordersList.forEach { order ->
                        println("EACH : $order")
                        Assertions
                                .assertThat(order)
                                .isNotNull
                                .hasNoNullFieldsOrProperties()
                                .hasFieldOrPropertyWithValue("item", "BEANS")
                    }
                }
                .verifyComplete()
    }

    @Configuration
    class StreamIntegrationConfiguration {
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

        @Bean
        fun orderService(tpl: ReactiveRedisTemplate<String, OrderEvent>) = OrderServiceXStream(tpl)
    }
}