# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Development
./gradlew bootRun              # Start server (http://localhost:8080)
./gradlew build                # Full build (compile, test, package)
./gradlew test                 # Run all tests
./gradlew test --tests "ClassName"           # Run single test class
./gradlew test --tests "ClassName.methodName" # Run single test method
./gradlew clean build          # Clean rebuild

# Dependencies
./gradlew dependencies         # Show dependency tree
```

## Tech Stack

- **Spring Boot 3.1** + **Java 17**
- **MariaDB** with **JPA/Hibernate** + **QueryDSL 5.0**
- **Spring Security** with **JWT (JJWT 0.11.5)**
- **Lombok**, **ModelMapper 3.1**, **SpringDoc OpenAPI 2.1**

## Project Architecture

```
src/main/java/com/damoyeo/api/
├── DamoyeoApplication.java     # Entry point (@EnableJpaAuditing)
├── domain/                     # Business domains
│   ├── member/                 # 회원 (로그인, 프로필)
│   ├── group/                  # 모임 CRUD & 멤버 관리
│   ├── meeting/                # 정모 (정기모임)
│   ├── category/               # 18개 카테고리
│   ├── notification/           # 알림
│   └── event/                  # 이벤트/배너 (NEW)
└── global/                     # Cross-cutting concerns
    ├── common/                 # BaseEntity, PageDTO
    ├── config/                 # SecurityConfig, WebConfig
    ├── exception/              # GlobalExceptionHandler, CustomException
    ├── security/               # JWT filters, handlers
    └── util/                   # JWTUtil
```

### Domain Layer Pattern

Each domain follows this structure:
```
domain/{name}/
├── entity/        # JPA entities
├── dto/           # Request/Response DTOs
├── repository/    # Spring Data JPA + custom queries
├── service/       # Interface + Impl pattern
└── controller/    # REST endpoints with @RestController
```

## Key Architecture Decisions

1. **Table naming**: `Group` entity maps to `club` table (SQL reserved word)
2. **Soft delete**: Groups use `status` enum (ACTIVE, INACTIVE, DELETED)
3. **N+1 prevention**: Use `@Query` with `left join fetch` or `@EntityGraph`
4. **BaseEntity**: All entities extend this for automatic `createdAt`/`modifiedAt`
5. **Constructor injection**: Via `@RequiredArgsConstructor` (no field injection)

## Security Architecture

### JWT Configuration (application.properties)
- Access Token: 10 minutes
- Refresh Token: 24 hours
- Algorithm: HS256

### Authentication Flow
```
Request → JWTCheckFilter → Controller → Service → Repository
                ↓
         Validates Bearer token
         Sets SecurityContext with MemberDTO
```

### Public Endpoints (no auth required)
- `POST /api/member/login`
- `POST /api/member/signup`
- `GET /api/categories`
- `GET /api/member/kakao`
- `GET /api/events/banners` - 활성 배너 목록
- `GET /api/events/{id}` - 이벤트 상세
- Swagger UI (`/swagger-ui.html`)

## Database

- **Host**: localhost:3306
- **Database**: damoyeo
- **DDL Strategy**: `update` (auto schema generation)

## API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html

## Important Files

| File | Purpose |
|------|---------|
| `SecurityConfig.java` | JWT filter chain, CORS, public paths |
| `JWTUtil.java` | Token generation/validation |
| `JWTCheckFilter.java` | Per-request token validation |
| `GlobalExceptionHandler.java` | Maps exceptions to HTTP responses |
| `DataInitializer.java` | Seeds 18 categories on startup |
| `BaseEntity.java` | Auditing fields (createdAt, modifiedAt) |
| `EventController.java` | 이벤트/배너 REST API (NEW) |
| `EventServiceImpl.java` | 이벤트 비즈니스 로직 (NEW) |

## Code Conventions

- Service layer uses Interface + Impl pattern
- DTOs: `{Name}CreateRequest`, `{Name}ModifyRequest`, `{Name}DTO`
- All controllers annotated with `@Tag` and `@Operation` for Swagger
- Use `@Transactional(readOnly = true)` for query-only methods

## Event API Endpoints (NEW)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/events/banners` | X | 활성 배너 목록 (시간 기반 필터링) |
| GET | `/api/events/{id}` | X | 이벤트 상세 조회 |
| GET | `/api/events` | O (ADMIN) | 전체 이벤트 목록 |
| POST | `/api/events` | O (ADMIN) | 이벤트 생성 |
| DELETE | `/api/events/{id}` | O (ADMIN) | 이벤트 삭제 |
| PATCH | `/api/events/{id}/toggle` | O (ADMIN) | 이벤트 활성화 토글 |

