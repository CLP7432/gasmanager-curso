# ===== EUREKA =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-eureka/pom.xml ./microservice-eureka/pom.xml
RUN ./mvnw -q -f microservice-eureka/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-eureka/src ./microservice-eureka/src
RUN ./mvnw -q -f microservice-eureka/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-eureka/target/microservice-eureka-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
