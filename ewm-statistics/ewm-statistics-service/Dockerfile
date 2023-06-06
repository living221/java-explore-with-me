FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar ewm-statistics-service.jar
ENTRYPOINT ["java","-jar","/ewm-statistics-service.jar"]