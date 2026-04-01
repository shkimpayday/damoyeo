# 배포가 어떻게 동작하는가? - 완전 초보자 가이드

> `git push` 한 번이 어떤 과정을 거쳐 브라우저에서 보이게 되는지 처음부터 끝까지 설명합니다.

---

## 전체 흐름 한눈에 보기

```
┌─────────────────────────────────────────────────────────────────┐
│  👨‍💻 개발자 PC                                                   │
│                                                                  │
│  코드 수정 → git push origin main                               │
└──────────────────────────┬──────────────────────────────────────┘
                           │ 코드 + 설정 파일 전송
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│  🐙 GitHub                                                       │
│                                                                  │
│  ┌─────────────────────┐   ┌──────────────────────────────────┐ │
│  │   코드 저장소        │   │  Repository Secrets (금고)       │ │
│  │  (소스코드)          │   │  DB_PASSWORD=****                │ │
│  │                     │   │  KAKAO_CLIENT_ID=****            │ │
│  │  "push 감지!"        │   │  JWT_SECRET=****                 │ │
│  │      ↓              │   │  ...                             │ │
│  │  Actions 실행!       │   └──────────────────────────────────┘ │
│  └─────────────────────┘              │ 필요할 때 꺼내서 사용     │
└──────────────────────────┬────────────┼─────────────────────────┘
                           │            │
                           ↓            ↓
┌─────────────────────────────────────────────────────────────────┐
│  ⚙️ GitHub Actions (GitHub 서버에서 자동 실행)                  │
│                                                                  │
│  1. 백엔드 빌드  →  Docker Hub 업로드                           │
│  2. 프론트 빌드  →  Docker Hub 업로드                           │
│     (이때 Secrets에서 VITE_KAKAO_CLIENT_ID 꺼내서 코드에 삽입)  │
│  3. EC2 SSH 접속                                                 │
│     → Secrets 값들로 .env 파일 생성                             │
│     → 최신 이미지 다운로드 & 컨테이너 재시작                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│  ☁️ EC2 서버 (AWS 클라우드)                                     │
│                                                                  │
│  .env 파일 (Secrets에서 만들어진 값들)                           │
│  ┌──────────────────────────────────────────┐                   │
│  │ KAKAO_CLIENT_ID=1285ad1e...              │                   │
│  │ KAKAO_CLIENT_SECRET=E14Wm...             │                   │
│  │ DB_PASSWORD=Damoyeo1234!                 │                   │
│  └──────────────────────────────────────────┘                   │
│              │ docker-compose가 읽어서 컨테이너에 주입           │
│              ↓                                                   │
│  ┌──────────┐  ┌────────────────┐  ┌─────────────────────────┐ │
│  │ mariadb  │  │    backend     │  │       frontend          │ │
│  │ (DB)     │  │  (Spring Boot) │  │  (nginx + React 앱)     │ │
│  └──────────┘  └────────────────┘  └─────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ↓
                 🌐 브라우저에서 접속!
              http://13.125.80.131
```

---

## 핵심 개념 이해하기

### Docker란?
> "어떤 컴퓨터에서도 똑같이 실행되는 독립된 실행 환경"

음식으로 비유하면 **밀키트**와 같습니다:

```
이미지 (Image)      = 밀키트 박스  → Docker Hub에 저장됨
컨테이너 (Container) = 밀키트를 실제로 요리한 상태  → 실행 중인 서버
```

### GitHub Repository Secrets란?
> "GitHub이 안전하게 보관해주는 비밀 금고"

- DB 비밀번호, API 키 같은 **민감한 정보**를 코드에 직접 쓰면 안 됩니다
- GitHub Secrets에 저장하면 **암호화**되어 보관됩니다
- GitHub Actions 실행 시 `${{ secrets.변수명 }}` 으로 꺼내 사용합니다
- 로그에도 `***` 로 가려져서 절대 노출되지 않습니다

