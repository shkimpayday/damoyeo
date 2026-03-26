# 다모여 (DaMoYeo) Frontend 프로젝트 분석

> 오프라인 모임 플랫폼 - React 19 + TypeScript + Vite 7

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [디렉토리 구조](#3-디렉토리-구조)
4. [아키텍처](#4-아키텍처)
5. [라우팅 구조](#5-라우팅-구조)
6. [컴포넌트 구성](#6-컴포넌트-구성)
7. [API 레이어](#7-api-레이어)
8. [상태 관리](#8-상태-관리)
9. [타입 정의](#9-타입-정의)
10. [인증 플로우](#10-인증-플로우)
11. [유틸리티](#11-유틸리티)
12. [개발 가이드](#12-개발-가이드)

---

## 1. 프로젝트 개요

### 소개

**다모여**는 소모임(somoim.co.kr)을 참조한 오프라인 모임 플랫폼입니다.

### 핵심 기능

- 지역/관심사 기반 모임 생성 및 가입
- 정기모임(정모) 일정 관리 및 참석
- 모임 내 채팅 (Phase 2)
- 위치 기반 근처 모임 검색 (Phase 2)

### 프로젝트 구조

```
damoyeo/
├── frontend/          # React 19 + Vite 7 + TypeScript (현재)
├── backend/           # Spring Boot 3.1 + Java 17 (예정)
└── CLAUDE.md          # 통합 문서
```

---

## 2. 기술 스택

### 핵심 의존성

| 기술 | 버전 | 용도 |
|-----|------|------|
| React | 19.2.0 | UI 프레임워크 |
| TypeScript | ~5.9.3 | 타입 안전성 |
| Vite | 7.2.4 | 빌드 도구 |
| React Router | 7.12.0 | 클라이언트 라우팅 |
| TanStack Query | 5.90.17 | 서버 상태 관리 |
| Zustand | 5.0.10 | 전역 상태 관리 |
| Axios | 1.13.2 | HTTP 클라이언트 |
| Tailwind CSS | 4.1.18 | 유틸리티 스타일링 |
| react-cookie | 8.0.1 | 쿠키 관리 |

### 개발 의존성

| 기술 | 버전 | 용도 |
|-----|------|------|
| ESLint | 9.39.1 | 코드 린팅 |
| @tailwindcss/vite | 4.1.18 | Vite 플러그인 |
| TanStack Query DevTools | 5.91.2 | 디버깅 |
| MSW | 2.12.7 | API 모킹 (개발/테스트) |

### 환경 변수 (.env)

```env
VITE_API_HOST=http://localhost:8080    # 백엔드 API URL
VITE_KAKAO_CLIENT_ID=your_client_id    # 카카오 OAuth
VITE_KAKAO_REDIRECT_URI=http://...     # 카카오 리다이렉트
```

---

## 3. 디렉토리 구조

```
frontend/
├── src/
│   ├── app/                          # 앱 설정 & 라우터
│   │   ├── index.tsx                 # 앱 exports
│   │   ├── provider.tsx              # QueryClient + CookiesProvider
│   │   └── routes/
│   │       ├── index.tsx             # 라우트 정의 (lazy loading)
│   │       ├── protected-route.tsx   # 인증 가드
│   │       └── pages/                # 페이지 컴포넌트
│   │           ├── main-page.tsx
│   │           ├── search-page.tsx
│   │           ├── notification-page.tsx
│   │           ├── auth/             # 인증 페이지
│   │           ├── groups/           # 모임 페이지
│   │           ├── meetings/         # 정모 페이지
│   │           └── events/           # 이벤트 페이지 (NEW)
│   │
│   ├── components/                   # 공유 컴포넌트
│   │   ├── layout/                   # 레이아웃
│   │   │   ├── mobile-layout.tsx
│   │   │   ├── header.tsx
│   │   │   ├── bottom-nav.tsx
│   │   │   └── notification-bell.tsx
│   │   └── ui/                       # UI 컴포넌트
│   │       ├── avatar.tsx
│   │       ├── spinner.tsx
│   │       ├── category-chip.tsx
│   │       ├── empty-state.tsx
│   │       └── result-modal.tsx
│   │
│   ├── features/                     # 기능 모듈 (Feature-based)
│   │   ├── auth/                     # 인증
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── stores/
│   │   │   └── types/
│   │   ├── groups/                   # 모임
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   └── types/
│   │   ├── meetings/                 # 정모
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   └── types/
│   │   ├── notifications/            # 알림
│   │   │   ├── api/
│   │   │   ├── stores/
│   │   │   └── types/
│   │   ├── events/                   # 이벤트/배너
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   └── types/
│   │   └── chat/                     # 채팅 (NEW)
│   │       ├── api/
│   │       │   └── chat-api.ts
│   │       ├── components/
│   │       │   ├── chat-room.tsx            # 메인 컨테이너
│   │       │   ├── chat-header.tsx          # 헤더 (연결 상태)
│   │       │   ├── message-list.tsx         # 메시지 목록
│   │       │   ├── message-item.tsx         # 메시지 아이템
│   │       │   ├── message-input.tsx        # 입력창 + 타이핑
│   │       │   └── typing-indicator.tsx     # 타이핑 인디케이터
│   │       ├── hooks/
│   │       │   ├── use-websocket.ts         # WebSocket 연결 관리
│   │       │   ├── use-chat-messages.ts     # TanStack Query
│   │       │   └── use-chat-room.ts         # Combined Hook
│   │       ├── stores/
│   │       │   └── chat-store.ts            # 메시지 + 타이핑 상태
│   │       ├── types/
│   │       │   └── index.ts                 # ChatMessageDTO, TypingEvent
│   │       └── index.ts
│   │
│   ├── mocks/                        # MSW 모킹 (NEW)
│   │   ├── browser.ts                # MSW 브라우저 설정
│   │   ├── handlers.ts               # API 핸들러
│   │   └── data.ts                   # 목 데이터
│   │
│   ├── lib/                          # 라이브러리 설정
│   │   ├── axios.ts                  # Axios 인스턴스 (JWT 인터셉터)
│   │   ├── cookie.ts                 # 쿠키 유틸리티
│   │   └── react-query.ts            # TanStack Query 설정
│   │
│   ├── config/                       # 환경 설정
│   │   └── env.ts
│   │
│   ├── utils/                        # 유틸리티 함수
│   │   └── date.ts
│   │
│   ├── assets/                       # 정적 자산
│   ├── main.tsx                      # 엔트리 포인트
│   └── index.css                     # 글로벌 스타일
│
├── dist/                             # 빌드 출력
├── public/                           # 정적 파일
├── index.html                        # HTML 템플릿
├── package.json
├── vite.config.ts
├── tsconfig.json
└── eslint.config.js
```

---

## 4. 아키텍처

### Feature-Based 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Pages (app/routes/pages/)                 │
│                     ┌───────────┬───────────────┐               │
│                     │           │               │               │
│                     ▼           ▼               ▼               │
│  ┌─────────────────────────────────────────────────────────────┤
│  │                    Features (기능 모듈)                      │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
│  │  │  auth  │ │ groups │ │meetings│ │ notif. │ │ events │   │
│  │  │ ────── │ │ ────── │ │ ────── │ │ ────── │ │ ────── │   │
│  │  │ api/   │ │ api/   │ │ api/   │ │ api/   │ │ api/   │   │
│  │  │ hooks/ │ │ hooks/ │ │ hooks/ │ │ stores/│ │ hooks/ │   │
│  │  │ stores/│ │ comps/ │ │ comps/ │ │ types/ │ │ comps/ │   │
│  │  │ comps/ │ │ types/ │ │ types/ │ │        │ │ types/ │   │
│  │  │ types/ │ │        │ │        │ │        │ │        │   │
│  │  └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘   │
│  └───────┼─────────┼──────────┼──────────┼──────────┼─────────┤
│          │         │          │          │          │          │
│          ▼         ▼          ▼          ▼          ▼          │
│  ┌─────────────────────────────────────────────────────────────┤
│  │                    Shared Libraries (lib/)                   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  │    axios     │  │ react-query  │  │    cookie    │       │
│  │  │ (jwtAxios)   │  │  (config)    │  │   (utils)    │       │
│  │  └──────────────┘  └──────────────┘  └──────────────┘       │
│  └─────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┤
│  │                  Shared Components (components/)             │
│  │        layout/ (MobileLayout, Header)                       │
│  │        ui/ (Avatar, Spinner, CategoryChip, etc.)            │
│  └─────────────────────────────────────────────────────────────┤
└─────────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Component  │────▶│  TanStack    │────▶│   Axios      │
│              │     │  Query Hook  │     │  (jwtAxios)  │
└──────────────┘     └──────────────┘     └──────┬───────┘
       ▲                    │                     │
       │                    │                     ▼
       │             ┌──────▼───────┐      ┌──────────────┐
       │             │    Cache     │      │   Backend    │
       │             │  (5min TTL)  │      │   (8080)     │
       │             └──────────────┘      └──────────────┘
       │
┌──────┴───────┐
│   Zustand    │
│  (Auth/알림) │
└──────────────┘
```

---

## 5. 라우팅 구조

### 라우트 맵

```
/                              → MainPage (홈 피드 + 배너 슬라이더)
├── /search                    → SearchPage (통합 검색)
├── /notifications             → NotificationPage
│
├── /events/:eventId           → EventDetailPage (이벤트 상세)
│
├── /groups
│   ├── /groups/list           → GroupListPage
│   ├── /groups/create         → GroupCreatePage [Protected]
│   ├── /groups/:groupId       → GroupDetailPage
│   └── /groups/:groupId/manage → GroupManagePage [Protected]
│
├── /meetings
│   ├── /meetings/list         → MeetingListPage [Protected]
│   ├── /meetings/:meetingId   → MeetingDetailPage [Protected]
│   └── /meetings/create/:groupId → MeetingCreatePage [Protected]
│
└── /member
    ├── /member/login          → LoginPage
    ├── /member/signup         → SignupPage
    ├── /member/profile        → ProfilePage [Protected]
    ├── /member/my-groups      → MyGroupsPage [Protected]
    └── /member/kakao          → KakaoRedirectPage
```

### 보호된 라우트

```tsx
// src/app/routes/protected-route.tsx
<ProtectedRoute>
  <Outlet />  // 인증된 사용자만 접근 가능
</ProtectedRoute>
```

### Lazy Loading

모든 페이지는 `React.lazy()`로 코드 스플리팅됨:

```tsx
const MainPage = lazy(() => import("./pages/main-page"));
const LoginPage = lazy(() => import("./pages/auth/login-page"));
// ...
```

---

## 6. 컴포넌트 구성

### 레이아웃 컴포넌트

#### MobileLayout

```tsx
// 구조
<div className="min-h-screen bg-gray-50">
  <Header />           // 상단 헤더 (고정)
  <main className="pt-14 pb-16">
    <Outlet />         // 페이지 콘텐츠
  </main>
  <BottomNav />        // 하단 네비게이션 (고정)
</div>
```

#### Header

- 로고 (홈 링크)
- 중앙 네비게이션: 홈, 모임, 정모 탭
- 검색 버튼
- 알림 벨 (로그인 시)
- 프로필 드롭다운 메뉴 (로그인 시)
  - 사용자 정보 (닉네임, 이메일)
  - 마이페이지
  - 내 모임
  - 모임 만들기
  - 로그아웃
- 로그인 버튼 (비로그인 시)

### UI 컴포넌트

#### Avatar

```tsx
<Avatar
  src={profileImage}
  name="닉네임"
  size="md"  // sm | md | lg | xl
/>
```

#### Spinner

```tsx
<Spinner size="md" />  // sm | md | lg
```

#### CategoryChip

```tsx
<CategoryChip category={category} />
// 예: 🏃 운동/스포츠
```

#### EmptyState

```tsx
<EmptyState message="모임이 없습니다" />
```

#### ResultModal

```tsx
<ResultModal
  title="로그인 성공"
  content="환영합니다!"
  callback={() => navigate("/")}
/>
```

### Feature 컴포넌트

#### GroupCard

```tsx
<GroupCard group={groupDTO} />
// 썸네일, 카테고리 뱃지, 이름, 위치, 멤버 수
```

#### MeetingCard

```tsx
<MeetingCard meeting={meetingDTO} />
// 모임명, 제목, 날짜, 장소, 참석자 수, 상태 뱃지
```

#### BannerSlider (NEW)

```tsx
<BannerSlider banners={eventBanners} />
// 메인 페이지 상단 자동 회전 배너 슬라이더
// 수동 네비게이션, 페이지네이션 인디케이터 포함
```

#### TopPromoBanner (NEW)

```tsx
<TopPromoBanner />
// 닫기 가능한 프로모션 배너
// localStorage로 24시간 숨김 유지
```

---

## 7. API 레이어

### Axios 인스턴스

```tsx
// src/lib/axios.ts

// 공개 API (인증 불필요)
export const publicAxios = axios.create({
  baseURL: ENV.API_URL,
});

// 인증 API (JWT 토큰 필요)
export const jwtAxios = axios.create({
  baseURL: ENV.API_URL,
});
```

### JWT 인터셉터

```tsx
// 요청 인터셉터
jwtAxios.interceptors.request.use((config) => {
  const memberInfo = getCookie("member");
  if (memberInfo?.accessToken) {
    config.headers.Authorization = `Bearer ${memberInfo.accessToken}`;
  }
  return config;
});

// 응답 인터셉터 (토큰 갱신)
jwtAxios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.data?.error === "ERROR_ACCESS_TOKEN") {
      // 토큰 갱신 후 재요청
      const newTokens = await refreshToken();
      return jwtAxios(originalRequest);
    }
  }
);
```

### API 엔드포인트

#### 인증 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `loginPost` | POST | `/api/member/login` | 로그인 |
| `signupPost` | POST | `/api/member/signup` | 회원가입 |
| `getProfile` | GET | `/api/member/profile` | 프로필 조회 |
| `updateProfile` | PUT | `/api/member/modify` | 프로필 수정 |
| `uploadProfileImage` | POST | `/api/member/profile/image` | 프로필 이미지 업로드 |
| `kakaoLogin` | GET | `/api/member/kakao` | 카카오 로그인 |

#### 이메일 인증 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `sendVerificationCode` | POST | `/api/email/send` | 인증 코드 발송 |
| `verifyEmailCode` | POST | `/api/email/verify` | 인증 코드 검증 |
| `checkEmailVerified` | GET | `/api/email/status` | 인증 상태 확인 |

#### 모임 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `getGroups` | GET | `/api/groups` | 모임 목록 |
| `getGroup` | GET | `/api/groups/{id}` | 모임 상세 |
| `createGroup` | POST | `/api/groups` | 모임 생성 |
| `updateGroup` | PUT | `/api/groups/{id}` | 모임 수정 |
| `deleteGroup` | DELETE | `/api/groups/{id}` | 모임 삭제 |
| `joinGroup` | POST | `/api/groups/{id}/join` | 가입 신청 |
| `leaveGroup` | POST | `/api/groups/{id}/leave` | 탈퇴 |
| `getMyGroups` | GET | `/api/groups/my` | 내 모임 |
| `getNearbyGroups` | GET | `/api/groups/nearby` | 근처 모임 |

#### 정모 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `getMeetingsByGroup` | GET | `/api/groups/{gid}/meetings` | 모임의 정모 목록 (전체) |
| `getUpcomingMeetingsByGroup` | GET | `/api/meetings/group/{gid}/upcoming` | 모임의 예정된 정모 (날짜 기반) |
| `getPastMeetingsByGroup` | GET | `/api/meetings/group/{gid}/past` | 모임의 지난 정모 (날짜 기반) |
| `getMeeting` | GET | `/api/meetings/{id}` | 정모 상세 |
| `createMeeting` | POST | `/api/groups/{gid}/meetings` | 정모 생성 |
| `attendMeeting` | POST | `/api/meetings/{id}/attend` | 참석 등록 |
| `cancelAttend` | DELETE | `/api/meetings/{id}/attend` | 참석 취소 |
| `getUpcomingMeetings` | GET | `/api/meetings/upcoming` | 다가오는 정모 |

> **정모 상태 관리**: 스케줄러 대신 날짜 기반 필터링 사용. 프론트엔드에서도 `meetingDate`와 현재 시간을 비교하여 상태 표시.

#### 카테고리 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `getCategories` | GET | `/api/categories` | 카테고리 목록 |

#### 알림 API

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `getNotifications` | GET | `/api/notifications` | 알림 목록 |
| `getUnreadCount` | GET | `/api/notifications/unread-count` | 안 읽은 수 |
| `markAsRead` | PUT | `/api/notifications/{id}/read` | 읽음 처리 |
| `markAllAsRead` | PUT | `/api/notifications/read-all` | 전체 읽음 |

#### 이벤트 API (NEW)

| 함수 | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| `getBanners` | GET | `/api/events/banners` | 활성 배너 목록 |
| `getDetail` | GET | `/api/events/{id}` | 이벤트 상세 |

---

## 8. 상태 관리

### 상태 관리 전략

| 상태 유형 | 도구 | 용도 |
|----------|------|------|
| 사용자 인증 | Zustand + Cookie | 로그인 정보, 토큰 |
| 알림 상태 | Zustand | 실시간 알림 |
| 서버 데이터 | TanStack Query | 모임/정모 목록, 상세 |
| URL 상태 | useSearchParams | 검색 필터, 페이지 번호 |

### Zustand Stores

#### Auth Store

```tsx
// features/auth/stores/auth-store.ts
interface AuthState {
  member: MemberInfo | null;
  status: "" | "pending" | "fulfilled" | "error";

  // Actions
  login: (email: string, pw: string) => Promise<void>;
  logout: () => void;
  save: (memberInfo: MemberInfo) => void;
  updateProfile: (nickname: string, profileImage: string) => void;
}

// 사용 예시
const { member, login, logout } = useAuthStore();
```

#### Notification Store

```tsx
// features/notifications/stores/notification-store.ts
interface NotificationState {
  notifications: NotificationDTO[];
  unreadCount: number;

  // Actions
  setNotifications: (list: NotificationDTO[]) => void;
  setUnreadCount: (count: number) => void;
  addNotification: (notification: NotificationDTO) => void;
  markAsRead: (id: number) => void;
  markAllAsRead: () => void;
}
```

### TanStack Query 설정

```tsx
// src/lib/react-query.ts
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,     // 5분
      gcTime: 30 * 60 * 1000,       // 30분
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});
```

### Query Hooks

#### Groups Hooks

```tsx
// 목록 조회 (무한 스크롤)
const { data, fetchNextPage, hasNextPage } = useGroupsInfinite(params);

// 상세 조회
const { data: group } = useGroupDetail(groupId);

// 내 모임
const { data: myGroups } = useMyGroups();

// Mutations
const createGroup = useCreateGroup();
const joinGroup = useJoinGroup();
```

#### Meetings Hooks

```tsx
// 모임의 예정된 정모 목록 (날짜 기반)
const { data: upcomingMeetings } = useUpcomingMeetingsByGroup(groupId);

// 모임의 지난 정모 목록 (날짜 기반)
const { data: pastMeetings } = usePastMeetingsByGroup(groupId);

// 정모 상세
const { data: meeting } = useMeetingDetail(meetingId);

// 참석 Mutation
const attendMeeting = useAttendMeeting();
```

#### Events Hooks (NEW)

```tsx
// 활성 배너 목록
const { data: banners } = useEventBanners();

// 이벤트 상세
const { data: event } = useEventDetail(eventId);
```

---

## 9. 타입 정의

> **중첩 객체 패턴**: API 응답에서 관련 엔티티 정보는 ID만 반환하지 않고 중첩 객체로 반환합니다.

### 공통 타입

```tsx
// features/auth/types/index.ts

// 위치 정보 (중첩 객체용)
interface LocationInfo {
  lat: number;
  lng: number;
}

// 회원 요약 정보 (중첩 객체용)
interface MemberSummary {
  id: number;
  nickname: string;
  profileImage?: string;
}
```

### 인증 타입

```tsx
// features/auth/types/index.ts

interface MemberInfo {
  email: string;
  nickname: string;
  profileImage: string;
  accessToken: string;
  refreshToken: string;
  roleNames: string[];  // USER, ADMIN, PREMIUM
  social?: boolean;
}

interface ProfileInfo {
  email: string;
  nickname: string;
  profileImage: string;
  introduction: string;
  location: LocationInfo | null;
  address: string;
  interests: Category[];
  createdAt: string;
}

interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}
```

### 카테고리 타입

```tsx
// features/groups/types/index.ts

type CategoryName =
  | "운동/스포츠" | "사교/인맥" | "아웃도어/여행"
  | "문화/공연" | "음악/악기" | "외국어"
  | "독서" | "스터디" | "게임/오락"
  | "사진/영상" | "요리" | "공예"
  | "자기계발" | "봉사활동" | "반려동물"
  | "IT/개발" | "금융/재테크" | "기타";

interface Category {
  id: number;
  name: string;
  icon: string;
  displayOrder: number;
  image?: string;
  color?: string;
}
```

### 모임 타입

```tsx
// features/groups/types/index.ts

type GroupStatus = "ACTIVE" | "INACTIVE" | "DELETED";
type GroupRole = "OWNER" | "MANAGER" | "MEMBER";
type GroupMemberStatus = "APPROVED" | "BANNED";

interface GroupDTO {
  id: number;
  name: string;
  description: string;
  category: Category;              // 중첩 객체
  coverImage: string;
  thumbnailImage: string;
  location: LocationInfo | null;   // 중첩 객체
  address: string;
  maxMembers: number;
  memberCount: number;
  isPublic: boolean;
  status: GroupStatus;
  owner: MemberSummary;            // 중첩 객체
  myRole?: GroupRole | null;
  createdAt: string;
  updatedAt: string;
}

interface GroupListDTO {
  id: number;
  name: string;
  description?: string;
  category: Category;              // 중첩 객체
  coverImage?: string;
  thumbnailImage?: string;
  location?: LocationInfo | null;  // 중첩 객체
  address?: string;
  maxMembers: number;
  memberCount: number;
  isPublic?: boolean;
  status?: GroupStatus;
  owner?: MemberSummary;           // 중첩 객체
  myRole?: GroupRole | null;
  createdAt?: string;
  updatedAt?: string;
  distance?: number;               // 거리 기반 검색 시
}

interface GroupMemberDTO {
  id: number;
  member: MemberSummary;           // 중첩 객체
  role: GroupRole;
  status: GroupMemberStatus;
  joinedAt: string;
}

interface GroupSearchParams {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number;
  lat?: number;
  lng?: number;
  radius?: number;
  sort?: "latest" | "popular" | "distance";
}

interface PageResponseDTO<T> {
  dtoList: T[];
  pageNumList: number[];
  prev: boolean;
  next: boolean;
  totalCount: number;
  prevPage: number;
  nextPage: number;
  totalPage: number;
  current: number;
}
```

### 정모 타입

```tsx
// features/meetings/types/index.ts

type MeetingStatus = "SCHEDULED" | "ONGOING" | "COMPLETED" | "CANCELLED";
type AttendStatus = "ATTENDING" | "MAYBE" | "NOT_ATTENDING";

interface MeetingDTO {
  id: number;
  groupId: number;
  groupName: string;
  title: string;
  description?: string;
  location?: LocationInfo | null;  // 중첩 객체
  address?: string;
  meetingDate: string;             // ISO 8601
  endDate?: string;
  maxAttendees: number;
  currentAttendees: number;
  fee: number;
  status: MeetingStatus;
  myStatus?: AttendStatus | null;
  createdBy: MemberSummary;        // 중첩 객체
  createdAt: string;
}

interface MeetingListDTO {
  id: number;
  groupId: number;
  groupName: string;
  title: string;
  address: string;
  meetingDate: string;
  maxAttendees: number;
  currentAttendees: number;
  status: MeetingStatus;
}

interface MeetingAttendeeDTO {
  id: number;
  member: MemberSummary;           // 중첩 객체
  status: AttendStatus;
  checkedIn?: boolean;
  registeredAt: string;
}

interface MeetingCreateRequest {
  title: string;
  description: string;
  address: string;
  lat?: number;
  lng?: number;
  meetingDate: string;
  endDate?: string;
  maxAttendees: number;
  fee?: number;
}
```

### 알림 타입

```tsx
// features/notifications/types/index.ts

type NotificationType =
  | "WELCOME"            // 회원가입 환영
  | "NEW_MEMBER"         // 새 멤버 가입
  | "MEMBER_LEFT"        // 멤버 탈퇴
  | "ROLE_CHANGED"       // 역할 변경
  | "MEMBER_KICKED"      // 강퇴됨
  | "GROUP_DISBANDED"    // 모임 해체됨
  | "GROUP_UPDATE"       // 모임 정보 변경
  | "NEW_MEETING"        // 새 정모 생성
  | "MEETING_REMINDER"   // 정모 리마인더
  | "MEETING_CANCELLED"; // 정모 취소

type NotificationRefType = "GROUP" | "MEETING" | "SYSTEM";

interface NotificationDTO {
  id: number;
  type: NotificationType;
  title: string;
  content: string;                 // 알림 메시지
  referenceId: number;             // 관련 엔티티 ID
  referenceType: NotificationRefType;  // GROUP, MEETING, CHAT
  read: boolean;
  createdAt: string;
}
```

### 이벤트 타입 (NEW)

```tsx
// features/events/types/index.ts

type EventType = "PROMOTION" | "NOTICE" | "SPECIAL" | "FEATURE";

interface EventBanner {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  linkUrl?: string;
  type: EventType;
  startDate: string;
  endDate: string;
}

interface EventDetail extends EventBanner {
  content: string;        // 마크다운 지원
  tags: string[];
  isActive: boolean;
  displayOrder: number;
  createdAt: string;
}
```

---

## 10. 인증 플로우

### 토큰 설정

- **Access Token**: 10분
- **Refresh Token**: 24시간

### 로그인 흐름

```
1. 사용자 로그인
   ┌─────────────────┐
   │  LoginForm      │
   │  email + pw     │
   └────────┬────────┘
            │
            ▼
   ┌─────────────────┐
   │  authApi.       │
   │  loginPost()    │
   └────────┬────────┘
            │ POST /api/member/login
            ▼
   ┌─────────────────┐
   │  Backend        │
   │  Returns tokens │
   └────────┬────────┘
            │
            ▼
   ┌─────────────────┐
   │  authStore.     │
   │  login()        │
   │  + setCookie()  │
   └─────────────────┘
```

### 토큰 갱신 흐름

```
1. API 요청 실패 (401)
   ┌─────────────────┐
   │  jwtAxios       │
   │  401 Error      │
   │  ERROR_ACCESS   │
   └────────┬────────┘
            │
            ▼
2. 토큰 갱신 요청
   ┌─────────────────┐
   │  GET /api/      │
   │  member/refresh │
   └────────┬────────┘
            │
            ▼
3. 새 토큰으로 재요청
   ┌─────────────────┐
   │  Update Cookie  │
   │  Retry Request  │
   └─────────────────┘
```

### Protected Route

```tsx
// src/app/routes/protected-route.tsx
export function ProtectedRoute({
  children,
  requiredRoles,
}: ProtectedRouteProps) {
  const { loginState, isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    return <Navigate to="/member/login" />;
  }

  if (requiredRoles && !hasRequiredRole(loginState, requiredRoles)) {
    return <Navigate to="/" />;
  }

  return children;
}
```

---

## 11. 유틸리티

### 날짜 유틸리티

```tsx
// src/utils/date.ts

// 날짜 포맷팅
formatDate("2024-01-15T10:30:00")      // "2024.01.15"
formatDateTime("2024-01-15T10:30:00")  // "2024.01.15 10:30"

// 상대 시간
getRelativeTime("2024-01-15T10:30:00") // "2시간 전", "방금 전"

// 요일
getDayOfWeek("2024-01-15")             // "월"

// Input 포맷
toInputDateFormat(new Date())          // "2024-01-15"
toInputDateTimeFormat(new Date())      // "2024-01-15T10:30"
```

### 쿠키 유틸리티

```tsx
// src/lib/cookie.ts

setCookie("member", memberInfo, 1);  // 1일 유효
getCookie("member");                 // MemberInfo | null
removeCookie("member");
```

---

## 12. 개발 가이드

### 빠른 시작

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev          # http://localhost:5173

# 빌드
npm run build

# 린트 검사
npm run lint
```

### 환경 설정

```bash
# .env 파일 생성
cp .env.example .env

# 환경 변수 수정
VITE_API_HOST=http://localhost:8080
VITE_KAKAO_CLIENT_ID=your_client_id
```

### 디렉토리 규칙

```
새 기능 추가 시:
1. features/{feature-name}/ 디렉토리 생성
2. api/, components/, hooks/, types/ 서브 디렉토리 구성
3. index.ts에서 public API export
```

### 코드 스타일

- TypeScript strict 모드 사용
- Path alias: `@/*` → `src/*`
- Tailwind CSS 유틸리티 클래스 사용
- TanStack Query로 서버 상태 관리
- Zustand로 클라이언트 상태 관리

### 프로젝트 빌드

```bash
# TypeScript 타입 체크 + Vite 빌드
npm run build

# 빌드 결과 미리보기
npm run preview
```

---

## 개발 단계

### Phase 1 - MVP (완료)

- [x] 회원 시스템 (로그인/회원가입/프로필)
- [x] 모임 CRUD + 가입/탈퇴
- [x] 정모 CRUD + 참석
- [x] 검색/필터
- [x] 카테고리 시스템

### Phase 2 - 핵심 기능 (진행 중)

- [x] 프로필 수정 기능 (이미지 업로드, 닉네임/소개 변경)
- [x] 위치 기반 근처 모임 검색 (Geolocation API)
- [x] 멤버 관리 (역할 변경, 강퇴)
- [x] 모임/정모 수정 기능
- [x] 무한 스크롤 (Intersection Observer)
- [x] 이벤트/배너 시스템 (메인 배너 슬라이더, 이벤트 상세 페이지)
- [x] MSW API 모킹 설정 (개발/테스트용)
- [x] 정모 상태 관리 개선 (날짜 기반 예정/지난 정모 분리)
- [x] 알림 시스템 (강퇴/모임해체 알림 추가)
- [x] **실시간 채팅 (WebSocket/STOMP)**
  - [x] @stomp/stompjs, sockjs-client 의존성 추가
  - [x] WebSocket 연결 관리 (use-websocket.ts)
  - [x] Zustand 채팅 스토어 (메시지 + 타이핑 상태)
  - [x] TanStack Query 메시지 히스토리 로드
  - [x] ChatRoom 컴포넌트 (헤더, 메시지 목록, 입력창)
  - [x] 타이핑 인디케이터 ("○○○님이 입력 중...")
  - [x] 스마트 자동 스크롤 (내 메시지 무조건 스크롤)
  - [x] 채팅 별도 페이지 (`/groups/:groupId/chat`)
- [ ] 실시간 알림 (SSE/WebSocket)

### Phase 3 - 고도화

- [ ] PWA + 푸시 알림
- [ ] 프리미엄 기능
- [ ] 관리자 대시보드

---

## 최근 추가된 파일

### 인증 기능
- `src/features/auth/hooks/use-profile.ts` - 프로필 조회/수정/이미지 업로드 hooks
- `src/features/auth/components/profile-edit-modal.tsx` - 프로필 수정 모달

### 모임 기능
- `src/features/groups/hooks/use-group-members.ts` - 멤버 관리 hooks (역할 변경, 강퇴)
- `src/features/groups/hooks/use-location.ts` - Geolocation 위치 hook (세션 저장)
- `src/features/groups/components/nearby-groups-section.tsx` - 내 근처 모임 섹션
- `src/app/routes/pages/groups/edit-page.tsx` - 모임 수정 페이지

### 정모 기능
- `src/app/routes/pages/meetings/edit-page.tsx` - 정모 수정 페이지
- `src/features/meetings/hooks/use-meetings.ts` - 예정/지난 정모 조회 hooks 추가
  - `useUpcomingMeetingsByGroup(groupId)` - 예정된 정모 (날짜 기반)
  - `usePastMeetingsByGroup(groupId)` - 지난 정모 (날짜 기반)
- `src/app/routes/pages/meetings/detail-page.tsx` - UI 개선 (카드 기반 레이아웃)

### 이벤트/배너 기능
- `src/features/events/api/events-api.ts` - 이벤트 API 클라이언트
- `src/features/events/hooks/use-events.ts` - 이벤트 Query hooks
- `src/features/events/components/banner-slider.tsx` - 메인 배너 슬라이더
- `src/features/events/components/top-promo-banner.tsx` - 프로모션 배너
- `src/features/events/types/index.ts` - 이벤트 타입 정의
- `src/app/routes/pages/events/detail-page.tsx` - 이벤트 상세 페이지

### MSW 모킹
- `src/mocks/browser.ts` - MSW 브라우저 설정
- `src/mocks/handlers.ts` - API 요청 핸들러
- `src/mocks/data.ts` - 목 데이터 (DEMO_EVENT_BANNERS 등)

### 채팅 기능 (NEW)
- `src/features/chat/api/chat-api.ts` - 채팅 REST API 클라이언트
- `src/features/chat/hooks/use-websocket.ts` - WebSocket 연결 및 STOMP 관리
- `src/features/chat/hooks/use-chat-messages.ts` - TanStack Query 메시지 히스토리
- `src/features/chat/hooks/use-chat-room.ts` - WebSocket + Query 통합 훅
- `src/features/chat/stores/chat-store.ts` - 메시지 + 타이핑 상태 (Zustand)
- `src/features/chat/components/chat-room.tsx` - 채팅방 메인 컨테이너
- `src/features/chat/components/chat-header.tsx` - 헤더 (연결 상태)
- `src/features/chat/components/message-list.tsx` - 메시지 목록 (스마트 스크롤)
- `src/features/chat/components/message-item.tsx` - 메시지 아이템 (좌/우 정렬)
- `src/features/chat/components/message-input.tsx` - 입력창 + 타이핑 이벤트
- `src/features/chat/components/typing-indicator.tsx` - 타이핑 인디케이터
- `src/features/chat/types/index.ts` - 채팅 타입 정의
- `src/app/routes/pages/groups/chat-page.tsx` - 채팅 전용 페이지

**주요 기능:**
- WebSocket/STOMP 실시간 메시지 송수신
- JWT 인증 통합 (CONNECT 프레임)
- 메시지 히스토리 로드 (TanStack Query)
- 읽음 상태 추적 (unread count)
- 타이핑 인디케이터 (3초 디바운싱)
- 스마트 자동 스크롤 (내 메시지 무조건 스크롤, 다른 사람 메시지 조건부)
- 채팅 별도 페이지 (`/groups/:groupId/chat`)

---

> 마지막 업데이트: 2025-02-25
