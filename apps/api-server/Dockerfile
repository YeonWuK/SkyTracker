# 1단계: Gradle을 이용해 build (빌더 이미지)
FROM gradle:8.5.0-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 전체 소스 복사 (멀티모듈을 위해 루트까지 복사)
COPY . .

# 빌드 수행 (테스트 제외)
RUN gradle :apps:api-server:bootJar --no-daemon --stacktrace -x test

# 2단계: 실행 이미지
FROM eclipse-temurin:17-jre

# 작업 디렉토리 생성
WORKDIR /app

# 빌드한 JAR 복사
COPY --from=builder /app/apps/api-server/build/libs/api-server-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# JAR 실행
ENTRYPOINT ["java", "-jar", "app.jar"]