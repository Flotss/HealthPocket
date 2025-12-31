# =========================
# BUILD STAGE
# =========================
FROM gradle:9.0.0-jdk21 AS build

WORKDIR /app

# Copier fichiers Gradle
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./

# Pré-charger les dépendances
RUN ./gradlew dependencies --no-daemon || true

# Copier le code source
COPY src src

# Build Spring Boot
RUN ./gradlew bootJar -x test --no-daemon

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
