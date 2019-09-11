package com.example.mongoembeddeddemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean

@SpringBootApplication
class MongoEmbeddedDemoApplication {
    @Bean
    fun beanRestTemplate() = RestTemplateBuilder().build()!!

//    @Bean
//    fun dataBeans( repo: UserRepository) : ApplicationRunner = ApplicationRunner {
//        repo.deleteAll()
//                .thenMany(repo.saveAll(Flux.just(
//                        User(null, "Mario"),
//                        User(null, "Lilo")
//                )))
//                .blockLast()
//    }

}

fun main(args: Array<String>) {
    runApplication<MongoEmbeddedDemoApplication>(*args)
}



