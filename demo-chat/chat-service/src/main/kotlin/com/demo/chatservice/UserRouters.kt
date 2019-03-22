package com.demo.chatservice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import java.sql.Time
import java.time.LocalTime
import java.util.*

@Configuration
class UserRouters {

    data class UserResponse(val token: UUID, val handle: String, val timestamp: Date)

    @Bean
    fun routes(repo: ChatUserCrudRepository): RouterFunction<ServerResponse> = router {
            GET("/newuser") { req ->
                ServerResponse
                        .ok()
                        .body(repo
                                .insert(ChatUser(UUID.randomUUID(),
                                        req.queryParam("handle").orElseThrow { Exception("User Not Found") },
                                        req.queryParam("name").orElse("Spring"),
                                        Time.valueOf(LocalTime.now())
                                ))
                                .map {
                                    UserResponse(it.id, it.handle, it.timestamp)
                                }
                        )
            }

        }
    }

