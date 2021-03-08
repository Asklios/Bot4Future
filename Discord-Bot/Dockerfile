FROM maven:latest as builder
COPY . /app
WORKDIR /app
RUN mvn install

FROM openjdk:11-jre-slim
RUN mkdir -p /app//data
COPY --from=builder /app/target/Bot4Future-*-jar-with-dependencies.jar /app/bot.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","/app/bot.jar"]