FROM maven:3.6.1-jdk-8-alpine AS builder
WORKDIR /build/
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:8-jre-alpine as RUNNER
WORKDIR /app/
COPY --from=builder /build/target/gameofthree-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
