# =========================
# Stage 1: Build the JAR
# =========================
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy all project files
COPY . .

# Build the project (skip tests for speed in CI, optional)
RUN gradle clean build -x test

# =========================
# Stage 2: Runtime image
# =========================
FROM eclipse-temurin:17-jre

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
