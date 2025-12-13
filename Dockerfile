FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /home/gradle/src

# Copy dependency files first for better layer caching
COPY --chown=gradle:gradle build.gradle settings.gradle gradle.properties ./
COPY --chown=gradle:gradle gradle ./gradle

# Download dependencies (cached unless build files change)
RUN gradle dependencies --no-daemon --quiet || true

# Copy source code
COPY --chown=gradle:gradle src ./src

# Build the application
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
# cert/ folder is created at runtime via secrets/volume mounts
RUN mkdir -p ./cert && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
