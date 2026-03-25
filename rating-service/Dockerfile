FROM eclipse-temurin:21-jre-alpine
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=UTC"
COPY target/rating*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
