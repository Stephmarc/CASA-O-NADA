FROM eclipse-temurin:21-jre
ARG APP
WORKDIR /app
COPY ${APP}/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC"
ENTRYPOINT ["java","-jar","app.jar"]
