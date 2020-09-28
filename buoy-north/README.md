# Consuming Kubernetes ConfigMaps with Spring Boot

The function for this demo is to illustrate configuring a Spring Boot
application for production-readiness with Kubernetes deployments. It will:

* Build a container with spring-boot/maven
* Application that consumes ConfigMaps for property sources

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
      - image: localhost:5000/buoy-north:latest
        name: buoy
        resources: {}
```

## Support for ConfigMaps in app

A well written [document](https://cloud.spring.io/spring-cloud-static/spring-cloud-kubernetes/1.0.0.M2/multi/multi__configmap_propertysource.html) exists on this subject as well as [project notes](https://github.com/fabric8io/spring-cloud-kubernetes). Please check it out for more details.

Head to [start.spring.io](https://start.spring.io) and setup a new application with `actuator` and `webflux`.
Additionally, add the spring-cloud dependencies for ConfigMap support to the project. 

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-kubernetes-config</artifactId>
            <version>0.3.0.RELEASE</version>
        </dependency>
```

To configure ConfigMaps property consumption in the app, setup a `ConfigReloadProperties` bean.
This bean is be configured by setting properties with prefix `spring.cloud.kubernetes.reload` as is 
done in the next snippet.

application.properties:  
```properties
spring.application.name=buoy-north

spring.cloud.kubernetes.config.name=buoy-north
spring.cloud.kubernetes.config.namespace=default
spring.cloud.kubernetes.config.sources[0].name=buoy-north
spring.cloud.kubernetes.reload.enabled=true
spring.cloud.kubernetes.reload.mode=polling
spring.cloud.kubernetes.reload.strategy=refresh
spring.cloud.kubernetes.reload.period=2000
```

In this example, configmap reloading will happen on a polling interval of 2 seconds, 
and will only refresh affected properties as configured with the `spring.cloud.kubernetes.reload.strategy`
property. This property can also be configured to restart the ApplicationContext or to even shutdown the application.

I suppose events might not be available due to system policy or system design so 'polling' is used here.
Don't forget to set `spring.cloud.kubernetes.reload.mode` to 'event' if that is supported in your environment.   

## Exposing Application Endpoint

This application exposes a single endpoint that emits the values loaded into the ConfigMap.

```kotlin
    @Bean
    fun routers(@Value("\${message:default message}") message: String,
                @Value("\${countof:0}") countof: Integer) = router {
        GET("/message") {
            ServerResponse
                    .ok()
                    .body(Mono.just("${message} - ${countof}"), String::class.java)
        }
    }
```   

With sufficient knowledge of our application, we can work on the deployment details in the next section.

## Build the image

We automatically get the feature for buildpack image builds when using Spring Boot 2.3.0 or newer.
To build the image, simply execute:

```shell script
mvn clean spring-boot:build-image
```

which then lets us take said generated image and distribute it to our HUB, or local registry.

The command to do that:

```shell script
docker tag buoy:0.0.1-SNAPSHOT localhost:5000/buoy-north:latest
docker push localhost:5000/buoy-north:latest
```

### Create the ConfigMap

Let's create a ConfigMap to hold a couple of properties that the app can read at runtime. 

buoy-north-configmap.yaml:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: buoy-north
data:
  application.properties: |-
    message=hello from configmap
    countof=3
```

Then apply this configmap: `k apply -f buoy-north-configmap.yaml`.

### Permission to read ConfigMap

Depending on your setup, you may need to open permissions for viewing ConfigMaps.
As I am using `minikube` for this demo, the default user doesnt have access, so I must
enable it.

Create a RoleBinding that gives the default system user access to ConfigMaps in the default namespace
For additional information about Kubernetes RBAC, see [the documentation](https://kubernetes.io/docs/reference/access-authn-authz/rbac/).

config-view-role.yaml
```
apiVersion: rbac.authorization.k8s.io/v1
#k create rolebinding config-view --clusterrole view
#  --user system:serviceaccount:default:default --namespace default
kind: RoleBinding
metadata:
  name: config-view
  namespace: default
subjects:
  - kind: User
    name: system:serviceaccount:default:default
    apiGroup: rbac.authorization.k8s.io
roleRef:
  # "roleRef" specifies the binding to a Role / ClusterRole
  kind: ClusterRole #this must be Role or ClusterRole
  name: view # this must match the name of the Role or ClusterRole you wish to bind to
  apiGroup: rbac.authorization.k8s.io
```

To enable, execute:

```shell script
kubectl apply -f config-view-role.yaml
```

## Deploy to Kubernetes

Finally, we can deploy the application to our cluster.

Given the following `deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: buoy-north
  name: buoy-north
spec:
  replicas: 1
  selector:
    matchLabels:
      app: buoy-north
  strategy: {}
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: buoy-north
    spec:
      containers:
      - image: localhost:5000/buoy-north:latest
        name: buoy-buoy-north
        resources: {}
status: {}
---
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: null
  labels:
    app: buoy-north
  name: buoy-north
spec:
  ports:
  - name: 8080-8080
    port: 8080
    protocol: TCP
    targetPort: 8080
  selector:
    app: buoy-north
  type: LoadBalancer
status:
  loadBalancer: {}
```

Finally, execute:

```shell script
kubectl apply -f deployment.yaml
```

## Examine the Running Application

Once the container is running, we request from the `message` endpoint to see the configmap-bound properties.

```shell script
$ http :8080/message
HTTP/1.1 200 OK
Content-Length: 24
Content-Type: text/plain;charset=UTF-8

hello from configmap - 3
```

## The End

For now, this is as much as it should take for sufficient Kubernetes ConfigMap support.

Happy hacking!
