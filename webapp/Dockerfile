FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS source

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./ ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-noble

WORKDIR /result

COPY --from=source /app/target/PollAI-1.0.0.jar PollAI-ai.war

EXPOSE 8050

ENTRYPOINT [ "java", "-jar", "PollAI-ai.war" ]