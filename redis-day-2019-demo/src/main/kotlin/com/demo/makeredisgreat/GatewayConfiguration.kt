package com.demo.makeredisgreat

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import reactor.core.publisher.Mono


@Bean
fun ringKeyResolver(): KeyResolver = KeyResolver {
    Mono.just(it.request.queryParams.getFirst("ring").orEmpty())
}