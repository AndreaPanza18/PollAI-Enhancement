# Lab 2: Dockerfile & Docker Compose

We engineered 3 separate Dockerfiles for our project:
1. A Dockerfile for the webapp, containing the Spring Boot backend and frontend;
2. A Dockerfile for the EurekaServer module, containing the service registry;
3. A Dockerfile for the UovaService module, containing the AI model.

## Dockerfiles

Multi-stage builds separate build-time dependencies from the runtime image, reducing its size.
A `.dockerignore` file has been attached to each Dockerfile, excluding unnecessary files for size reduction.

### Webapp  

The [Webapp Dockerfile](../webapp/Dockerfile) uses a multi-stage build:
1. Download the Maven 3.9 image with JDK 21 from Docker Hub;
2. Set `/app` as the working directory;
3. Copy the WebApp `pom.xml` in the work directory;
4. Download the dependencies;
5. Copy the WebApp `src` folder in the work directory;
6. Build the WAR file, skipping tests;
7. Download the Tomcat 10.1 image with Java 21 from Docker Hub;
8. Remove Tomcat's default web application;
9. Copy the WAR file from the first stage into the Tomcat image as `ROOT.war`;
10. Document the exposed port;
11. Set the entry command to start Tomcat.

### EurekaServer

The [EurekaServer Dockerfile](../modulo-AI/EurekaServer/Dockerfile) uses a multi-stage build:
1. Download the Maven 4.0.0-rc-5 image with JDK 21 from Docker Hub;
2. Set `/app` as the working directory;
3. Copy the EurekaServer `pom.xml` in the work directory;
4. Download the dependencies;
5. Copy the EurekaServer `src` folder in the work directory.
6. Package the application, skipping tests;
7. Download the Eclipse Temurin JRE 21 image;
8. Set `/app` as the working directory;
9. Copy the generated JAR file into the image;
10. Document the exposed port;
11. Set the entry command to run the JAR file.

### UovaService

The [UovaService Dockerfile](../modulo-AI/UovaService/Dockerfile) is organized as follows:
1. Download the Python 3.11 slim image from Docker Hub;
2. Set `/app` as the working directory;
3. Configure the service port;
4. Copy the UovaService `requirements.txt` in the work directory;
5. Install the dependencies;
6. Copy the Flask application and AI model files into the image;
7. Document the exposed port;
8. Set the entry command to start the Flask application.

## Docker Compose

The [Docker Compose file](../docker-compose.yml) builds and runs the 3 services:
- **eureka** exposes port 8761 and has a health check;
- **uova-service** exposes port 8050, waits for Eureka to be healthy, and checks the `/health` endpoint;
- **webapp** exposes port 8060, waits for Eureka to be healthy, and loads environment variables from `secrets.env`.

The services communicate through the Compose network using their service names. The database container is hosted online through Aiven hosting service.

## Antipatterns

We avoided the following antipatterns while engineering the files:
- **No hardcoded credentials in the Dockerfiles or Compose file**; the webapp variables are loaded from `secrets.env`, as shown in the [`secrets.env.example`](../secrets.env.example).
- **No `:latest` tag used in the base images**.
- **No use of `ADD` for local files**.
- **Multi-stage builds for the Java applications**, so Maven is not included in their runtime images.
