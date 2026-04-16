# ── Stage 1: Build ──────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Cache de dependências
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar user não-root
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /app/target/*.jar app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
