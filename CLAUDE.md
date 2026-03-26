# CLAUDE.md - 다모여 (DaMoYeo) 프로젝트

이 파일은 Claude Code가 다모여 프로젝트 작업 시 참조할 수 있는 가이드입니다.

---

## ⚠️ 필수 개발 지침 (Claude Code 기본 적용)

> **이 프로젝트는 7년차 풀스택 개발자의 포트폴리오용 프로젝트입니다.**
> 모든 코드는 시니어 개발자 수준의 퀄리티와 구조를 갖춰야 합니다.

### 코드 품질 기준

1. **아키텍처 & 설계**
   - 확장 가능하고 유지보수가 용이한 구조 설계
   - SOLID 원칙 준수
   - 적절한 디자인 패턴 적용 (Repository, Service, Factory 등)
   - 관심사 분리 (Separation of Concerns) 철저히 적용

2. **주석 & 문서화 (필수)**
   - 모든 새로운 함수/메서드에 JSDoc(프론트) 또는 Javadoc(백엔드) 주석 작성
   - 복잡한 비즈니스 로직에는 상세한 설명 주석 포함
   - 왜(Why) 이렇게 구현했는지 의도를 명확히 기술
   - API 엔드포인트에는 요청/응답 예시 포함

3. **코드 스타일**
   - 명확하고 의미 있는 변수/함수명 사용
   - 매직 넘버 대신 상수 정의
   - 에러 처리 철저히 (try-catch, 에러 바운더리 등)
   - 타입 안전성 확보 (TypeScript strict, Java Generics 등)

4. **프론트엔드 (React/TypeScript)**
   - 컴포넌트 단위의 모듈화
   - Custom Hooks로 로직 분리
   - 성능 최적화 (React.memo, useMemo, useCallback 적절히 사용)
   - 접근성(a11y) 고려

5. **백엔드 (Spring Boot/Java)**
   - 계층형 아키텍처 (Controller → Service → Repository)
   - DTO 패턴으로 엔티티 노출 방지
   - 적절한 예외 처리 및 글로벌 예외 핸들러
   - 트랜잭션 관리 (@Transactional 적절히 사용)

### 주석 작성 예시

**프론트엔드 (TypeScript/React)**
```typescript
/**
 * 모임 목록을 무한 스크롤로 조회하는 커스텀 훅
 *
 * @description
 * - TanStack Query의 useInfiniteQuery를 활용하여 페이지네이션 처리
 * - 스크롤 위치에 따라 자동으로 다음 페이지 로드
 * - 검색 조건 변경 시 자동으로 캐시 무효화
 *
 * @param params - 검색 필터 조건 (카테고리, 키워드, 정렬 등)
 * @returns 모임 목록 데이터 및 페이지네이션 상태
 *
 * @example
 * const { data, fetchNextPage, hasNextPage } = useGroupsInfinite({
 *   categoryId: 1,
 *   keyword: '운동'
 * });
 */
```

**백엔드 (Java/Spring)**
```java
/**
 * 모임 가입 신청을 처리하는 서비스 메서드
 *
 * <p>가입 신청 프로세스:</p>
 * <ol>
 *   <li>모임 존재 여부 및 활성 상태 확인</li>
 *   <li>중복 가입 신청 검증</li>
 *   <li>최대 인원 초과 여부 확인</li>
 *   <li>가입 신청 엔티티 생성 및 저장</li>
 *   <li>모임장에게 알림 발송</li>
 * </ol>
 *
 * @param groupId 가입할 모임 ID
 * @param memberId 신청자 회원 ID
 * @return 생성된 가입 신청 정보
 * @throws GroupNotFoundException 모임이 존재하지 않는 경우
 * @throws AlreadyJoinedException 이미 가입된 회원인 경우
 * @throws GroupFullException 모임 정원이 초과된 경우
 */
```

---

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
| MSW | 2.12 | API 모킹 (개발용) |

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
  createdAt: string;
  updatedAt: string;
}

