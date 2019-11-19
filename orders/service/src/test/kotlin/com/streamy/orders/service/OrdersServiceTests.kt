package com.streamy.orders.service

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
import reactor.test.StepVerifier
import java.util.*

@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(OrdersServiceTests.ServiceRsocketTestConfiguration::class)
class OrdersServiceTests : RsocketTestBase() {

    @MockBean
    lateinit var orderService: OrderService

    @Test
    fun `should serve an order`() {
        BDDMockito
                .given(orderService.allOrders())
                .willReturn(Flux.just(
                        OrderEvent(UUID.randomUUID(),"BEANS",20000)
                ))

        StepVerifier
                .create(requestor
                        .route("all")
                        .data(Void::class.java)
                        .retrieveFlux(OrderEvent::class.java))
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
        fun ktModule() = KotlinModule()

        @Bean
        fun controller(svc: OrderService) = OrderController(svc)

    }

    @Controller
    class OrderController(val service: OrderService) {
        @MessageMapping("all")
        fun allOrders(): Flux<OrderEvent> = service.allOrders()
    }
}
