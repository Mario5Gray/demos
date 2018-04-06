package com.example.actuatorclassic

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.endpoint.Endpoint
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.metrics.CounterService
import org.springframework.boot.actuate.metrics.GaugeService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.TimeUnit


@SpringBootApplication
class ActuatorClassicApplication

fun main(args: Array<String>) {
    SpringApplication.run(ActuatorClassicApplication::class.java, *args)
}

@Configuration
class MetricsConfiguration {


    //@Bean
    fun graphiteReporter(
            registry: MetricRegistry,
            @Value("\${HOSTEDGRAPHITE_APIKEY}") apiKey: String,
            @Value("\${HOSTEDGRAPHITE_URL}") host: String,
            @Value("\${HOSTEDGRAPHITE_PORT}") port: Int): GraphiteReporter {

        println("${apiKey} for ${host} and ${port}")
        val reporter =
                GraphiteReporter
                        .forRegistry(registry)
                        .prefixedWith(apiKey)
                        .build(Graphite(host, port))
        reporter.start(2, TimeUnit.SECONDS)
        return reporter
    }
}

@Component
class MarioHealthIndicator : HealthIndicator {

    override fun health(): Health = Health.status("I <3 Production!!").build()

}

@RestController
class GreetingsRestController(val gs: GaugeService, val cs: CounterService) {

    @GetMapping("/hi")
    fun hi(): String {
        gs.submit("histogram.demo", System.currentTimeMillis().toDouble())
        cs.increment("meter.winning")
        gs.submit("mario-awesomeness", Math.max(Math.random() * 1000,
                Math.random() * 1000))
        return "Hello, world"
    }
}

@Component
class CustomEndpoint : Endpoint<List<String>> {

    override fun getId(): String = "customendpoint";

    override fun isEnabled(): Boolean = true;

    override fun isSensitive(): Boolean = true;

    //@Autowired
    //lateinit var counterService: CounterService;

    override operator fun invoke(): List<String> {
        // Custom logic to build the output
        val messages = ArrayList<String>()
        messages.add("This is message 1")
        messages.add("This is message 2")
        return messages
    }
}