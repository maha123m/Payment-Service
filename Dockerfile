# Build stage
FROM gradle:8.0-jdk17 AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle gradle.properties settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Copy source code
COPY grails-app ./grails-app
COPY src ./src

# Build the application (skip tests and browser driver setup)
RUN ./gradlew clean build -x test -x configureChromeDriverBinary -x configureGeckoDriverBinary

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy built WAR file from builder
COPY --from=builder /app/build/libs/*.war app.war

# Expose port
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.war"]