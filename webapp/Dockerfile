# Maven needs a JDK to compile the application. Use the same Java version as pom.xml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# The application runs on Tomcat, so the runtime image only needs a JRE.
FROM tomcat:10.1-jre21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/WebApp-1.0.0.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]