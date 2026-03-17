# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy maven wrapper và pom.xml để tải dependencies (cache layer)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Cấp quyền thực thi cho maven wrapper
RUN chmod +x mvnw

# Tải trước các dependencies để tối ưu thời gian build
RUN ./mvnw dependency:go-offline

# Copy toàn bộ source code
COPY src ./src

# Build project (bỏ qua test để tăng tốc độ nếu cần, có thể bỏ -DskipTests)
RUN ./mvnw clean package -DskipTests

# Stage 2: Chạy application (dùng JRE để giảm dung lượng image)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cài đặt ffmpeg và curl (dùng để tải yt-dlp), python3 (yt-dlp dependency)
RUN apk add --no-cache ffmpeg curl python3 && \
    # Tải yt-dlp bản Linux mới nhất
    curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

# Copy file jar từ stage builder sang
COPY --from=builder /app/target/*.jar app.jar

# Mở port (thường là 8080 cho Spring Boot)
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