### Event Entity 특징
- **시간 기반 표시**: `startDate` ~ `endDate` 범위 내에만 배너 표시
- **활성화 플래그**: `isActive`로 관리자가 수동 비활성화 가능
- **정렬 순서**: `displayOrder`로 배너 순서 관리
- **마크다운 지원**: `content` 필드에 마크다운 콘텐츠 저장
- **태그 시스템**: 쉼표 구분 태그로 분류

## DTO 구조 (중첩 객체 패턴)

API 응답에서 관련 엔티티 정보는 ID만 반환하지 않고 **중첩 객체**로 반환합니다.

### MemberSummaryDTO (공통)
```java
// 회원 요약 정보 - 목록/참조용
public class MemberSummaryDTO {
    private Long id;
    private String nickname;
    private String profileImage;
}
```

### GroupDTO
```java
public class GroupDTO {
    private Long id;
    private String name;
    private String description;
    private CategoryDTO category;        // 중첩 객체
    private String coverImage;
    private String thumbnailImage;
    private LocationDTO location;        // 중첩 객체 {lat, lng}
    private String address;
    private Integer maxMembers;
    private Integer memberCount;
    private Boolean isPublic;
    private String status;               // ACTIVE, INACTIVE, DELETED
    private MemberSummaryDTO owner;      // 중첩 객체
    private String myRole;               // OWNER, MANAGER, MEMBER, null
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### MeetingDTO
```java
public class MeetingDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private String title;
    private String description;
    private LocationDTO location;        // 중첩 객체 {lat, lng}
    private String address;
    private LocalDateTime meetingDate;
    private LocalDateTime endDate;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private Integer fee;
    private String status;               // SCHEDULED, ONGOING, COMPLETED, CANCELLED
    private String myStatus;             // ATTENDING, MAYBE, NOT_ATTENDING, null
    private MemberSummaryDTO createdBy;  // 중첩 객체
    private LocalDateTime createdAt;
}
```

### NotificationDTO
```java
public class NotificationDTO {
    private Long id;
    private String type;                 // NEW_MEMBER, NEW_MEETING, etc.
    private String content;              // 알림 메시지
    private Long referenceId;            // 관련 엔티티 ID
    private String referenceType;        // GROUP, MEETING, CHAT
    private Boolean isRead;
    private LocalDateTime createdAt;
}
```

### GroupMemberDTO / MeetingAttendeeDTO
```java
// 모임 멤버
public class GroupMemberDTO {
    private Long id;
    private MemberSummaryDTO member;     // 중첩 객체
    private String role;                 // OWNER, MANAGER, MEMBER
    private LocalDateTime joinedAt;
}

// 정모 참석자
public class MeetingAttendeeDTO {
    private Long id;
    private MemberSummaryDTO member;     // 중첩 객체
    private String status;               // ATTENDING, MAYBE, NOT_ATTENDING
    private Boolean checkedIn;
    private LocalDateTime registeredAt;
}
```

### EventDTO (NEW)
```java
// 이벤트 타입 enum
public enum EventType {
    PROMOTION, NOTICE, SPECIAL, FEATURE
}

// 배너 목록용 (가벼운 DTO)
public class EventBannerDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private EventType type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

// 이벤트 상세용 (전체 DTO)
public class EventDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String content;              // 마크다운 지원
    private String imageUrl;
    private String linkUrl;
    private EventType type;
    private List<String> tags;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
