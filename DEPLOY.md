# 다모여 배포 가이드

> AWS EC2 + Docker + GitHub Actions CI/CD

---

## 전체 흐름 한눈에 보기

배포는 총 8단계입니다. 한 번만 설정하면 이후에는 `git push` 한 번으로 자동 배포됩니다.

```
0단계: GitHub 레포지토리 만들고 코드 올리기
1단계: AWS에서 서버 컴퓨터 빌리기 (EC2)
2단계: 빌린 서버에 Docker 설치하기
3단계: 서버에 배포 파일 올리기
4단계: Docker Hub 계정 만들기 (이미지 저장소)
5단계: GitHub에 비밀 설정값 등록하기
6단계: 코드 push → 자동 배포 확인
7단계: 브라우저에서 접속 확인
```

---

## ⚠️ 시작 전 준비

### Git Bash 설치 (Windows 필수)
일반 명령 프롬프트(CMD) 대신 **Git Bash**를 사용해야 합니다.

1. [git-scm.com](https://git-scm.com) 접속
2. **Download for Windows** 클릭 → 설치
3. 설치 후 바탕화면 우클릭 → **"Git Bash Here"** 메뉴가 생깁니다

> 이후 모든 명령어는 **Git Bash**에서 실행하세요.

### 줄바꿈 문자 설정 (이미 clone한 경우)
Windows에서 코드를 받으면 일부 파일이 깨질 수 있습니다.
Git Bash를 열고 프로젝트 폴더에서 아래를 실행하세요:

```bash
git rm --cached -r .
git reset --hard
```

---

## 0단계. GitHub 레포지토리 만들고 코드 올리기

> GitHub에 코드를 올려야 자동 배포(CI/CD)가 작동합니다.

### 0-1. GitHub 레포지토리 생성

1. [github.com](https://github.com) 로그인
2. 우측 상단 **"+"** 버튼 → **"New repository"** 클릭
   또는 직접 접속: `https://github.com/new`
3. 아래와 같이 설정:

   | 항목 | 값 |
   |------|-----|
   | Repository name | `damoyeo` |
   | Visibility | **Public** 또는 Private |
   | Initialize this repository | **체크 안 함** (이미 코드가 있으므로) |

4. **"Create repository"** 클릭
5. 생성 후 표시되는 레포 주소를 메모 (예: `https://github.com/shkim777/damoyeo`)

### 0-2. 로컬 코드를 GitHub에 올리기

프로젝트 루트 폴더(`damoyeo/`)에서 **Git Bash**를 열고:

```bash
# 현재 git 상태 확인
git status

# 변경된 파일 전체 스테이징
git add .

# 첫 커밋
git commit -m "initial commit"

# GitHub 레포를 원격 저장소로 등록 (주소는 본인 것으로 변경)
git remote add origin https://github.com/{계정명}/damoyeo.git

# main 브랜치로 push
git push -u origin main
```

> 이미 `git remote`가 등록되어 있다면 `git remote -v` 로 확인 후 생략하세요.

---

## 1단계. AWS에서 서버 컴퓨터 빌리기

> AWS(아마존 클라우드)에서 무료로 서버를 빌립니다. 신용카드가 필요하지만 1년간 요금이 청구되지 않습니다.

### 1-1. AWS 가입
1. [aws.amazon.com](https://aws.amazon.com/ko/) 접속
2. **"무료로 시작하기"** 클릭
3. 이메일, 비밀번호, 계정 이름 입력 후 가입
4. 카드 정보 입력 (1달러 임시 결제 후 취소됨, 프리티어는 무료)
5. 휴대폰 인증 완료

### 1-2. EC2 서버 만들기
1. 로그인 후 상단 검색창에 **"EC2"** 검색 → 클릭
2. 우측 상단 지역을 **"아시아 태평양(서울)"** 로 변경
3. 주황색 **"인스턴스 시작"** 버튼 클릭
4. 아래와 같이 설정:

   | 항목 | 선택값 |
   |------|--------|
   | 이름 | `damoyeo-server` |
   | AMI(운영체제) | **Ubuntu Server 22.04 LTS** (프리티어 표시된 것) |
   | 인스턴스 유형 | **t2.micro** (프리티어) |
   | 키 페어 | **"새 키 페어 생성"** → 이름: `damoyeo-server` → **".pem 다운로드"** → 저장 |

5. **네트워크 설정** 에서 **"보안 그룹 생성"** 선택 후:
   - "SSH 트래픽 허용" ✅ → 소스: **내 IP**
   - **"다음 위치에서 HTTP 트래픽 허용"** ✅ → 소스: **0.0.0.0/0**

6. **"인스턴스 시작"** 클릭

### 1-3. 고정 IP 설정 (탄력적 IP)
서버를 껐다 켜도 IP가 바뀌지 않도록 고정 IP를 설정합니다.

1. 왼쪽 메뉴 → **"탄력적 IP"** 클릭
2. 주황색 **"탄력적 IP 주소 할당"** 클릭 → **"할당"**
3. 방금 만든 IP 체크 → **"작업"** → **"탄력적 IP 주소 연결"**
4. 인스턴스: `damoyeo-server` 선택 → **"연결"**
5. **이 IP 주소를 메모해두세요** (예: `13.125.123.456`)

---

## 2단계. 서버에 Docker 설치하기

> 방금 만든 서버에 원격으로 접속해서 Docker를 설치합니다.

### 2-1. 서버 접속

`.pem` 파일이 있는 폴더에서 **Git Bash**를 열고:

```bash
# Windows: .pem 파일 권한 설정 (최초 1회, 안 하면 접속 거부됨)
# Git Bash에서 실행 (CMD에서는 %USERNAME% 사용, Git Bash에서는 $(whoami) 사용)
icacls damoyeo-server.pem /inheritance:r /grant:r "$(whoami):R"

# 서버 접속 (damoyeo-server.pem과 IP는 본인 것으로 변경)
ssh -i damoyeo-server.pem ubuntu@13.125.80.131
```

"Are you sure you want to continue connecting?" 라는 질문이 나오면 `yes` 입력 후 엔터

> 접속 성공하면 `ubuntu@ip-xxx:~$` 형태로 프롬프트가 바뀝니다.

### 2-2. Docker 설치

서버에 접속된 상태에서 아래를 **순서대로** 실행:

```bash
# 패키지 목록 업데이트
sudo apt-get update && sudo apt-get upgrade -y

# Docker 설치 (한 줄 명령어)
curl -fsSL https://get.docker.com | sudo sh

# 현재 사용자(ubuntu)에게 Docker 권한 부여
sudo usermod -aG docker ubuntu
```

```bash
# 서버 재접속 (권한 적용을 위해 필수)
exit
ssh -i damoyeo-server.pem ubuntu@13.125.80.131
```

### 2-3. 메모리 부족 방지 (Swap 설정)

t2.micro는 메모리가 1GB뿐이라 Spring Boot 실행 시 부족할 수 있습니다.
아래 명령어로 1GB 가상 메모리를 추가합니다:

```bash
sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### 2-4. 배포 폴더 생성

```bash
mkdir ~/damoyeo
```

---

## 3단계. 서버에 배포 설정 파일 올리기

> 로컬 PC에서 서버로 `docker-compose.prod.yml` 파일을 전송합니다.

**Git Bash를 새로 열고** (서버 접속 상태가 아닌 로컬에서) 프로젝트 폴더로 이동 후:

```bash
# 파일 전송 (damoyeo-server.pem 경로와 IP는 본인 것으로 변경)
scp -i damoyeo-server.pem docker-compose.prod.yml ubuntu@13.125.80.131:~/damoyeo/
```

> **경로에 한글/공백이 있으면 오류 발생**: `.pem` 파일을 `C:/keys/` 같은 심플한 경로로 옮기세요.

---

## 4단계. Docker Hub 계정 만들기

> Docker Hub는 만들어진 서버 이미지를 저장하는 창고입니다. GitHub과 비슷한 개념입니다.

1. [hub.docker.com](https://hub.docker.com) 접속 → **Sign Up**
2. 가입 완료 후 로그인
3. 우측 상단 **"Create repository"** 클릭, 아래 2개 생성:

   | Repository 이름 | Visibility |
   |----------------|------------|
   | `damoyeo-backend` | Public |
   | `damoyeo-frontend` | Public |

4. **Docker Hub 아이디와 비밀번호(또는 Access Token)를 메모해두세요**
   - 일반 가입: 아이디 + 비밀번호 사용
   - 구글/소셜 로그인으로 가입한 경우 비밀번호가 없으므로 **Access Token** 발급 필요
     1. `https://app.docker.com/settings/personal-access-tokens` 접속
     2. **"Generate new token"** 클릭
     3. 이름 입력 (예: `damoyeo-deploy`), Permissions: **Read & Write**
     4. 생성된 토큰 복사 (창 닫으면 다시 볼 수 없음)
     5. 이후 비밀번호 자리에 이 토큰을 사용

---

## 5단계. GitHub에 비밀 설정값 등록하기

> GitHub Actions가 자동 배포할 때 필요한 비밀번호, IP 등을 안전하게 저장합니다.

### 5-1. Secrets 페이지 접속
아래 URL로 직접 접속 (계정명과 레포명 교체):
```
https://github.com/{계정명}/{레포명}/settings/secrets/actions
```
또는 GitHub 저장소 → **Settings** 탭 → 왼쪽 메뉴 **"Secrets and variables"** → **"Actions"** → **"New repository secret"**

### 5-2. 아래 목록을 하나씩 등록

**"New repository secret"** 버튼을 눌러 이름(Name)과 값(Secret)을 입력합니다:

| Name | Secret 값 | 어떻게 구하나요? |
|------|-----------|----------------|
| `DOCKER_USERNAME` | Docker Hub 아이디 | 4단계에서 가입한 아이디 |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 | 4단계에서 가입한 비밀번호 |
| `EC2_HOST` | `13.125.123.456` | 1-3단계에서 메모한 IP |
| `EC2_SSH_KEY` | `.pem` 파일 내용 전체 | 아래 방법으로 확인 |
| `DB_USERNAME` | `damoyeo` | 그대로 입력 |
| `DB_PASSWORD` | 원하는 DB 비밀번호 | 본인이 정하는 값 (예: `Damoyeo1234!`) |
| `DB_ROOT_PASSWORD` | 원하는 root 비밀번호 | 본인이 정하는 값 (예: `Root1234!`) |
| `JWT_SECRET` | 랜덤 문자열 | 아래 방법으로 생성 |
| `MAIL_USERNAME` | Gmail 주소 | 본인 Gmail 주소 |
| `MAIL_PASSWORD` | Gmail 앱 비밀번호 | 아래 방법으로 생성 |
| `CORS_ALLOWED_ORIGINS` | `http://13.125.123.456` | 1-3단계 IP 앞에 `http://` 붙이기 |
| `FRONTEND_URL` | `http://13.125.123.456` | 위와 동일 |
| `VITE_API_HOST` | `http://13.125.123.456` | 위와 동일 |
| `VITE_KAKAO_CLIENT_ID` | 카카오 REST API 키 | [카카오 개발자 콘솔](https://developers.kakao.com/console/app) → 앱 선택 → 앱 → 플랫폼 키 → REST API 키 |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 | `VITE_KAKAO_CLIENT_ID`와 **동일한 값** 입력 (백엔드 카카오 로그인용) |

---

### EC2_SSH_KEY 값 확인 방법

`.pem` 파일이 있는 폴더에서 Git Bash 열고:
```bash
cat damoyeo-server.pem
```
출력된 내용 **전체** (`-----BEGIN RSA PRIVATE KEY-----` 부터 `-----END RSA PRIVATE KEY-----` 까지) 를 복사해서 붙여넣기

---

### JWT_SECRET 생성 방법

Git Bash에서:
```bash
openssl rand -base64 64
```
출력된 문자열을 그대로 복사해서 붙여넣기

Git Bash가 없다면 PowerShell에서:
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Max 256 }) -as [byte[]])
```

---

### Gmail 앱 비밀번호 생성 방법

일반 Gmail 비밀번호가 아닌 **앱 전용 비밀번호**가 필요합니다.

1. [myaccount.google.com](https://myaccount.google.com) 접속
2. **"보안"** 탭 클릭
3. **"2단계 인증"** 활성화 (안 되어 있으면 먼저 설정)
4. 검색창에 **"앱 비밀번호"** 검색 → 클릭
5. 앱 이름 아무거나 입력 (예: `damoyeo`) → **"만들기"**
6. 생성된 **16자리 코드**를 복사 (예: `abcd efgh ijkl mnop`)
   - 공백 없이 붙여서 입력: `abcdefghijklmnop`

---

## 6단계. 자동 배포 실행

모든 설정이 완료되었습니다. 이제 코드를 push하면 자동으로 배포됩니다.

```bash
git push origin main
```

### 배포 진행 상황 확인
1. GitHub 저장소 → **"Actions"** 탭 클릭
2. 가장 최근 워크플로우 클릭
3. 각 단계가 ✅ 로 바뀌는 것을 확인

**첫 배포는 10~15분 소요됩니다** (Gradle 빌드 + npm 빌드 포함)
이후 배포는 캐시 덕분에 **3~5분** 내외

---

## 7단계. 접속 확인

브라우저에서 아래 주소로 접속:

```
http://여기에_EC2_IP        → 다모여 메인 페이지
http://여기에_EC2_IP/api/   → 백엔드 API (JSON 응답 오면 성공)
```

---

## 문제 해결

### 배포 후 사이트가 열리지 않을 때

서버에 SSH 접속 후:
```bash
cd ~/damoyeo

# 컨테이너 상태 확인 (모두 "Up" 이어야 정상)
docker compose -f docker-compose.prod.yml ps

# 백엔드 로그 확인
docker compose -f docker-compose.prod.yml logs --tail=50 backend

# 프론트 로그 확인
docker compose -f docker-compose.prod.yml logs --tail=50 frontend
```

### Spring Boot가 시작되지 않을 때 (메모리 부족)
```bash
# 메모리 사용량 확인
free -h

# Swap이 없으면 2단계 2-3 다시 실행
# 백엔드 재시작
docker compose -f docker-compose.prod.yml restart backend
```

### DB 연결 실패
```bash
# MariaDB가 완전히 시작되기 전에 백엔드가 먼저 뜨는 경우
docker compose -f docker-compose.prod.yml logs mariadb
docker compose -f docker-compose.prod.yml restart backend
```

### 수동으로 재배포하고 싶을 때
GitHub 저장소 → **Actions** 탭 → 왼쪽 **"Deploy to AWS EC2"** → **"Run workflow"** 클릭
