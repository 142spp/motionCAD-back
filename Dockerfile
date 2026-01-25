# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Use clean to ensure no stale artifacts and specifically build the bootJar
RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

# Run stage
FROM eclipse-temurin:17-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app
# Explicitly copy the generated Boot JAR
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
