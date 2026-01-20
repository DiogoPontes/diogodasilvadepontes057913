FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . /app

RUN ./mvnw -q -DskipTests package

CMD ["java", "-jar", "target/music-api-0.0.1-SNAPSHOT.jar"]