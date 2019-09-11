package com.example.mongoembeddeddemo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux

data class User(var id: String?, val name: String)

interface UserRepository : ReactiveMongoRepository<User, String> {
    @Query("{name: {\$in: ['Mario', 'Lilo']} }")
    fun findFavorites(): Flux<User>
}

@Configuration
class UserWebConfig {
    @Bean
    fun restRoutes(repo: UserRepository): RouterFunction<ServerResponse> {
        return router {
            GET("/all") {
                ServerResponse
                        .ok()
                        .body(repo.findAll())
            }
            GET("/favorites") {
                ServerResponse
                        .ok()
                        .body(repo.findFavorites())
            }
        }
    }
}