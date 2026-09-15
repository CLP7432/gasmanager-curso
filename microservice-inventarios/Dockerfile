# ===== INVENTARIOS =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-inventarios/pom.xml ./microservice-inventarios/pom.xml
RUN ./mvnw -q -f microservice-inventarios/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-inventarios/src ./microservice-inventarios/src
RUN ./mvnw -q -f microservice-inventarios/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-inventarios/target/microservice-inventarios-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]
