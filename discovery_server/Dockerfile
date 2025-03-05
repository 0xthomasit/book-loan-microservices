# Stage 1: Use Maven for building
FROM maven:3.9.9-amazoncorretto-17-alpine AS build
WORKDIR /app
COPY ./discovery_server ./discovery_server
WORKDIR /app/discovery_server
RUN mvn clean package -DskipTests

# Stage 2: use Amazoncorretto for running
FROM amazoncorretto:17-alpine3.21-jdk
WORKDIR /app
COPY --from=build /app/discovery_server/target/*.jar discovery_server.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","discovery_server.jar"]