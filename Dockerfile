# Base image สำหรับ Maven build
FROM maven:3.9.4-eclipse-temurin-17 AS build

# ตั้ง Working Directory
WORKDIR /app

# คัดลอกไฟล์ pom.xml และ dependencies ไปก่อนเพื่อ cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline

# คัดลอก source code ทั้งหมดและ build
COPY src ./src
RUN ./mvnw package -DskipTests

# Base image สำหรับรันแอป
FROM openjdk:17-jdk-slim

# ตั้ง Working Directory
WORKDIR /app

# คัดลอกไฟล์ JAR จากขั้นตอน build ไปยัง container
COPY --from=build /app/target/server-management-0.0.1-SNAPSHOT.jar app.jar

# ระบุคำสั่งรันแอปพลิเคชัน
ENTRYPOINT ["java", "-jar", "app.jar"]

# เปิดพอร์ต 8080
EXPOSE 8080
