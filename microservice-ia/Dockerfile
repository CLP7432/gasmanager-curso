# ===== IA =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-ia/pom.xml ./microservice-ia/pom.xml
RUN ./mvnw -q -f microservice-ia/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-ia/src ./microservice-ia/src
RUN ./mvnw -q -f microservice-ia/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-ia/target/microservice-ia-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8097
ENTRYPOINT ["java", "-jar", "app.jar"]
