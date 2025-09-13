# Stage 1: Build the project
FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the project
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/job-scheduler-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
