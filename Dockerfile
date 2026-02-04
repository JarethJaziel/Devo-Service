FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
COPY --from=build target/*.jar app.jar
# Limitamos la memoria para que Render no mate el proceso
ENTRYPOINT ["java","-Xmx300m","-jar","/app.jar"]