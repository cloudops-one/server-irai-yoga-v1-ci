# =========================
# Stage 1: Build the JAR
# =========================
FROM gradle:8.5-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and config first (better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# ✅ Fix gradlew permissions so it can run
RUN chmod +x gradlew

# Pre-download dependencies (cache layer)
RUN ./gradlew dependencies || true

# Copy the full source code
COPY . .

# Build the project (skip tests for speed in CI/CD)
RUN ./gradlew clean build -x test

# =========================
# Stage 2: Runtime image
# =========================
FROM eclipse-temurin:21-jre

# Create non-root user
RUN groupadd -r cloudops && useradd -r -g cloudops cloudops

# Set working directory
WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Fix permissions
RUN chown -R cloudops:cloudops /app
USER cloudops

# Run the JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
