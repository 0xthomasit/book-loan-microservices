# Stage 1: Use Maven for building
FROM maven:3.9.9-amazoncorretto-17-alpine AS build
WORKDIR /app

COPY ./notification_service ./notification_service
COPY ./common_service ./common_service

WORKDIR /app/common_service
RUN mvn clean install -DskipTests

WORKDIR /app/notification_service
RUN mvn clean package -DskipTests

# Stage 2: use Amazoncorretto for running
FROM amazoncorretto:17-alpine3.21-jdk
WORKDIR /app
COPY --from=build /app/notification_service/target/*.jar notification_service.jar
EXPOSE 9003
ENTRYPOINT ["java","-jar","notification_service.jar"]