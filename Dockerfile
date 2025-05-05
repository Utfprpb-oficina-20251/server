FROM amazoncorretto:21-alpine-jdk

EXPOSE 8080

COPY target/server-0.0.1-SNAPSHOT.jar server.jar

ENTRYPOINT ["java", "-jar", "/server.jar"]