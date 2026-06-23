# 1. AŞAMA: Projeyi Derleme (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. AŞAMA: Çalıştırma (Run)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# İŞTE EKSİK OLAN O SİHİRLİ SATIR (Railway'in trafiği yönlendireceği kapı)
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]