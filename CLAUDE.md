# CLAUDE.md - 다모여 (DaMoYeo) 프로젝트

이 파일은 Claude Code가 다모여 프로젝트 작업 시 참조할 수 있는 가이드입니다.

## 프로젝트 개요

**다모여**는 소모임(somoim.co.kr)을 참조한 오프라인 모임 플랫폼입니다.
- 지역/관심사 기반 모임 생성 및 가입
- 정기모임(정모) 일정 관리 및 참석
- 모임 내 채팅 (Phase 2)
- 위치 기반 근처 모임 검색 (Phase 2)

## 프로젝트 구조

```
damoyeo/
├── frontend/          # React 19 + Vite 7 + TypeScript
├── backend/           # Spring Boot 3.1 + Java 17 (예정)
└── CLAUDE.md          # 통합 문서 (이 파일)
```

## 빠른 시작

### Frontend
```bash
cd frontend
npm install
npm run dev          # 개발 서버 (http://localhost:5173)
npm run build        # 프로덕션 빌드
npm run lint         # ESLint 검사
```

### Backend
```bash
cd backend
./gradlew bootRun    # 서버 실행 (http://localhost:8080)
./gradlew build      # 프로젝트 빌드
./gradlew test       # 테스트 실행
```

> **참고**: Java 17은 IntelliJ IDEA에서 제공하는 JDK를 사용합니다.
> 터미널에서 Gradle 빌드 시 Java 버전 오류가 발생하면, IntelliJ 내장 터미널을 사용하거나
> IntelliJ의 Run Configuration으로 실행하세요.

### 정적 리소스 접근
- 프로필 이미지: `http://localhost:8080/uploads/profiles/{filename}`
- 모임 이미지: `http://localhost:8080/uploads/groups/{filename}` (예정)

---

## 기술 스택

### Frontend
| 기술 | 버전 | 용도 |
|-----|------|------|
| React | 19 | UI 프레임워크 |
| TypeScript | 5.9 | 타입 안전성 |
| Vite | 7+ | 빌드 도구 |
| React Router | 7 | 라우팅 (lazy loading) |
| Zustand | 5 | 전역 상태 (인증, 알림) |
| TanStack Query | 5.90 | 서버 상태 + 캐싱 |
| Axios | 1.13 | HTTP 클라이언트 |
| Tailwind CSS | 4 | 스타일링 |
| react-cookie | 8 | 쿠키 관리 |

### Backend
| 기술 | 버전 | 용도 |
|-----|------|------|
| Spring Boot | 3.1 | 백엔드 프레임워크 |
| Java | 17 | 언어 |
| Spring Security | 6.x | 인증/인가 |
| JWT (jjwt) | 0.11.5 | 토큰 기반 인증 |
| JPA + QueryDSL | 5.0.0 | ORM + 동적 쿼리 |
| MariaDB | - | 데이터베이스 |
| Lombok | - | 보일러플레이트 코드 제거 |
| SpringDoc OpenAPI | 2.1.0 | Swagger UI |
| Gson | 2.10.1 | JSON 처리 |

> **DTO 변환**: ModelMapper 대신 Builder 패턴 사용 (더 명시적이고 타입 안전)

---

## 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────┐
│  Frontend (localhost:5173)                                       │
│  React 19 + TypeScript + Vite 7                                  │
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────┐         │
│  │ Zustand     │  │ TanStack    │  │ jwtAxios         │         │
│  │ (인증/알림)  │  │ Query       │  │ (JWT 인터셉터)   │         │
│  └──────┬──────┘  └──────┬──────┘  └────────┬─────────┘         │
└─────────┼────────────────┼──────────────────┼───────────────────┘
          │                │                  │
          └────────────────┼──────────────────┘
                           │ HTTP (REST API)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Backend (localhost:8080)                                        │
│  Spring Boot 3.1 + Java 17                                       │
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │ JWTCheck    │→ │ Controller  │→ │ Service     │              │
│  │ Filter      │  │ (REST)      │  │ (Interface  │              │
│  └─────────────┘  └─────────────┘  │  + Impl)    │              │
│                                     └──────┬──────┘              │
│                                            ▼                     │
│                                     ┌─────────────┐              │
│                                     │ Repository  │              │
│                                     │ (JPA+QDsl)  │              │
│                                     └──────┬──────┘              │
└────────────────────────────────────────────┼────────────────────┘
                                             ▼
                                      ┌─────────────┐
                                      │  MariaDB    │
                                      │  (damoyeo)  │
                                      └─────────────┘
```

---

## 핵심 도메인

### Member (회원)
```typescript
interface MemberInfo {
  email: string;
  nickname: string;
  profileImage: string;
  accessToken: string;
  refreshToken: string;
  roleNames: string[];  // USER, ADMIN, PREMIUM
}

