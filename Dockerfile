FROM openjdk:8-jre-alpine
EXPOSE 8084
WORKDIR /app
COPY target/thor-1.0.1.jar .
ENTRYPOINT [ "java", "-jar", "thor-1.0.1.jar" ]