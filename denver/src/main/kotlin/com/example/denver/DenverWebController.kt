package com.example.denver

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class DenverWebController(val service: MessageService) {

    @Bean
    fun getRoutes(): RouterFunction<ServerResponse> = router {
        GET("/get") {
            ServerResponse
                    .ok()
                    .body(service.get("demo"), Message::class.java)
        }
    }
}