// 회원 요약 정보 (중첩 객체용)
interface MemberSummary {
  id: number;
  nickname: string;
  profileImage?: string;
}
```

### Category (카테고리)
```typescript
interface Category {
  id: number;
  name: string;
  icon: string;
  displayOrder: number;
}
```
**18개 카테고리**: 운동/스포츠, 사교/인맥, 아웃도어/여행, 문화/공연, 음악/악기, 외국어, 독서, 스터디, 게임/오락, 사진/영상, 요리, 공예, 자기계발, 봉사활동, 반려동물, IT/개발, 금융/재테크, 기타

### Group (모임)
```typescript
interface GroupDTO {
  id: number;
  name: string;
  description: string;
  category: Category;           // 중첩 객체
  coverImage: string;
  thumbnailImage: string;
  location?: { lat: number; lng: number };  // 중첩 객체
  address: string;
  maxMembers: number;
  memberCount: number;
  isPublic: boolean;
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED';
  owner: MemberSummary;         // 중첩 객체
  myRole?: 'OWNER' | 'MANAGER' | 'MEMBER' | null;
  myStatus?: 'APPROVED' | 'PENDING' | null;
  createdAt: string;
  updatedAt: string;
}

interface GroupMemberDTO {
  id: number;
  member: MemberSummary;        // 중첩 객체
  role: 'OWNER' | 'MANAGER' | 'MEMBER';
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  joinedAt: string;
}
```

### Meeting (정모)
```typescript
interface MeetingDTO {
  id: number;
  groupId: number;
  groupName: string;
  title: string;
  description?: string;
  location?: { lat: number; lng: number };  // 중첩 객체
  address?: string;
  meetingDate: string;  // ISO 8601
  maxAttendees: number;
  currentAttendees: number;
  fee: number;
  status: 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  createdBy: MemberSummary;     // 중첩 객체
  myStatus?: 'ATTENDING' | 'MAYBE' | 'NOT_ATTENDING' | null;
  createdAt: string;
}

interface MeetingAttendeeDTO {
  id: number;
  member: MemberSummary;        // 중첩 객체
  status: 'ATTENDING' | 'MAYBE' | 'NOT_ATTENDING';
  registeredAt: string;
}
```

### Notification (알림)
```typescript
interface NotificationDTO {
  id: number;
  type: NotificationType;
  title: string;
  content: string;
  referenceId: number | null;
  referenceType: 'GROUP' | 'MEETING' | 'SYSTEM';
  isRead: boolean;
  createdAt: string;
}

// 알림 타입
type NotificationType =
  | 'WELCOME'           // 회원가입 환영 (referenceType: SYSTEM)
  | 'JOIN_APPROVED'     // 가입 승인 (referenceType: GROUP)
  | 'JOIN_REJECTED'     // 가입 거절 (referenceType: GROUP)
  | 'MEMBER_JOINED'     // 새 멤버 가입 (referenceType: GROUP)
  | 'ROLE_CHANGED'      // 역할 변경 (referenceType: GROUP)
  | 'NEW_MEETING'       // 새 정모 생성 (referenceType: MEETING)
  | 'MEETING_REMINDER'  // 정모 리마인더 (referenceType: MEETING)
  | 'MEETING_CANCELLED';// 정모 취소 (referenceType: MEETING)
```

---

## API 엔드포인트

### 인증 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| POST | `/api/member/login` | 로그인 | X |
| GET | `/api/member/refresh` | 토큰 갱신 | O |
| POST | `/api/member/signup` | 회원가입 | X |
| GET | `/api/member/profile` | 프로필 조회 | O |
| PUT | `/api/member/modify` | 프로필 수정 | O |
| POST | `/api/member/profile/image` | 프로필 이미지 업로드 | O |
| GET | `/api/member/check/email` | 이메일 중복 확인 | X |
| GET | `/api/member/check/nickname` | 닉네임 중복 확인 | X |
| GET | `/api/member/kakao` | 카카오 로그인 | X |

### 이메일 인증 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| POST | `/api/email/send` | 인증 코드 발송 | X |
| POST | `/api/email/verify` | 인증 코드 검증 | X |
| GET | `/api/email/status` | 인증 상태 확인 | X |

### 모임 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/groups` | 모임 목록 (검색/필터) | O |
| POST | `/api/groups` | 모임 생성 | O |
| GET | `/api/groups/{id}` | 모임 상세 | O |
| PUT | `/api/groups/{id}` | 모임 수정 | O (OWNER/MANAGER) |
| DELETE | `/api/groups/{id}` | 모임 삭제 | O (OWNER) |
| POST | `/api/groups/{id}/join` | 가입 신청 | O |
| POST | `/api/groups/{id}/leave` | 탈퇴 | O |
| GET | `/api/groups/{id}/members` | 멤버 목록 | O |
| GET | `/api/groups/my` | 내 모임 | O |
| GET | `/api/groups/nearby` | 근처 모임 | O |
| GET | `/api/groups/recommended` | 추천 모임 | O |

