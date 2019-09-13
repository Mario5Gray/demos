package com.example.denver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.ReactiveStringRedisTemplate

@SpringBootApplication
class DenverApplication

fun main(args: Array<String>) {
	runApplication<DenverApplication>(*args)
}
