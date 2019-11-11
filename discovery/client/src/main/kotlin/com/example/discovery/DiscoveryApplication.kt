package com.example.discovery

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import java.util.*

// login to consul
// add a KV blob called data and fill it with the contents of your .yaml file
//
// /config/testApplication/data
// /config/testApplication,dev/data      # for profiles use ','
//
// See bootstrap.yaml for application bootstrap configurations.
//
//https://stackoverflow.com/questions/49880453/what-difference-does-enableconfigurationproperties-make-if-a-bean-is-already-an/49888642
@RefreshScope
@ConstructorBinding
@ConfigurationProperties("sample")
data class KVConfigurationProperty(val name: String, val id: UUID) {
	constructor() : this("nope", UUID.randomUUID())
}


@EnableConfigurationProperties(KVConfigurationProperty::class)
@SpringBootApplication
@RefreshScope
class DiscoveryApplication {
	private val log = LoggerFactory.getLogger(this::class.qualifiedName)

	@Value("\${name}")
	lateinit var name: String


	@Bean
	fun appRun(KVConfigurationProperty: KVConfigurationProperty
			   ): ApplicationRunner =
			ApplicationRunner {args ->
				log.info("SampleProp: ${KVConfigurationProperty.name} / ${KVConfigurationProperty.id}")
				log.info("name: ${name}")
			}
}

fun main(args: Array<String>) {
	runApplication<DiscoveryApplication>(*args)
}