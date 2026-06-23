# 1. AŞAMA: Projeyi Derleme (Build)
# İçinde Maven yüklü olan bir Java ortamı çağırıyoruz
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Pom dosyamızı ve kaynak kodlarımızı (src) sunucuya kopyalıyoruz
COPY pom.xml .
COPY src ./src

# Projeyi bulut üzerinde derleyip JAR dosyasını oluşturuyoruz (Testleri atlıyoruz ki hızlı olsun)
RUN mvn clean package -DskipTests

# 2. AŞAMA: Çalıştırma (Run)
# Sadece çalıştırmak için daha hafif bir Java ortamı çağırıyoruz
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# İlk aşamada üretilen JAR dosyasını bu hafif ortama alıyoruz
COPY --from=build /app/target/*.jar app.jar

# Uygulamayı başlatıyoruz
ENTRYPOINT ["java","-jar","app.jar"]