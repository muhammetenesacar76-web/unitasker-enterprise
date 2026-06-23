# Projemizin çalışması için arka planda hafif bir Java (Temurin) ortamı kuruyoruz
FROM eclipse-temurin:17-jdk-alpine

# Proje dosyalarının tutulacağı geçici bir klasör
VOLUME /tmp

# Bilgisayarımızda derlediğimiz JAR dosyasını Docker'ın içine "app.jar" olarak kopyala
COPY target/*.jar app.jar

# Uygulamayı çalıştır
ENTRYPOINT ["java","-jar","/app.jar"]