# ===== NOMINA =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-nomina/pom.xml ./microservice-nomina/pom.xml
RUN ./mvnw -q -f microservice-nomina/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-nomina/src ./microservice-nomina/src
RUN ./mvnw -q -f microservice-nomina/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-nomina/target/microservice-nomina-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