### 정모 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/groups/{gid}/meetings` | 모임의 정모 목록 | O |
| POST | `/api/groups/{gid}/meetings` | 정모 생성 | O (OWNER/MANAGER) |
| GET | `/api/meetings/{id}` | 정모 상세 | O |
| PUT | `/api/meetings/{id}` | 정모 수정 | O (OWNER/MANAGER) |
| DELETE | `/api/meetings/{id}` | 정모 취소 | O (OWNER/MANAGER) |
| POST | `/api/meetings/{id}/attend` | 참석 등록 | O |
| DELETE | `/api/meetings/{id}/attend` | 참석 취소 | O |
| GET | `/api/meetings/upcoming` | 다가오는 정모 | O |
| GET | `/api/meetings/my` | 내 정모 | O |

### 카테고리 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/categories` | 카테고리 목록 | X |

### 알림 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/notifications` | 알림 목록 | O |
| PUT | `/api/notifications/{id}/read` | 읽음 처리 | O |
| PUT | `/api/notifications/read-all` | 전체 읽음 | O |
| GET | `/api/notifications/unread-count` | 안 읽은 수 | O |

---

## 프론트엔드 디렉토리 구조

```
frontend/src/
├── api/                    # API 클라이언트
│   ├── memberApi.tsx       # 인증/프로필
│   ├── groupApi.tsx        # 모임 CRUD
│   ├── meetingApi.tsx      # 정모
│   ├── categoryApi.tsx     # 카테고리
│   └── notificationApi.tsx # 알림
│
├── components/
│   ├── common/             # 공통 UI (PageComponent, Avatar, etc.)
│   ├── member/             # 인증/프로필 관련
│   ├── group/              # 모임 관련 (GroupCard, etc.)
│   ├── meeting/            # 정모 관련 (MeetingCard, etc.)
│   └── menus/              # 네비게이션 (Header, BottomNav)
│
├── hooks/                  # 커스텀 훅
│   ├── useCustomLogin.tsx  # 인증 로직
│   ├── useCustomMove.tsx   # 네비게이션
│   ├── useGroups.tsx       # TanStack Query (모임)
│   └── useMeetings.tsx     # TanStack Query (정모)
│
├── layouts/
│   └── MobileLayout.tsx    # 모바일 레이아웃
│
├── pages/
│   ├── MainPage.tsx        # 홈 피드
│   ├── NotificationPage.tsx
│   ├── member/             # 로그인, 프로필, 내모임
│   ├── group/              # 목록, 상세, 생성, 관리
│   ├── meeting/            # 목록, 상세, 생성
│   └── search/             # 검색
│
├── router/
│   ├── root.tsx            # 메인 라우터
│   ├── groupRouter.tsx
│   ├── meetingRouter.tsx
│   ├── memberRouter.tsx
│   └── ProtectedRoute.tsx  # 인증 보호
│
├── types/                  # TypeScript 타입 정의
│   ├── global.d.ts
│   ├── member.d.ts
│   ├── group.d.ts
│   ├── meeting.d.ts
│   ├── category.d.ts
│   └── notification.d.ts
│
├── util/
│   ├── jwtUtil.ts          # JWT 인터셉터
│   ├── cookieUtil.ts       # 쿠키 유틸
│   └── dateUtil.ts         # 날짜 포맷팅
│
└── zstore/                 # Zustand 스토어
    ├── useZustandMember.ts # 인증 상태
    ├── useZustandMyGroups.ts
    └── useZustandNotifications.ts
```

---

## 상태 관리 전략

| 상태 유형 | 도구 | 용도 |
|----------|------|------|
| 사용자 인증 | Zustand + Cookie | 로그인 정보, 토큰 |
| 내 모임 목록 | Zustand | 빠른 접근용 캐시 |
| 알림 | Zustand | 실시간 알림 상태 |
| 서버 데이터 | TanStack Query | 모임/정모 목록, 상세 |
| URL 상태 | useSearchParams | 검색 필터, 페이지 번호 |

---

## 라우팅 구조

```
/                          → MainPage (홈 피드)
├── /search                → SearchPage (통합 검색)
├── /notifications         → NotificationPage
│
├── /groups                → Redirect to /groups/list
│   ├── /groups/list       → GroupListPage
│   ├── /groups/create     → GroupCreatePage
│   ├── /groups/:groupId   → GroupDetailPage
│   └── /groups/:groupId/manage → GroupManagePage
│
├── /meetings              → MeetingListPage
│   ├── /meetings/:meetingId → MeetingDetailPage
│   └── /meetings/create/:groupId → MeetingCreatePage
│
└── /member
    ├── /member/login      → LoginPage
    ├── /member/signup     → SignupPage
    ├── /member/profile    → ProfilePage
    ├── /member/my-groups  → MyGroupsPage
    └── /member/kakao      → KakaoRedirectPage
```

