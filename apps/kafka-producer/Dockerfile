FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :apps:kafka-producer:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/apps/kafka-producer/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]