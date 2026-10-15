# Build from the repository root:  docker build -f auth-server/Dockerfile -t auth-server .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY auth-server/pom.xml auth-server/pom.xml
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -q -B -pl auth-server -am dependency:go-offline -DskipTests || true
COPY auth-server auth-server
RUN mvn -q -B -pl auth-server -am package -DskipTests \
 && java -Djarmode=layertools -jar auth-server/target/auth-server-*.jar extract --destination /layers

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /layers/dependencies/ ./
COPY --from=build /layers/spring-boot-loader/ ./
COPY --from=build /layers/snapshot-dependencies/ ./
COPY --from=build /layers/application/ ./
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
