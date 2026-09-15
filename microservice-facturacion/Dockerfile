# ===== FACTURACION =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-facturacion/pom.xml ./microservice-facturacion/pom.xml
RUN ./mvnw -q -f microservice-facturacion/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-facturacion/src ./microservice-facturacion/src
RUN ./mvnw -q -f microservice-facturacion/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-facturacion/target/microservice-facturacion-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
