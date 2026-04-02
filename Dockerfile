FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /lab1p
COPY . .
RUN chmod +x ./mvnw && ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /lab1p
COPY --from=build /lab1p/target/*.jar lab1p.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "lab1p.jar"]