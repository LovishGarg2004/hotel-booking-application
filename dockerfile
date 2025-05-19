# Build stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /workspace/app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon --stacktrace

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew clean build --no-daemon -x test

# Production stage
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Set non-root user for security
RUN useradd -m myuser && chown -R myuser:myuser /app
USER myuser

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]