package com.streamy.orders.service

import com.fasterxml.jackson.annotation.ObjectIdGenerators
import org.springframework.beans.propertyeditors.UUIDEditor
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface OrderService {
    fun allOrders(): Flux<OrderEvent>
    fun saveOrder(order: OrderEvent): Mono<OrderEvent>
}

open class OrderServiceXStream(val template: ReactiveRedisTemplate<String, OrderEvent>) : OrderService {
    override fun allOrders(): Flux<OrderEvent> = template
            .opsForStream<String, OrderEvent>()
            .read(OrderEvent::class.java, StreamOffset.fromStart("orders"))
            .map {
                OrderEvent(
                        UUID(it.id.timestamp!!, it.id.sequence!!),
                        it.value.item,
                        it.value.count)
            }
            .checkpoint("all")

    override fun saveOrder(order: OrderEvent): Mono<OrderEvent> = template
            .opsForStream<String, OrderEvent>()
            .add(StreamRecords.newRecord()
                    .`in`("orders")
                    .ofObject(order)
                    .withId(RecordId.autoGenerate()))
            .map {
                OrderEvent(UUID(it.timestamp!!, it.sequence!!), order.item, order.count)
            }
            .checkpoint("save")
}