FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/*.jar app.jar

# Optimisations mémoire pour le plan gratuit
ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080 

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]