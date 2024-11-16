# เลือก base image สำหรับ Java 23
FROM openjdk:23-jdk-slim

# กำหนด Working Directory
WORKDIR /app

# คัดลอกไฟล์ JAR ของแอปไปยัง container
COPY target/server-management-0.0.1-SNAPSHOT.jar app.jar

# ระบุคำสั่งสำหรับการรันแอป
ENTRYPOINT ["java", "-jar", "app.jar"]