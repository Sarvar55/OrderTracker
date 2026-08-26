FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY --chmod=755 mvnw .
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q dependency:go-offline -DskipTests

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q clean package -DskipTests

FROM gcr.io/distroless/java17-debian12:nonroot

LABEL org.opencontainers.image.title="OrderTracker"

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
