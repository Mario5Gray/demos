package com.streamy.orders.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration


fun <T> anyObject(): T {
    Mockito.anyObject<T>()
    return uninitialized()
}

fun <T> uninitialized(): T = null as T

class TestVoid()


@ExtendWith(SpringExtension::class)
class OrdersServiceTests {

    @MockBean
    private lateinit var service: OrderService

    @Test
    fun `should save and find one order`() {
        val order = Order(null, "WIDGETS", 1000)

        BDDMockito
                .given(service.save(anyObject()))
                .willReturn(Mono.just(order))

        StepVerifier
                .create(
                        service.save(order)
                )
                .expectSubscription()
                .assertNext {
                    Assertions
                            .assertThat(it)
                            .isNotNull
                }
                .verifyComplete()

    }
}