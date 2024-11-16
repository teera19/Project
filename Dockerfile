# Base image สำหรับ Maven build
FROM maven:3.9.4-eclipse-temurin-22 AS build

# ตั้ง Working Directory
WORKDIR /app

# คัดลอกไฟล์ pom.xml และ Maven Wrapper (mvnw และ .mvn) ไปยัง container
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# กำหนดสิทธิ์รันสำหรับ mvnw
RUN chmod +x mvnw

# ดาวน์โหลด dependencies ล่วงหน้าเพื่อเพิ่มประสิทธิภาพ cache
RUN ./mvnw dependency:go-offline

# คัดลอก source code ทั้งหมดและ build ไฟล์ JAR
COPY src ./src
RUN ./mvnw package -DskipTests

# Base image สำหรับรันแอปพลิเคชัน
FROM openjdk:22-jdk-slim

# ตั้ง Working Directory
WORKDIR /app

# คัดลอกไฟล์ JAR จากขั้นตอน build
COPY --from=build /app/target/server-management-0.0.1-SNAPSHOT.jar app.jar

# ระบุคำสั่งรันแอปพลิเคชัน
ENTRYPOINT ["java", "-jar", "app.jar"]

# เปิดพอร์ต 8080
EXPOSE 8080