```
GitHub Secrets 금고
┌─────────────────────────────────────────┐
│  DOCKER_USERNAME    = myid              │  ← Docker Hub 아이디
│  DOCKER_PASSWORD    = mypassword        │  ← Docker Hub 비밀번호
│  EC2_HOST           = 13.125.80.131     │  ← 서버 IP
│  EC2_SSH_KEY        = -----BEGIN...     │  ← 서버 접속 키
│  DB_PASSWORD        = Damoyeo1234!      │  ← DB 비밀번호
│  JWT_SECRET         = abcd1234...       │  ← JWT 암호화 키
│  KAKAO_CLIENT_ID    = 1285ad1e...       │  ← 카카오 REST API 키
│  KAKAO_CLIENT_SECRET = E14Wmz...        │  ← 카카오 Client Secret
│  VITE_KAKAO_CLIENT_ID = 1285ad1e...     │  ← 프론트 빌드용 카카오 키
│  VITE_API_HOST      = http://13....     │  ← 프론트 빌드용 API 주소
│  ...                                    │
└─────────────────────────────────────────┘
```

---

## 관련 파일 설명

### 1. `backend/Dockerfile` — 백엔드 이미지 만드는 설계도

```dockerfile
# 1. Java 17 환경 준비
FROM eclipse-temurin:17

# 2. Gradle로 Spring Boot 프로젝트 빌드
RUN ./gradlew build

# 3. 빌드된 .jar 파일 복사
COPY build/libs/app.jar app.jar

# 4. 실행 명령 설정
ENTRYPOINT ["java", "-jar", "app.jar"]
```

이 과정으로 만들어진 이미지가 Docker Hub에 올라갑니다:
`myid/damoyeo-backend:latest`

### 2. `frontend/Dockerfile` — 프론트엔드 이미지 만드는 설계도

```dockerfile
# 1. Node.js 환경 준비
FROM node:20

# 2. 패키지 설치
RUN npm install

# ⚠️ 핵심: 빌드 시 Secrets 값이 코드에 삽입됨!
# VITE_KAKAO_CLIENT_ID 같은 값이 이 단계에서 JS 파일 안에 박힘
ARG VITE_KAKAO_CLIENT_ID
RUN npm run build

# 3. nginx 환경으로 전환
FROM nginx:alpine

# 4. 빌드된 React 파일을 nginx 폴더에 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 5. nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

> ⚠️ **중요**: `VITE_` 로 시작하는 환경변수는 빌드 시점에 JavaScript 코드 안에
> **직접 삽입**됩니다. 서버에서 나중에 바꿔도 소용없고, **빌드를 다시 해야** 반영됩니다.
> 이것이 GitHub Secrets의 `VITE_KAKAO_CLIENT_ID`가 별도로 존재하는 이유입니다.

### 3. `frontend/nginx.conf` — 웹 서버 설정

nginx가 요청을 어떻게 처리할지 정의합니다:

```
브라우저 요청 → nginx (80포트)
    │
    ├── /api/member/login  →  backend:8080 으로 전달 (프록시)
    │                         Spring Boot가 실제 처리
    │
    ├── /ws/               →  backend:8080 으로 전달 (WebSocket)
    │                         채팅 기능
    │
    ├── /uploads/          →  backend:8080 으로 전달
    │                         프로필 이미지 등
    │
    └── / (그 외 모든 것)  →  index.html 반환
                              브라우저의 React Router가 라우팅 처리
```

**왜 nginx가 필요한가?**

```
브라우저는 http://13.125.80.131 하나의 주소만 알고 있습니다.
포트 80으로 모든 요청이 들어옵니다.

nginx가 없다면?
  → React 앱도, API도 각각 다른 포트를 사용해야 함
  → 브라우저에서 포트를 직접 입력해야 해서 불편함

nginx가 있다면?
  → 80포트 하나로 모든 요청을 받아서
  → /api/ 요청은 백엔드(8080)로 보내고
  → 나머지는 React 파일로 응답하는 교통정리 역할
