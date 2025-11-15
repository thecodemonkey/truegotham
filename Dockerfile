FROM eclipse-temurin:21.0.9_10-jdk-ubi10-minimal
LABEL authors="TheCodemonkey"

WORKDIR /app
COPY build/libs/true-gotham.jar true-gotham.jar
EXPOSE 7171

# Anwendung starten
ENTRYPOINT ["java", "-jar", "true-gotham.jar"]