# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-jammy AS runtime
ARG APP_VERSION=local
LABEL org.opencontainers.image.title="inxbtwt-api" \
      org.opencontainers.image.version="${APP_VERSION}"
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --home-dir /app app
COPY --from=build --chown=app:app /build/target/inxbtwt-api-*.jar /app/api.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/api.jar"]
