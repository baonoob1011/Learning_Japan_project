# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first for better build cache
COPY pom.xml ./

# Pre-download dependencies
RUN mvn -B -ntp dependency:go-offline

# Copy source code
COPY src ./src

# Build project
RUN mvn -B -ntp clean package -DskipTests

# Stage 2: Run application (JRE)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install runtime tools used by the app
RUN apk add --no-cache ffmpeg curl python3 nodejs && \
    curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

# Set environment variables for tools used by the app
ENV YT_DLP_PATH=/usr/local/bin/yt-dlp
ENV FFMPEG_PATH=/usr/bin/ffmpeg

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]