# Let's Foray Into Production Ready Kubernetes Deployments With Spring Boot

The function for this demo is to illustrate configuring a Spring Boot
application for production-readiness with Kubernetes deployments. It will:

* Build a container with spring-boot/maven
* Expose liveness, readiness and custom heath data
* Deployment for kubernetes that utilizes these endpoints
* Application that exercises lifecycle state-changing

## Building the container

While not the main feature of this post, it should be known that building containers 
from build-tool alone (sans custom DOCKERFILE) is achievable without much
intervention. This will be described in a later blog post in detail, however
a good place to start with in discovering this feature are:

* [spring.io Post 1](https://spring.io/blog/2020/08/14/creating-efficient-docker-images-with-spring-boot-2-3)
* [spring.io Post 2](https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1)
* [The docs for maven](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/)
* [The docs for gradle](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)

### Setup Docker for local registry

I am using Docker as my container agent, so specifically I wanted to deploy locally 
without the need for dockerhub. This requires a little configuration, and as such I recommend 
the following Docker documentation:

* [Docker Docs](https://docs.docker.com/registry/)

This demo assumes a docker registry on port 5000, and is configured using this command:

```shell script
docker run -d -p 5000:5000 --name registry registry:2
```

Therefore, the 'deployment.yaml' would call for the image as follows:

```yaml
    spec:
      containers:
      - image: localhost:5000/buoy:latest
        name: buoy
        resources: {}
```

## liveness/readiness probe configuration

The following snippet from 'deployment.yaml' configures the liveness and rediness probes
for Kubernetes to know what state our lifecycle is in. 

```yaml
    spec:
      containers:
      - image: localhost:5000/buoy:latest
        name: buoy
        resources: {}
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 20
          failureThreshold: 2
          periodSeconds: 5
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          failureThreshold: 1
          periodSeconds: 5
```

In the future, I'll set the HttpGet path's to whatever the app has configured.

## Exposing Application liveness and readiness Endpoints

This application makes use of Actuator's Health endpoints (Web Root of `/actuator`) for giving lifecycle availability
state through an HTTP endpoint. Specifically - configuration for `application.properties` looks like:

```properties
management.endpoints.web.exposure.include=health,shutdown

management.endpoint.health.probes.enabled=true

management.endpoint.health.group.custom.include=my,diskSpace
management.endpoint.health.group.liveness.include=livenessState
management.endpoint.health.group.readiness.include=readinessState
```   

The two endpoints: 

* health - Application Lifecycle Availability 
* shutdown - Gracefully Shutdown the Instance

### For `/health` endpoints: Indicators

Here there are 3 health groups for the `/health` endpoint. These will be accessible by  `http://****/health/{liveness,readiness,custom}`.
 
The `/health` endpoint itself returns groups that are supplying indicator data. It also supplies aggregate status. 

The next 2 groups `liveness` and `readiness` use their designated Probes as state.

**Important**: if any (custom or otherwise) endpoints exposed then there MUST be some indicators
to feed them. In other words requesting status from a probe-less endpoint will result in 404 since nothing
is there to fill it.

Thus, the lines: 

```properties
management.endpoint.health.probes.enabled=true
management.endpoint.health.group.liveness.include=livenessState
management.endpoint.health.group.readiness.include=readinessState
``` 

are absolutely required to meet the minimal aggregation for HTTP-status 200 on `liveness` and `readiness`.

## The Application liveness/readiness

The application exposes an endpoint called `/count` that exhibits this behaviour:

* first GET request - bring `custom` health-indicator from 'UP' to 'DOWN' state
* second GET request - bring `liveness` health-indicator from 'UP' to 'DOWN' state

To do this, we put into service, the `CountdownLatch` that lets us block threads and read it's state atomically.
Using a `CommandLineRunner` which is executed at the final stages of application readiness, we can latch on to a 
single (ExecutorService bound) thread that will block until latch is 0, then publish 'BROKEN' to `livenessState` probe. 

```kotlin
    private val latch = CountDownLatch(2)

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Bean
    fun latchDepletion() = CommandLineRunner {
        Executors.newSingleThreadExecutor()
                .execute {
                    latch.await()
                    AvailabilityChangeEvent.publish(this.eventPublisher, Exception("Countdown latch depletion"), LivenessState.BROKEN)
                }
    }
```

### Custom Health info

This `my` Health indicator will submit 'broken' state when the countdown latch is 1.
Create a bean of type`HealthIndicator` or it's Reactive variant `ReactiveHealthIndicator`.
The Health value provided by this Indicator will be used as the `my` aggregate in health-groups.

**NOTE:** Non-reactive variants will be wrapped when using WebFlux.

Per documentation, this kind of bean can have a naming schema: '{indicator name}HealthIndicator'.

```kotlin
    @Bean
    fun myHealthIndicator() = ReactiveHealthIndicator {
        when (latch.count) {
            1L -> Mono.just(Health.down().build())
            else -> Mono.just(Health.up().build())
        }
    }
``` 

It can be wired into the `custom` group with the following property:

```properties
management.endpoint.health.group.custom.include=my
```

## Build the image

We automatically get the feature for buildpack image builds when using Spring Boot 2.3.0 or newer.
To build the image, simply execute:

```shell script
mvn clean spring-boot:build-image
```

which then lets us take said generated image and distribute it to our HUB, or local registry.

The command to do that:

```shell script
docker tag buoy:0.0.1-SNAPSHOT localhost:5000/buoy:latest
docker push
```

## Deploy to Kubernetes

Finally, we can deploy the application to our cluster.

Execute:

```shell script
kubectl apply -f deployment.yaml
```

## Examine the Running Application

Once the container is running, we can do something like see what health groups are exposed:

```shell script
$ http :8080/actuator/health
HTTP/1.1 200 OK
Content-Length: 58
Content-Type: application/vnd.spring-boot.actuator.v3+json

{
    "groups": [
        "custom",
        "liveness",
        "readiness"
    ],
    "status": "UP"
}
```

We know the 3 groups are available and have aggregated 'UP' status, lets do the first round of endpoint checking:

```shell script
$ http :8080/actuator/health/custom
HTTP/1.1 200 OK
Content-Length: 15
Content-Type: application/vnd.spring-boot.actuator.v3+json

{
    "status": "UP"
}
```

Change the custom HealthIndicator to 'DOWN' by hitting the `/count` endpoint for the first time:

```shell script
$ http :8080/count
HTTP/1.1 200 OK
Content-Length: 1
Content-Type: application/json

1
```

Because the count is now 1, our `my` HealthIndicator will send `BROKEN` to the health group:

```shell script
$ http :8080/actuator/health/custom
HTTP/1.1 503 Service Unavailable
Content-Length: 17
Content-Type: application/vnd.spring-boot.actuator.v3+json

{
    "status": "DOWN"
}
```

Note that subsequent requests to `/actuator/heath` will return `503 - Service Unavailable` but we are configuring liveness for a different endpoint (`/actuator/health/liveness`).
Thus, this configuration will not alter our app behaviour when `health` is `DOWN`.

Good, now lets trigger the app restart by requesting `/count` once again:

```shell script
$ http :8080/count
HTTP/1.1 200 OK
Content-Length: 1
Content-Type: application/json

0
```

At this point, within our probe frequency we will see that the app restarts as expected. For the next few seconds
our app will not respond to requests as another container will spin up and take over - with fresh counter to boot.

I usually check this by executing `kubectl get pods` and watching the `RESTARTS` counter go up by one.

## The End

