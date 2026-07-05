# Build avec Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Image finale (très légère)
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Optimisations mémoire pour le plan gratuit
ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080 

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]