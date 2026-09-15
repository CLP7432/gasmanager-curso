# ===== CLIENTES =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-clientes/pom.xml ./microservice-clientes/pom.xml
RUN ./mvnw -q -f microservice-clientes/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-clientes/src ./microservice-clientes/src
RUN ./mvnw -q -f microservice-clientes/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-clientes/target/microservice-clientes-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
