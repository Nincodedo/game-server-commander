FROM maven:3.6.3-openjdk-15 AS build
COPY pom.xml .
COPY .mvn ./.mvn
RUN mvn -B dependency:resolve
COPY src ./src
COPY .git ./.git
RUN mvn package -P git-commit

FROM adoptopenjdk/openjdk15:debianslim-jre
LABEL mainainer="Nincodedo"
LABEL source="https://github.com/Nincodedo/game-server-commander"
RUN mkdir /app
RUN groupadd -r commander && useradd -r -s /bin/false -g commander commander
WORKDIR /app
COPY --chown=commander:commander --from=build target/game-server-commander*.jar /app/game-server-commander.jar
RUN apt-get update && apt-get install curl=7.64.0-4+deb10u1 -y --no-install-recommends && apt-get clean && rm -rf /var/lib/apt/lists/*
USER commander
HEALTHCHECK --start-period=20s CMD curl --fail --silent http://localhost:8080/actuator/health 2>&1 | grep UP || exit 1
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["--enable-preview", "-jar", "/app/game-server-commander.jar"]
