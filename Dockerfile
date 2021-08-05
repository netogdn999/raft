FROM openjdk:8-jdk-alpine
COPY target/no-0.0.1-SNAPSHOT.jar no-1.0.0.jar
ENTRYPOINT ["java","-jar","/no-1.0.0.jar"]