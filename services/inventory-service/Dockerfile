FROM maven:3.9-eclipse-temurin-25-noble AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY services/order-service/pom.xml services/order-service/pom.xml
COPY services/inventory-service/pom.xml services/inventory-service/pom.xml
COPY services/inventory-service/src services/inventory-service/src
RUN mvn -pl services/inventory-service -am package -DskipTests
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/services/inventory-service/target/inventory-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
