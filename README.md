# Irai Yoga v1 Server Project

This repository contains the backend server for Irai Yoga v1, built with Java and Spring Boot. It provides RESTful APIs and backend logic for the Irai Yoga application.

## Project Structure

```
├── build.gradle            # Gradle build configuration
├── dockerfile              # Dockerfile for containerization
├── src/
│   ├── main/
│   │   ├── java/           # Main Java source code (organized by package)
│   │   └── resources/      # Application configs, static files, templates
│   └── test/
│       └── java/           # Test code (unit/integration tests)
├── logs/                   # Application logs
├── build/                  # Build outputs and reports
├── gradle/                 # Gradle wrapper files
├── settings.gradle         # Gradle settings
├── README.md               # Project documentation
```

## Main Modules (src/main/java/yoga/irai/server/module)
- **account/**: User accounts and Authentication management
- **dashboard/**: Admin and mobile dashboard APIs
- **event/**: Event management
- **organization/**: Organization management
- **poem/**: Poem-related features
- **practice/**: Practice management
- **practice/category/**: Practice category management
- **program/**: Program management
- **setting/**: Application settings and configurations
- **shorts/**: Short video management
- **storage/**: File storage and management
- **utilities/**: Additional modules for various features

## Configuration
- `src/main/resources/application.properties`: Main application configuration

## Running the Application

### Prerequisites
- Java 21 or higher
- Gradle (or use the provided wrapper: `./gradlew`)
- Docker (optional, for containerized deployment)

## Code Style and Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) for code formatting and style checks.

### Check Code Style
To verify that your code meets the style requirements, run:
```
./gradlew spotlessCheck
```
### Format Code Automatically
To automatically format your code according to the project's style rules, run:
```
./gradlew spotlessApply
```
## Testing

Run tests with:
```
./gradlew test
```

## Build and Run..

```
./gradlew build
./gradlew bootRun
```

Or, to run with Docker:

```
docker build -t irai-yoga-server .
docker run -p 8080:8080 irai-yoga-server
```

## Logging

Application logs are stored in the `logs/` directory.

## Swagger Documentation

API documentation is available at `http://localhost:8080/api/swagger-ui/index.html` after starting the application.

