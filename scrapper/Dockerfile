FROM eclipse-temurin:latest
LABEL authors="eflerrr"

WORKDIR /app

COPY target/scrapper.jar .

EXPOSE 8080 12080

CMD ["java", "-jar", "scrapper.jar"]
