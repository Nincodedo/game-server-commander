FROM maven:3.8.4-eclipse-temurin-17 AS build
COPY pom.xml .
COPY .mvn ./.mvn
RUN mvn -B dependency:resolve
COPY src ./src
COPY .git ./.git
RUN mvn package -P git-commit

FROM openjdk:17-jdk-slim-buster

LABEL maintainer="Nincodedo"

RUN mkdir /app
RUN groupadd -g 1000 -r gsc && useradd -r -s /bin/false -u 1000 -g gsc gsc
WORKDIR /app
COPY --chown=gsc:gsc --from=build target/ocw-game-server-commander*.jar /app/gsc.jar
COPY DockerHealthCheck.java /app
USER gsc
HEALTHCHECK CMD java /app/DockerHealthCheck.java 2>&1 || exit 1
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-Xss512k", "-Xmx256M", "--enable-preview", "-jar", "/app/gsc.jar"]
