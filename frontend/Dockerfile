# ============================================================
# 다모여 Frontend Dockerfile
# Multi-stage build: Node.js로 빌드 → Nginx로 서빙
# ============================================================

# ── Stage 1: Build ──────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# 의존성 캐시 최적화
COPY package*.json ./
RUN npm ci

# 소스 복사
COPY . .

# 빌드 시 주입할 환경변수
# GitHub Actions에서 --build-arg VITE_API_HOST=... 로 전달
ARG VITE_API_HOST
ARG VITE_KAKAO_CLIENT_ID
ARG VITE_KAKAO_REDIRECT_URI

ENV VITE_API_HOST=$VITE_API_HOST
ENV VITE_KAKAO_CLIENT_ID=$VITE_KAKAO_CLIENT_ID
ENV VITE_KAKAO_REDIRECT_URI=$VITE_KAKAO_REDIRECT_URI

RUN npm run build

# ── Stage 2: Nginx ──────────────────────────────────────────
# 경량 Nginx 이미지로 정적 파일 서빙
FROM nginx:alpine

# 빌드 결과물 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 기본 Nginx 설정 제거 (default.conf만 사용)
RUN rm -f /etc/nginx/conf.d/default.conf.bak

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
