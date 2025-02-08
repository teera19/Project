# ใช้ OpenJDK 17 เป็น Base Image
FROM openjdk:21-jdk-slim

# กำหนด Working Directory
WORKDIR /app

# คัดลอกไฟล์ JAR เข้าไปใน Container
COPY target/*.jar app.jar

# รันแอปพลิเคชัน
ENTRYPOINT ["java", "-jar", "app.jar"]
