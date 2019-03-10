package com.demo.redisweb

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AnonymousAuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain


@Configuration
class SecurityConfiguration {

    @Bean
    fun anonymousManager(): ReactiveAuthenticationManagerAdapter {
        return ReactiveAuthenticationManagerAdapter(
                ProviderManager(
                        listOf(AnonymousAuthenticationProvider("ANON"))
                ))
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        return http
                .authorizeExchange()
                .pathMatchers("/*")
                .permitAll()
                .anyExchange()
                .permitAll()
                .and()
                .csrf().disable()
                .cors().disable()
                .build()
    }
}