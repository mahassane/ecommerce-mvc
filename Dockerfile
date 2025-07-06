FROM openjdk:17-jdk-alpine
COPY out/artifacts/mvc.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
