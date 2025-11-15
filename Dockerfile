FROM eclipse-temurin:21.0.9_10-jdk-ubi10-minimal
LABEL authors="TheCodemonkey"

WORKDIR /app
COPY build/libs/true-gotham.jar true-gotham.jar
EXPOSE 7171

# Optional: build-arg to allow overriding default at build time
ARG SPRING_PROFILES_ACTIVE=prod
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# Application start — allows overriding the profile at runtime
ENTRYPOINT ["sh","-c","java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar true-gotham.jar"]