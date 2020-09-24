package com.streamy.orders.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderService {
    fun save(order: Order): Mono<Order>
    fun getAll(): Flux<Order>
}