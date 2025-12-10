FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
# cert/ folder is created at runtime via secrets/volume mounts
RUN mkdir -p ./cert
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
