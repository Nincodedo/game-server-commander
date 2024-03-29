FROM maven:3.8.7-eclipse-temurin-19 AS build
COPY pom.xml .
COPY .mvn ./.mvn
RUN mvn -B dependency:resolve
COPY src ./src
COPY .git ./.git
RUN mvn package -P git-commit

FROM eclipse-temurin:19-jre-focal

LABEL maintainer="Nincodedo"

RUN mkdir /app
WORKDIR /app
COPY --from=build target/ocw-game-server-commander*.jar /app/gsc.jar
HEALTHCHECK CMD curl http://localhost:8085/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-Xss512k", "-Xmx256M", "--enable-preview", "-jar", "/app/gsc.jar"]
