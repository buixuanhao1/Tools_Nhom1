# =============================================
# STAGE 1 - BUILD: Dùng Maven để compile và đóng gói thành file .jar
# =============================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Đặt thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml trước để cache dependencies (tối ưu tốc độ build)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code vào container
COPY src ./src

# Build project, bỏ qua test để tăng tốc độ
RUN mvn package -DskipTests -B

# =============================================
# STAGE 2 - RUN: Dùng JRE nhỏ gọn để chạy ứng dụng
# =============================================
FROM eclipse-temurin:21-jre-alpine

# Tạo user không có quyền root để tăng bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Đặt thư mục làm việc
WORKDIR /app

# Chỉ copy file .jar đã build từ stage 1 (không mang theo source code)
COPY --from=builder /app/target/*.jar app.jar

# Đổi owner file sang user không có quyền root
RUN chown appuser:appgroup app.jar

# Chạy ứng dụng với user an toàn
USER appuser

# Expose port mà ứng dụng lắng nghe
EXPOSE 8081

# Lệnh khởi động ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
