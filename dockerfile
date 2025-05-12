# Use a lightweight Java 17 image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR into the image
COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]