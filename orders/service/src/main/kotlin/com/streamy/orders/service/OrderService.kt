package com.streamy.orders.service

import reactor.core.publisher.Flux

interface OrderService {
    fun allOrders(): Flux<Order>
}
