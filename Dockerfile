# Build stage
FROM gradle:8.7-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
