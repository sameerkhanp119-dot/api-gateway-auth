# Build from the repository root:  docker build -f gateway/Dockerfile -t gateway .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY auth-server/pom.xml auth-server/pom.xml
COPY gateway/pom.xml gateway/pom.xml
RUN mvn -q -B -pl gateway -am dependency:go-offline -DskipTests || true
COPY gateway gateway
RUN mvn -q -B -pl gateway -am package -DskipTests \
 && java -Djarmode=layertools -jar gateway/target/gateway-*.jar extract --destination /layers

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
