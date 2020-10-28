FROM openjdk:11.0.9-jdk-slim-buster as build
WORKDIR /workspace/buoy

COPY pom.xml .
COPY src src
COPY .mvn .mvn
COPY mvnw .

RUN ./mvnw install -DskipTests

FROM openjdk:11.0.9-jdk-slim-buster
ARG JAR_FILE=/workspace/buoy/target/buoy-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