interface GroupMemberDTO {
  id: number;
  member: MemberSummary;        // 중첩 객체
  role: 'OWNER' | 'MANAGER' | 'MEMBER';
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
  | 'NEW_MEMBER'        // 새 멤버 가입 (referenceType: GROUP)
  | 'MEMBER_LEFT'       // 멤버 탈퇴 (referenceType: GROUP)
  | 'ROLE_CHANGED'      // 역할 변경 (referenceType: GROUP)
  | 'MEMBER_KICKED'     // 강퇴됨 (referenceType: GROUP)
  | 'GROUP_DISBANDED'   // 모임 해체됨 (referenceType: GROUP)
  | 'GROUP_UPDATE'      // 모임 정보 변경 (referenceType: GROUP)
  | 'NEW_MEETING'       // 새 정모 생성 (referenceType: MEETING)
  | 'MEETING_REMINDER'  // 정모 리마인더 (referenceType: MEETING)
  | 'MEETING_CANCELLED';// 정모 취소 (referenceType: MEETING)
```

### Event (이벤트/배너)
```typescript
type EventType = 'PROMOTION' | 'NOTICE' | 'SPECIAL' | 'FEATURE';

interface EventBannerDTO {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  linkUrl?: string;
  type: EventType;
  startDate: string;
  endDate: string;
}

interface EventDetailDTO extends EventBannerDTO {
  content: string;        // 마크다운 지원
  tags: string[];
  isActive: boolean;
  displayOrder: number;
  createdAt: string;
}
```
**이벤트 타입**: PROMOTION (프로모션), NOTICE (공지), SPECIAL (특별 이벤트), FEATURE (신기능 소개)

### Chat (채팅)
```typescript
type MessageType = "TEXT" | "IMAGE" | "SYSTEM";
type ConnectionStatus = "connecting" | "connected" | "disconnected" | "error";

interface ChatMessageDTO {
  id: number;
  groupId: number;
  sender: MemberSummary | null;    // SYSTEM 메시지는 null
  message: string;
  messageType: MessageType;
  createdAt: string;
}

interface ChatRoomDTO {
  groupId: number;
  groupName: string;
  latestMessage?: ChatMessageDTO;
  unreadCount: number;
}

interface TypingEvent {
  email: string;
  typing: boolean;
}
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
| GET | `/api/groups/{gid}/meetings` | 모임의 정모 목록 (전체) | O |
| GET | `/api/meetings/group/{gid}/upcoming` | 모임의 예정된 정모 (날짜 기반) | O |
| GET | `/api/meetings/group/{gid}/past` | 모임의 지난 정모 (날짜 기반) | O |
| POST | `/api/groups/{gid}/meetings` | 정모 생성 | O (OWNER/MANAGER) |
| GET | `/api/meetings/{id}` | 정모 상세 | O |
| PUT | `/api/meetings/{id}` | 정모 수정 | O (OWNER/MANAGER) |
| DELETE | `/api/meetings/{id}` | 정모 취소 | O (OWNER/MANAGER) |
| POST | `/api/meetings/{id}/attend` | 참석 등록 | O |
| DELETE | `/api/meetings/{id}/attend` | 참석 취소 | O |
| GET | `/api/meetings/upcoming` | 다가오는 정모 | O |
| GET | `/api/meetings/my` | 내 정모 | O |

> **정모 상태 관리**: 스케줄러 대신 날짜 기반 필터링 사용. `meetingDate > now`이면 예정, `meetingDate <= now`이면 지난 정모로 분류.

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

### 이벤트 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/events/banners` | 활성 배너 목록 | X |
| GET | `/api/events/{id}` | 이벤트 상세 | X |
| GET | `/api/events` | 전체 이벤트 목록 (관리자) | O (ADMIN) |
| POST | `/api/events` | 이벤트 생성 | O (ADMIN) |
| DELETE | `/api/events/{id}` | 이벤트 삭제 | O (ADMIN) |
| PATCH | `/api/events/{id}/toggle` | 이벤트 활성화 토글 | O (ADMIN) |

### 채팅 API
| Method | Endpoint | Description | 인증 |
|--------|----------|-------------|------|
| GET | `/api/chat/{groupId}/messages` | 메시지 히스토리 (페이지네이션) | O |
| GET | `/api/chat/{groupId}/unread-count` | 읽지 않은 메시지 개수 | O |
| POST | `/api/chat/{groupId}/read` | 읽음 처리 | O |
| GET | `/api/chat/my-chats` | 내 채팅방 목록 | O |

**WebSocket 엔드포인트:**
- `SEND /app/chat/{groupId}` - 메시지 전송 (클라이언트 → 서버)
- `SUBSCRIBE /topic/chat/{groupId}` - 메시지 수신 (서버 → 클라이언트)
- `SEND /app/chat/{groupId}/typing` - 타이핑 이벤트 전송
- `SUBSCRIBE /topic/chat/{groupId}/typing` - 타이핑 이벤트 수신
- `SUBSCRIBE /user/queue/errors` - 에러 메시지 수신 (개인)

**WebSocket 연결:**
- 엔드포인트: `ws://localhost:8080/ws` (SockJS)
- 인증: CONNECT 프레임의 `Authorization` 헤더에 JWT 토큰 포함
- 프로토콜: STOMP over SockJS

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
/                          → MainPage (홈 피드 + 배너 슬라이더)
├── /search                → SearchPage (통합 검색)
├── /notifications         → NotificationPage
│
├── /events/:eventId       → EventDetailPage (이벤트 상세)
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
- [x] 멤버 관리 (역할 변경, 강퇴)
- [x] 모임/정모 수정 기능
- [x] 알림 시스템 (회원가입 환영 알림, 폴링 기반)
- [x] 이벤트/배너 시스템 (메인 배너 슬라이더, 이벤트 상세 페이지)
- [x] 정모 상태 관리 (날짜 기반 예정/지난 정모 분리)
- [x] 알림 확장 (강퇴/모임해체 알림)
- [x] **실시간 채팅 (WebSocket/STOMP)**
  - [x] WebSocket/STOMP 설정 및 JWT 인증
  - [x] 메시지 송수신 및 히스토리 저장
  - [x] 읽음 상태 추적 (unread count)
  - [x] 타이핑 인디케이터 ("○○○님이 입력 중...")
  - [x] 채팅 별도 페이지 분리 (`/groups/:groupId/chat`)
  - [x] 스마트 자동 스크롤 (내 메시지 무조건 스크롤)
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
│   ├── email/                       # 이메일 인증
│   ├── event/                       # 이벤트/배너
│   └── chat/                        # 채팅 (NEW)
│       ├── controller/
│       │   └── ChatController.java  # REST + WebSocket 엔드포인트
│       ├── service/
│       │   ├── ChatService.java
│       │   └── ChatServiceImpl.java
│       ├── repository/
│       │   ├── ChatMessageRepository.java
│       │   └── ChatReadRepository.java
│       ├── entity/
│       │   ├── ChatMessage.java     # 메시지 엔티티
│       │   ├── ChatRead.java        # 읽음 상태 엔티티
│       │   └── MessageType.java     # TEXT, IMAGE, SYSTEM
│       └── dto/
│           ├── ChatMessageDTO.java
│           ├── SendMessageRequest.java
│           └── ChatRoomDTO.java
│
├── global/                          # 공통 모듈
│   ├── config/                      # 설정
│   │   ├── SecurityConfig.java      # Spring Security
│   │   ├── WebConfig.java           # CORS, 정적 리소스
│   │   ├── WebSocketConfig.java     # WebSocket/STOMP 설정 (NEW)
│   │   └── MailConfig.java          # 메일 설정
│   ├── security/                    # 보안
│   │   ├── filter/
│   │   │   ├── JWTCheckFilter.java  # JWT 검증 필터
│   │   │   └── LoginFilter.java     # 로그인 처리
│   │   ├── handler/
│   │   ├── interceptor/
│   │   │   └── JWTChannelInterceptor.java  # WebSocket JWT 인증 (NEW)
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
- `/api/events/banners`, `/api/events/{id}` - 공개 이벤트 API
- `/uploads/**` - 정적 파일 (프로필 이미지 등)
- `/swagger-ui/**`, `/v3/api-docs/**` - API 문서