```

### 4. `docker-compose.prod.yml` — 3개 컨테이너 통합 관리

```yaml
services:
  mariadb:           # 데이터베이스 컨테이너
    environment:
      MYSQL_PASSWORD: ${DB_PASSWORD}    # ← .env 파일에서 읽어옴

  backend:           # Spring Boot API 컨테이너
    environment:
      KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID}        # ← .env 파일에서 읽어옴
      KAKAO_CLIENT_SECRET: ${KAKAO_CLIENT_SECRET:-}  # ← .env 파일에서 읽어옴
    depends_on:
      mariadb:
        condition: service_healthy    # mariadb 준비 완료 후 실행

  frontend:          # nginx + React 컨테이너
    ports:
      - "80:80"      # EC2의 80포트 → 컨테이너 80포트 연결
    depends_on:
      - backend      # backend 실행 후 실행
```

**`.env` 파일과의 관계:**

```
docker-compose.prod.yml:       .env 파일:
┌─────────────────────────┐    ┌────────────────────────────────┐
│ KAKAO_CLIENT_ID:         │    │ KAKAO_CLIENT_ID=1285ad1e...    │
│   ${KAKAO_CLIENT_ID}    │ ←─ │ KAKAO_CLIENT_SECRET=E14Wm...   │
│                         │    │ DB_PASSWORD=Damoyeo1234!        │
│ DB_PASSWORD:            │    │ ...                            │
│   ${DB_PASSWORD}        │ ←─ └────────────────────────────────┘
└─────────────────────────┘              ↑
                                  GitHub Secrets 값으로
                                  deploy.yml이 생성한 파일
```

### 5. `.github/workflows/deploy.yml` — 자동 배포 스크립트

`git push`가 발생하면 GitHub이 자동으로 실행하는 스크립트입니다.
각 단계에서 GitHub Secrets 값이 어떻게 사용되는지 표시했습니다:

```
[Step 1] 코드 체크아웃
    └─ GitHub 저장소의 최신 코드를 GitHub Actions 가상 컴퓨터로 복사

[Step 2] Docker Buildx 설정
    └─ 이미지를 효율적으로 빌드하기 위한 도구 준비

[Step 3] Docker Hub 로그인
    └─ secrets.DOCKER_USERNAME  ← GitHub Secrets에서 꺼냄
    └─ secrets.DOCKER_PASSWORD  ← GitHub Secrets에서 꺼냄
    └─ 이 값으로 Docker Hub에 로그인

[Step 4] 백엔드 이미지 빌드 & 푸시
    └─ backend/Dockerfile 실행
    └─ Gradle 빌드 (Java 컴파일 + .jar 생성)
    └─ secrets.DOCKER_USERNAME/damoyeo-backend:latest 로 Docker Hub에 업로드

[Step 5] 프론트엔드 이미지 빌드 & 푸시
    └─ frontend/Dockerfile 실행
    └─ build-args 로 Secrets 값 주입:
       ┌──────────────────────────────────────────────────────┐
       │ VITE_API_HOST       ← secrets.VITE_API_HOST 꺼냄    │
       │ VITE_KAKAO_CLIENT_ID ← secrets.VITE_KAKAO_CLIENT_ID │
       └──────────────────────────────────────────────────────┘
    └─ npm run build 실행 → 이 값들이 JS 코드 안에 삽입됨!
    └─ Docker Hub에 업로드

[Step 6] EC2 배포
    └─ secrets.EC2_HOST     ← GitHub Secrets에서 서버 IP 꺼냄
    └─ secrets.EC2_SSH_KEY  ← GitHub Secrets에서 SSH 접속 키 꺼냄
    └─ SSH로 EC2 서버 접속 후:

    ① .env 파일 새로 생성 (매 배포마다 덮어씀)
       ┌─────────────────────────────────────────────────────────┐
       │ DOCKER_USERNAME    ← secrets.DOCKER_USERNAME 꺼냄       │
       │ DB_USERNAME        ← secrets.DB_USERNAME 꺼냄           │
       │ DB_PASSWORD        ← secrets.DB_PASSWORD 꺼냄           │
       │ JWT_SECRET         ← secrets.JWT_SECRET 꺼냄            │
       │ KAKAO_CLIENT_ID    ← secrets.KAKAO_CLIENT_ID 꺼냄       │
       │ KAKAO_CLIENT_SECRET ← secrets.KAKAO_CLIENT_SECRET 꺼냄  │
       │ ...                                                      │
       └─────────────────────────────────────────────────────────┘
       이 .env 파일을 docker-compose가 읽어서 컨테이너에 주입

    ② Docker Hub에서 최신 이미지 다운로드
       docker compose pull

    ③ 컨테이너 재시작 (.env 값이 컨테이너 환경변수로 들어감)
       docker compose up -d

    ④ 오래된 이미지 삭제 (디스크 절약)
       docker image prune -f