---

## 인증 플로우 (JWT)

### 토큰 설정
- **Access Token**: 10분
- **Refresh Token**: 24시간 (만료 1시간 전 자동 갱신)

### 인증 흐름
```
1. 로그인 요청
   Frontend: memberApi.loginPost(email, pw)
   Backend:  POST /api/member/login (form-urlencoded)

2. 토큰 저장
   Frontend: setCookie("member", {email, accessToken, refreshToken, ...})

3. API 요청
   Frontend: jwtAxios 인터셉터가 Authorization: Bearer {accessToken} 헤더 추가

4. 토큰 갱신 (Access Token 만료 시)
   Frontend: jwtUtil.ts 응답 인터셉터가 ERROR_ACCESS_TOKEN 감지
             → GET /api/member/refresh 자동 호출
```

---

## 개발 단계

### Phase 1 - MVP (완료)
- [x] 회원 시스템 (로그인/회원가입/프로필)
- [x] 모임 CRUD + 가입/탈퇴
- [x] 정모 CRUD + 참석
- [x] 검색/필터
- [x] 카테고리 시스템
- [x] 이메일 인증

### Phase 2 - 핵심 기능 (진행 중)
- [x] 위치 기반 검색 (Geolocation + Spatial Query)
- [x] 무한 스크롤
- [x] 프로필 이미지 업로드
- [x] 프로필 수정 기능
- [x] 멤버 관리 (역할 변경, 강퇴, 가입 승인/거절)
- [x] 모임/정모 수정 기능
- [x] 알림 시스템 (회원가입 환영 알림, 폴링 기반)
- [ ] 실시간 채팅 (WebSocket/STOMP)
- [ ] 실시간 알림 (SSE/WebSocket)

### Phase 3 - 고도화
- [ ] PWA + 푸시 알림
- [ ] 프리미엄 기능
- [ ] 관리자 대시보드

---

## 백엔드 구조

### 패키지 구조
```
backend/src/main/java/com/damoyeo/api/
├── domain/                          # 도메인별 모듈
│   ├── member/                      # 회원
│   │   ├── controller/
│   │   ├── service/                 # Interface + Impl
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── group/                       # 모임
│   ├── meeting/                     # 정모
│   ├── notification/                # 알림
│   ├── category/                    # 카테고리
│   └── email/                       # 이메일 인증
│
├── global/                          # 공통 모듈
│   ├── config/                      # 설정
│   │   ├── SecurityConfig.java      # Spring Security
│   │   ├── WebConfig.java           # CORS, 정적 리소스
│   │   └── MailConfig.java          # 메일 설정
│   ├── security/                    # 보안
│   │   ├── filter/
│   │   │   ├── JWTCheckFilter.java  # JWT 검증 필터
│   │   │   └── LoginFilter.java     # 로그인 처리
│   │   ├── handler/
│   │   └── CustomUserDetailsService.java
│   ├── util/                        # 유틸리티
│   │   ├── JWTUtil.java             # JWT 생성/검증
│   │   └── FileUploadUtil.java      # 파일 업로드
│   ├── common/                      # 공통 DTO
│   │   └── dto/
│   │       ├── PageRequestDTO.java
│   │       └── PageResponseDTO.java
│   └── exception/                   # 예외 처리
│       ├── CustomException.java
│       └── GlobalExceptionHandler.java
│
└── ApiApplication.java              # 메인 클래스
```

### 주요 설계 패턴
- **Service Interface + Impl**: 인터페이스 분리로 테스트 용이성 확보
- **Builder Pattern**: DTO 변환 시 사용 (ModelMapper 대신)
- **@ElementCollection**: 회원 역할(USER, ADMIN, PREMIUM) 저장

### 파일 업로드 구조
```
uploads/                          # 로컬 파일 시스템
├── profiles/                     # 프로필 이미지
│   └── {UUID}.{확장자}           # 예: f47ac10b-58cc-4372.jpg
└── groups/                       # 모임 이미지 (예정)
    └── {UUID}.{확장자}
```

### 보안 설정 (JWTCheckFilter)
JWT 검증을 **건너뛰는** 경로:
- `/api/member/login`, `/api/member/signup` - 인증 없이 접근
- `/api/member/refresh` - 토큰 갱신
- `/api/categories` - 공개 API
- `/uploads/**` - 정적 파일 (프로필 이미지 등)
- `/swagger-ui/**`, `/v3/api-docs/**` - API 문서
