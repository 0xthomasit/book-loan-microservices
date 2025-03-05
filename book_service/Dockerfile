# Stage 1: Use Maven for building
FROM maven:3.9.9-amazoncorretto-17-alpine AS build
WORKDIR /app

COPY ./book_service ./book_service
COPY ./common_service ./common_service

WORKDIR /app/common_service
RUN mvn clean install -DskipTests

WORKDIR /app/book_service
RUN mvn clean package -DskipTests

# Staage 2: use Amazoncorretto for running
FROM amazoncorretto:17-alpine3.21-jdk
WORKDIR /app
COPY --from=build /app/book_service/target/*.jar book_service.jar
EXPOSE 9001
ENTRYPOINT ["java","-jar","book_service.jar"]