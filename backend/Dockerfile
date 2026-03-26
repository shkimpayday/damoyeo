# ============================================================
# 다모여 Backend Dockerfile
# Multi-stage build: 빌드 이미지와 실행 이미지 분리
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
# Gradle 빌드를 위한 JDK 이미지 사용
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 의존성 캐시 최적화: 소스 변경 없이 의존성만 변경 시 캐시 활용
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Windows에서 checkout 시 CRLF로 변환된 경우 LF로 복구
# (없으면 "bad interpreter: No such file or directory" 오류 발생)
RUN sed -i 's/\r$//' gradlew && chmod +x ./gradlew

# 의존성 먼저 다운로드 (캐시 레이어 분리)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드 (테스트 제외)
COPY src src
RUN ./gradlew build -x test --no-daemon

# ── Stage 2: Run ────────────────────────────────────────────
# 실행에는 JRE만 필요 (이미지 크기 최소화)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# 포트 노출
EXPOSE 8080

# JVM 메모리 제한 (t2.micro 1GB RAM 환경 최적화)
# -Xmx384m: 최대 힙 384MB
# -Xss256k: 스레드 스택 256KB (기본 512KB에서 축소)
# -XX:+UseContainerSupport: 컨테이너 환경 메모리 자동 감지
ENTRYPOINT ["java", \
  "-Xmx384m", \
  "-Xss256k", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=50.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
