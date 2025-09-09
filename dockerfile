# Base image with JDK for running Spring Boot application
FROM eclipse-temurin:21-jdk-jammy

# ✅ Create a non-root user for running the app
RUN groupadd -r cloudops && useradd -r -g cloudops cloudops

# ✅ Create application directory and assign permissions
WORKDIR /app
RUN mkdir -p /app && chown -R cloudops:cloudops /app

# ✅ Copy the built JAR file from the Jenkins workspace to the Docker image
COPY ./build/libs/*.jar /app/app.jar

# ✅ Change ownership of JAR file so cloudops user can access it
RUN chown cloudops:cloudops /app/app.jar

# ✅ Switch to non-root user
USER cloudops

# ✅ Inform Docker that the app listens on port 8080
EXPOSE 8080

# ✅ Run the JAR file using the active Spring profile from an environment variable
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
