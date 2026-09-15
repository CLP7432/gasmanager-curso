# ===== USERS =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-users/pom.xml ./microservice-users/pom.xml
RUN ./mvnw -q -f microservice-users/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-users/src ./microservice-users/src
RUN ./mvnw -q -f microservice-users/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-users/target/microservice-users-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
