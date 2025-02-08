# ใช้ OpenJDK 21 เป็น Base Image
FROM openjdk:21-jdk-slim AS build

# กำหนดโฟลเดอร์ทำงาน
WORKDIR /app

# คัดลอกไฟล์ทั้งหมดเข้าไปใน Container
COPY . .

# รัน Maven เพื่อ Build JAR (สร้างโฟลเดอร์ target/)
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ใช้ JAR ที่ Build เสร็จแล้ว
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# รันแอป
ENTRYPOINT ["java", "-jar", "app.jar"]
