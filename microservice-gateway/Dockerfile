# ===== GATEWAY =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-gateway/pom.xml ./microservice-gateway/pom.xml
RUN ./mvnw -q -f microservice-gateway/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-gateway/src ./microservice-gateway/src
RUN ./mvnw -q -f microservice-gateway/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-gateway/target/microservice-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
