# Paso 1: Compilar con Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Paso 2: Ejecutar con Java 21
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build target/*.jar app.jar
ENTRYPOINT ["java","-Xmx300m","-jar","/app.jar"]