```

---

## Secrets 값이 두 가지 방식으로 사용되는 이유

이 프로젝트에 `VITE_KAKAO_CLIENT_ID`와 `KAKAO_CLIENT_ID` 두 개가 따로 있는 이유:

```
VITE_KAKAO_CLIENT_ID (프론트엔드용)
├─ 사용 시점: 빌드 시 (npm run build)
├─ 사용 방법: JavaScript 코드 안에 직접 삽입됨
├─ 역할: 카카오 로그인 버튼 클릭 시 카카오 인가 페이지 URL 생성에 사용
└─ 저장 위치: 빌드된 JS 파일 안 (브라우저에서 실행)

KAKAO_CLIENT_ID (백엔드용)
├─ 사용 시점: 런타임 (서버 실행 중)
├─ 사용 방법: .env → docker-compose → 컨테이너 환경변수 → Spring Boot
├─ 역할: 카카오로부터 액세스 토큰 발급 시 사용
└─ 저장 위치: 백엔드 서버 메모리 (서버에서 실행)

카카오 로그인 흐름:
브라우저에서 VITE_KAKAO_CLIENT_ID로 카카오 인가 요청
    ↓
카카오가 인가 코드(code) 발급해서 redirect
    ↓
백엔드가 KAKAO_CLIENT_ID + KAKAO_CLIENT_SECRET으로 카카오에 토큰 요청
    ↓
카카오 사용자 정보 받아서 로그인 처리

⚠️ 두 값이 다르면 카카오에서 401 에러 발생!
   (카카오 입장에서 "인가 요청한 앱"과 "토큰 요청한 앱"이 다름)
```

---

## git push 이후 전체 흐름 상세

```
1. git push origin main
   └─ 로컬 코드가 GitHub에 업로드

2. GitHub Actions 자동 시작
   └─ .github/workflows/deploy.yml 실행 시작
   └─ GitHub Secrets 금고가 열려서 필요한 값들을 준비

3. 백엔드 Docker 이미지 빌드
   └─ backend/Dockerfile 읽기
   └─ Gradle 빌드 (Java 컴파일 → app.jar 생성)
   └─ 이미지 완성 → Docker Hub에 업로드
      "myid/damoyeo-backend:latest" 로 저장

4. 프론트엔드 Docker 이미지 빌드
   └─ frontend/Dockerfile 읽기
   └─ Secrets에서 VITE_API_HOST, VITE_KAKAO_CLIENT_ID 꺼내기
   └─ npm run build 실행
      (이때 VITE_ 값들이 JS 파일 안에 직접 삽입!)
   └─ nginx + 빌드된 파일로 이미지 완성 → Docker Hub에 업로드
      "myid/damoyeo-frontend:latest" 로 저장

5. EC2 서버에 SSH 접속
   └─ Secrets의 EC2_SSH_KEY (.pem 내용)로 접속
   └─ EC2_HOST (서버 IP)로 연결

6. .env 파일 새로 생성 (이전 내용 덮어씀)
   └─ Secrets에서 값들을 꺼내서 .env 파일 작성:
      KAKAO_CLIENT_ID=1285ad1e...
      KAKAO_CLIENT_SECRET=E14Wmz...
      DB_PASSWORD=...
      JWT_SECRET=...
      ...

7. Docker Hub에서 최신 이미지 다운로드
   └─ docker compose pull
   └─ 방금 빌드해서 올린 최신 이미지를 EC2로 가져옴

