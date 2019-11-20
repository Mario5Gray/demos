package com.streamy.orders.client

import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

data class OrderEvent(val id: UUID?, val item: String, val count: Int)

class OrdersClient(private val requester: RSocketRequester) {
    fun allOrders(): Flux<OrderEvent> =
            requester
                    .route("all")
                    .retrieveFlux(OrderEvent::class.java)



    fun saveOrder(order: OrderEvent): Mono<Void> =
            requester
                    .route("save")
                    .data(Mono.just(order), OrderEvent::class.java)
                    .retrieveMono(Void::class.java)
                    .timeout(Duration.ofMillis(10000))

}