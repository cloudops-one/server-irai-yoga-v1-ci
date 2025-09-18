# =========================
# Stage 1: Build the JAR
# =========================
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy Gradle wrapper and configs first (cache optimization)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Fix wrapper permissions
RUN chmod +x gradlew

# Pre-download dependencies
RUN ./gradlew dependencies || true

# Copy the full source code
COPY . .

# ✅ Fix formatting automatically before build
RUN ./gradlew spotlessApply

# Build the project (skip tests for speed in CI/CD)
RUN ./gradlew clean build -x test

# =========================
# Stage 2: Runtime image
# =========================
FROM eclipse-temurin:21-jre

RUN groupadd -r cloudops && useradd -r -g cloudops cloudops
WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/build/libs/*.jar /app/app.jar

RUN chown -R cloudops:cloudops /app
USER cloudops

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
