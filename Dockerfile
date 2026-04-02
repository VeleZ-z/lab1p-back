FROM eclipse-temurin:21-jre-jammy
EXPOSE 8080
ADD target/lab1p.jar lab1p.jar
ENTRYPOINT ["java","-jar","/lab1p.jar"]
