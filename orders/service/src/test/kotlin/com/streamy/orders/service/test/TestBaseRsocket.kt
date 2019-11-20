package com.streamy.orders.service.test

import io.rsocket.RSocketFactory
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.transport.netty.server.CloseableChannel
import io.rsocket.transport.netty.server.TcpServerTransport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.springframework.context.ApplicationContext
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import reactor.core.publisher.Hooks

fun <T> anyObject(): T {
    Mockito.anyObject<T>()
    return uninitialized()
}

fun <T> uninitialized(): T = null as T

open class TestBaseRsocket {
    lateinit var requestor: RSocketRequester
    lateinit var server: CloseableChannel

    @BeforeEach
    fun setUp(context: ApplicationContext) {
        val messageHandler = context.getBean(RSocketMessageHandler::class.java)
        val strategies = context.getBean(RSocketStrategies::class.java)

        server = RSocketFactory.receive()
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .acceptor(messageHandler.responder())
                .transport(TcpServerTransport.create("localhost", 0))
                .start()
                .block()!!

        requestor = RSocketRequester
                .builder()
                .rsocketStrategies(strategies)
                .connectTcp("localhost", server.address().port)
                .block()!!

        Hooks.onOperatorDebug()
    }

    @AfterEach
    fun tearDown() {
        requestor.rsocket().dispose()
        server.dispose()
    }
}