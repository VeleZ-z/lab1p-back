FROM openjdk:21-ea-18-slim
EXPOSE 8080
ADD target/lab1p.jar lab1p.jar
ENTRYPOINT ["java","-jar","/lab1p.jar"]
