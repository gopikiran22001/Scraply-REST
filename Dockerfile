FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the packaged Spring Boot application JAR built by Maven.
COPY target/rest-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]