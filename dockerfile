# =========================
# Stage 1: Build the JAR
# =========================
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy Gradle files first (better caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (cache layer)
RUN ./gradlew dependencies || true

# Copy source code
COPY . .

# Build the project (skip tests)
RUN ./gradlew clean build -x test

# =========================
# Stage 2: Create Runtime Image
# =========================
FROM eclipse-temurin:21-jre AS runtime

# Create user and app directory
RUN groupadd -r cloudops && useradd -r -g cloudops cloudops
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Change ownership
RUN chown -R cloudops:cloudops /app
USER cloudops

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
