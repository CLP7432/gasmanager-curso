# ===== LEALTAD =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY microservice-lealtad/pom.xml ./microservice-lealtad/pom.xml
RUN ./mvnw -q -f microservice-lealtad/pom.xml dependency:go-offline -DskipTests || true
COPY microservice-lealtad/src ./microservice-lealtad/src
RUN ./mvnw -q -f microservice-lealtad/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/microservice-lealtad/target/microservice-lealtad-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "app.jar"]