8. 컨테이너 재시작
   └─ docker compose up -d
   └─ docker-compose.prod.yml이 .env 파일 읽기
   └─ 각 컨테이너에 환경변수 주입하여 실행:
      mariadb  → DB_PASSWORD 등 주입 → DB 시작
      backend  → KAKAO_CLIENT_ID, DB_URL 등 주입 → Spring Boot 시작
      frontend → nginx 시작 → React 앱 서빙 준비

9. 배포 완료
   └─ 브라우저에서 http://13.125.80.131 접속 가능
```

---

## 컨테이너끼리 통신하는 방법

같은 docker-compose로 실행된 컨테이너들은 **서로의 이름으로 통신**합니다:

```
nginx.conf 안:
    proxy_pass http://backend:8080;
                          ↑
               컨테이너 이름 = "backend"
               IP 주소를 몰라도 이름으로 찾아감

docker-compose.prod.yml 안:
    DB_URL: jdbc:mariadb://mariadb:3306/damoyeo
                               ↑
                    컨테이너 이름 = "mariadb"

→ 세 컨테이너가 같은 네트워크(damoyeo-network)에 속해 있어서 가능
```

---

## 각 파일 수정 시 반영 방법

| 수정 내용 | 수정 파일 | 반영 방법 |
|-----------|-----------|-----------|
| API 로직 변경 | `backend/` 코드 | `git push` → Actions가 자동 빌드·배포 |
| 화면(UI) 변경 | `frontend/src/` 코드 | `git push` → Actions가 자동 빌드·배포 |
| nginx 설정 변경 | `frontend/nginx.conf` | `git push` → 프론트 이미지 재빌드 |
| 새 환경변수 추가 | `docker-compose.prod.yml` + `deploy.yml` + GitHub Secrets | `git push` + EC2에 SCP로 최신 파일 전송 |
| 환경변수 값만 변경 | GitHub Secrets 수정 | `git push` 또는 Actions 수동 실행 |
| EC2 즉시 반영 (빠른 테스트) | EC2 `.env` 직접 수정 | `docker compose up -d --no-deps backend` |
| VITE_ 값 변경 | GitHub Secrets 수정 | `git push` 필수 (프론트 이미지 재빌드 필요) |

---

## 자주 쓰는 EC2 명령어 모음

```bash
# 컨테이너 상태 확인 (모두 "Up" 이어야 정상)
docker compose -f docker-compose.prod.yml ps

# 백엔드 로그 실시간 보기 (에러 확인)
docker compose -f docker-compose.prod.yml logs -f backend

# 프론트 로그 보기
docker compose -f docker-compose.prod.yml logs -f frontend

# 백엔드만 재시작 (DB/프론트 영향 없음)
docker compose -f docker-compose.prod.yml up -d --no-deps backend

# 컨테이너 안 환경변수 확인 (실제로 값이 들어갔는지 검증)
docker exec damoyeo-backend env | grep KAKAO
docker exec damoyeo-backend env | grep DB

# .env 파일 내용 확인
cat ~/damoyeo/.env

# 전체 재시작
docker compose -f docker-compose.prod.yml up -d
```

---

## 문제 발생 시 체크리스트

```
카카오 로그인 502 에러?
├─ docker exec damoyeo-backend env | grep KAKAO
│   → KAKAO_CLIENT_ID, KAKAO_CLIENT_SECRET 둘 다 값이 있어야 함
├─ cat ~/damoyeo/.env | grep KAKAO
│   → .env에 두 값이 올바르게 있어야 함
└─ grep KAKAO ~/damoyeo/docker-compose.prod.yml
    → docker-compose에 두 변수가 정의되어 있어야 함

컨테이너가 실행 안 됨?
├─ docker compose -f docker-compose.prod.yml ps
│   → 상태 확인
└─ docker compose -f docker-compose.prod.yml logs backend
    → 에러 메시지 확인

새 Secrets 값이 반영 안 됨?
└─ git push 또는 GitHub Actions 수동 실행 필요
   (Secrets는 Actions 실행 시에만 .env에 반영됨)
```
