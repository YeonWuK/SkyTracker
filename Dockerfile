FROM gradle:8.5.0-jdk17 AS builder

WORKDIR /app

# 전체 프로젝트 복사
COPY . .

# BootJar 빌드
RUN gradle :apps:api-server:bootJar --no-daemon --stacktrace -x test

# 2단계: 실행 이미지
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/apps/api-server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]