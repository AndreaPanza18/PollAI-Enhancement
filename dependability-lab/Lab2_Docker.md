# Lab 2: Dockerfile & Docker Compose

We engineered 3 separate Dockerfiles for our project:
1. A Dockefile for the webapp module, containing both the backend and frontend of the application;
2. A Dockerfile for the EurekaServer module, containing the AI model;
3. A Dockerfile for the UovaService module, containing the 

## Dockerfiles
### Dockerfile 1

The [`Webapp Dockerfile`](/webapp/Dockerfile) uses a multi-stage build:
1. Download maven 3.9 base image from Docker Hub
2. 

### Dockerfile 2

The [`EurekaServer Dockerfile`](/modulo-AI/EurekaServer/Dockerfile) uses a multi-stage build:

### Dockerfile 3

The [`UovaService Dockerfile`](/modulo-AI/UovaService/Dockerfile) is organized as follows:


## Docker Compose

The [`Docker Compose`](/docker-compose.yml) is 

## Antipatterns

We avoided the following antipatterns while engineering the files:
- **No hardcoded secrets**, all credentials are stored in a dedicated secrets.env file, as shown in the [`secrets.env.example`](/secrets.env.example).
- **No `:latest` tag used in the base images**.
- **No use of `ADD` for local files**.
- **No size bloating, each image only copies files in its dedicate working directory**.