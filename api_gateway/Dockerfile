# Stage 1: Use Maven for building
FROM maven:3.9.9-amazoncorretto-17-alpine AS build
WORKDIR /app

COPY ./api_gateway ./api_gateway

WORKDIR /app/api_gateway
RUN mvn clean package -DskipTests

# Stage 2: use Amazoncorretto for running
FROM amazoncorretto:17-alpine3.21-jdk
WORKDIR /app
COPY --from=build /app/api_gateway/target/*.jar api_gateway.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","api_gateway.jar"]