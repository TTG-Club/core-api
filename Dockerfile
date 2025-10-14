FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /opt/app
COPY --link pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B
COPY --link src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -B -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app
COPY --from=build /opt/app/target/dnd5.jar dnd5.jar
ENTRYPOINT ["java","-jar","dnd5.jar"]
