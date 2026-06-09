# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
# Cache dependencies before copying source
RUN ./gradlew dependencies --no-daemon --quiet 2>/dev/null || true

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=railway

EXPOSE ${PORT:-8080}

ENTRYPOINT ["sh", "-c", "java -Xms48m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=64m -jar app.jar --server.port=${PORT:-8080}"]
