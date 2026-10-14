FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS source

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./ ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-noble

WORKDIR /result

COPY --from=source /app/target/PollAI-0.0.1-SNAPSHOT.war PollAI.war

EXPOSE 8060

ENTRYPOINT [ "java", "-jar", "PollAI.war" ]