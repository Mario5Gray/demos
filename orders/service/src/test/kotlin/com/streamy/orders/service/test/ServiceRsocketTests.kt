package com.streamy.orders.service.test

import com.streamy.orders.service.OrderEvent
import com.streamy.orders.service.OrderService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.stereotype.Controller
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(SpringExtension::class)
@Import(ServiceRsocketTests.ServiceRsocketTestConfiguration::class)
class ServiceRsocketTests : TestBaseRsocket() {

    @MockBean
    lateinit var orderService: OrderService

    @Test
    fun `should save an order`() {
        val anOrder = OrderEvent(UUID.randomUUID(), "BEANS", 20000)
        BDDMockito
                .given(orderService.saveOrder(anyObject()))
                .willReturn(Mono.just(anOrder))

        StepVerifier
                .create(requestor
                        .route("save")
                        .data(Mono.just(OrderEvent(null, "BEANS", 20000)), OrderEvent::class.java)
                        .retrieveMono(OrderEvent::class.java))
                .assertNext {
                    Assertions
                            .assertThat(it)
                            .isNotNull
                            .hasNoNullFieldsOrProperties()
                            .hasFieldOrPropertyWithValue("item", "BEANS")
                }
                .verifyComplete()
    }

    @Test
    fun `should serve an order`() {
        BDDMockito
                .given(orderService.allOrders())
                .willReturn(Flux.just(
                        OrderEvent(UUID.randomUUID(), "BEANS", 20000)
                ))

        StepVerifier
                .create(requestor
                        .route("all")
                        .data(Void::class.java)
                        .retrieveFlux(OrderEvent::class.java)
                )
                .assertNext {
                    Assertions
                            .assertThat(it)
                            .isNotNull
                            .hasNoNullFieldsOrProperties()
                            .hasFieldOrPropertyWithValue("item", "BEANS")
                }
                .verifyComplete()
    }

    @Configuration
    @Import(JacksonAutoConfiguration::class, RSocketStrategiesAutoConfiguration::class)
    class ServiceRsocketTestConfiguration() {
        @Bean
        fun messageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
            rSocketStrategies = strategies
            afterPropertiesSet()
        }

        @Bean
        fun controller(svc: OrderService) = OrderController(svc)
    }

    @Controller
    class OrderController(val service: OrderService) {
        @MessageMapping("all")
        fun allOrders(): Flux<OrderEvent> = service.allOrders()
        @MessageMapping("save")
        fun save(order: OrderEvent): Mono<OrderEvent> = service.saveOrder(order)
    }
}
