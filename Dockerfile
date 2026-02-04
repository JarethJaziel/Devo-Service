FROM eclipse-temurin:17-jdk-alpine
COPY target/*.jar app.jar
# Limitamos la memoria para que Render no mate el proceso
ENTRYPOINT ["java","-Xmx300m","-jar","/app.jar"]