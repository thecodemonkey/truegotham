FROM openjdk:21-jdk-slim
LABEL authors="TheCodemonkey"

WORKDIR /app
COPY build/libs/true-gotham.jar true-gotham.jar
EXPOSE 7171

# Anwendung starten
ENTRYPOINT ["java", "-jar", "true-gotham.jar"]