FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew 
RUN ./gradlew dependencies 

COPY . .

RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY graduate-real-data-part1.csv .
COPY graduate-real-data-part2.csv .
RUN cat graduate-real-data-part1.csv graduate-real-data-part2.csv > graduate-real-data.csv && \
    rm -f graduate-real-data-part1.csv graduate-real-data-part2.csv

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
