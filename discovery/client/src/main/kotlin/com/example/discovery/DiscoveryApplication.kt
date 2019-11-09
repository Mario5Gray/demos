package com.example.discovery

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

// login to consul
// add a KV
//
// /config/testApplication/sample/name : MARIO
//
//https://stackoverflow.com/questions/49880453/what-difference-does-enableconfigurationproperties-make-if-a-bean-is-already-an/49888642
@RefreshScope
@ConstructorBinding
@ConfigurationProperties("sample")
data class SampleProp(val name: String) {
	constructor() : this("nope")
}

@EnableConfigurationProperties(SampleProp::class)
@SpringBootApplication
class DiscoveryApplication {
	private val log = LoggerFactory.getLogger(this::class.qualifiedName)


	@Bean
	fun appRun(sampleProp: SampleProp): ApplicationRunner =
			ApplicationRunner {args ->
				log.info("SampleProp: ${sampleProp.name}")
			}
}

fun main(args: Array<String>) {
	runApplication<DiscoveryApplication>(*args)
}
