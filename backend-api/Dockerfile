# =========================
# BUILD
# =========================
FROM gradle:8.6-jdk21 AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

# =========================
# RUNTIME
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
