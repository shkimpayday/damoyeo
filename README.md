# 다모여 (DaMoYeo)

오프라인 모임 플랫폼 — 지역/관심사 기반 모임 생성, 정기모임 관리, 실시간 채팅

[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com)
[![AWS](https://img.shields.io/badge/AWS-EC2-FF9900?logo=amazonaws&logoColor=white)](https://aws.amazon.com)
[![HTTPS](https://img.shields.io/badge/HTTPS-damoyeo.store-brightgreen?logo=letsencrypt)](https://damoyeo.store)

**라이브 데모: [https://damoyeo.store](https://damoyeo.store)**

포트폴리오: [https://shkimpayday.github.io/damoyeo/docs/portfolio/](https://shkimpayday.github.io/damoyeo/docs/portfolio/)

---

## 프로젝트 소개

소모임(somoim.co.kr)을 참조해서 만든 오프라인 모임 플랫폼입니다.
기획부터 설계, 구현, 배포 자동화까지 풀스택 1인 개발로 진행했습니다.

주요 기능은 모임 생성/가입, 정기모임(정모) 관리, WebSocket 기반 실시간 채팅, 게시판/갤러리, 카카오페이 결제, 고객 상담 채팅, 관리자 대시보드입니다. 회원가입 시 이메일 인증, 카카오 소셜 로그인도 지원합니다.

---

## 기술 스택

**Frontend** — React 19, TypeScript 5.9, Vite 7, TanStack Query 5, Zustand 5, Tailwind CSS 4, @stomp/stompjs, MSW

**Backend** — Spring Boot 3.1, Java 17, Spring Security, JWT(jjwt), JPA + QueryDSL, MariaDB 10.11, Spring WebSocket/STOMP

**Infra** — AWS EC2, Docker Compose, GitHub Actions, Nginx(이중), Let's Encrypt

---

## 아키텍처

```
브라우저 (React SPA)
    │ HTTPS
    ▼
호스트 nginx — SSL 종료, HTTP→HTTPS 리다이렉트
    │ HTTP (127.0.0.1:8888, 외부 접근 차단)
    ▼
Docker Compose (EC2 t2.micro)
    ├── frontend (nginx) — React SPA 서빙, /api/* 프록시
    ├── backend (Spring Boot :8080)
    └── mariadb (10.11)
```

CI/CD: `git push` → GitHub Actions → Docker 이미지 빌드 → Docker Hub 푸시 → EC2 SSH → `docker compose pull && up -d`

---

## 핵심 구현

**JWT 동시 갱신 Race Condition 처리**

Access Token 만료 시 여러 요청이 동시에 401을 받는 상황을 `isRefreshing` 플래그와 `failedQueue` 대기열로 처리했습니다. 갱신이 완료되면 대기 중이던 요청들을 새 토큰으로 일괄 재실행합니다.

**WebSocket JWT 인증**

HTTP 요청과 달리 WebSocket은 연결 후 헤더 변경이 불가능합니다. STOMP CONNECT 프레임의 커스텀 헤더로 JWT를 전달하고, 서버의 `JWTChannelInterceptor`에서 토큰을 검증해 SecurityContext를 설정합니다.

**nginx 이중 프록시**

호스트 nginx에서 SSL을 종료하고, Docker nginx를 `127.0.0.1:8888`로만 바인딩해 EC2 외부에서 직접 접근을 차단했습니다. SSL 인증서 갱신(certbot)도 호스트 nginx에서만 처리합니다.

**Feature-based 아키텍처**

```
features/
├── auth, groups, meetings, chat
├── board, gallery, notifications, events
├── payment, support, admin
```

기능 단위로 `api/`, `components/`, `hooks/`, `types/`를 묶어서 도메인 추가 시 해당 폴더만 수정되도록 구성했습니다.

---

## 프로젝트 구조

```
damoyeo/
├── frontend/                  # React 19 + Vite 7
│   ├── src/
│   │   ├── app/routes/        # 라우터 + 페이지
│   │   ├── features/          # 기능별 모듈 (11개)
│   │   ├── components/        # 공통 UI 컴포넌트
│   │   ├── lib/               # axios, react-query 설정
│   │   └── mocks/             # MSW API 모킹 (개발용)
│   ├── nginx.conf
│   └── Dockerfile
│
├── backend/
│   └── src/main/java/com/damoyeo/api/
│       ├── domain/            # 비즈니스 도메인 (12개)
│       └── global/            # Security, WebSocket, JWT 설정
│
├── docker-compose.prod.yml
└── .github/workflows/         # GitHub Actions CI/CD
```

---

## 로컬 실행

```bash
# Frontend
cd frontend
npm install
cp .env.example .env
npm run dev   # http://localhost:5173

# Backend
cd backend
./gradlew bootRun   # http://localhost:8080
```

**.env**
```
VITE_API_HOST=http://localhost:8080
VITE_KAKAO_CLIENT_ID=카카오_REST_API_키
```

Swagger UI: http://localhost:8080/swagger-ui.html

---

## 데모 계정

| 구분 | 이메일 | 비밀번호 |
|------|--------|---------|
| 일반 사용자 | `demo@damoyeo.store` | `admin` |
| 관리자 | `admin@damoyeo.store` | `admin` |

---

## 회고

잘 됐다고 생각하는 부분은 JWT Race Condition 처리, WebSocket 인증 구조, GitHub Actions 기반 자동 배포 파이프라인입니다. 모임 채팅/정모 채팅/고객지원 채팅을 단일 WebSocket 인프라로 통일한 것도 만족스럽습니다.

아쉬운 점은 알림을 폴링으로 구현해서 SSE로 전환하지 못한 것, 이미지 업로드 시 리사이징/최적화 미처리, 테스트 코드가 없는 것입니다. EC2 t2.micro에서 간헐적으로 메모리 부족이 발생하는 것도 해결하고 싶습니다.
