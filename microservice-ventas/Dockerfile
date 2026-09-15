# ===== VENTAS =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-ventas/pom.xml ./microservice-ventas/pom.xml
RUN ./mvnw -q -f microservice-ventas/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-ventas/src ./microservice-ventas/src
RUN ./mvnw -q -f microservice-ventas/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-ventas/target/microservice-ventas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "app.jar"]
