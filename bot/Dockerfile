FROM eclipse-temurin:latest
LABEL authors="eflerrr"

WORKDIR /app

COPY target/bot.jar .

EXPOSE 8090 12090

CMD ["java", "-jar", "bot.jar"]
