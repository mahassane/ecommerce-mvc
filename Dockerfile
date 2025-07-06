FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:17-jdk-alpine
COPY --from=build /app/target/mvc.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
