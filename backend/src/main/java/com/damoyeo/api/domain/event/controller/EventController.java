package com.damoyeo.api.domain.event.controller;

import com.damoyeo.api.domain.event.dto.EventBannerDTO;
import com.damoyeo.api.domain.event.dto.EventCreateRequest;
import com.damoyeo.api.domain.event.dto.EventDetailDTO;
import com.damoyeo.api.domain.event.dto.EventUpdateRequest;
import com.damoyeo.api.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 이벤트(Event) REST API 컨트롤러
 * ============================================================================
 *
 * [역할]
 * 이벤트 관련 HTTP 요청을 처리하는 REST API 엔드포인트를 제공합니다.
 *
 * [기본 URL]
 * /api/events
 *
 * [API 목록]
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │ Method │ URL                          │ 설명                │ 인증 │
 * ├────────────────────────────────────────────────────────────────────────┤
 * │ GET    │ /api/events/banners          │ 활성 배너 목록 조회  │ X   │
 * │ GET    │ /api/events/{id}             │ 이벤트 상세 조회    │ X   │
 * │ GET    │ /api/events                  │ 전체 이벤트 목록    │ X   │
 * │ POST   │ /api/events                  │ 이벤트 생성 (관리자) │ O   │
 * │ PUT    │ /api/events/{id}             │ 이벤트 수정 (관리자) │ O   │
 * │ DELETE │ /api/events/{id}             │ 이벤트 삭제 (관리자) │ O   │
 * │ PATCH  │ /api/events/{id}/toggle      │ 활성화 토글 (관리자) │ O   │
 * └────────────────────────────────────────────────────────────────────────┘
 *
 * [Swagger 문서]
 * @Tag로 API 그룹화, @Operation으로 각 API 설명
 * http://localhost:8080/swagger-ui.html 에서 확인 가능
 *
 * [프론트엔드 연동]
 * - 메인 페이지 BannerSlider: GET /api/events/banners
 * - 이벤트 상세 페이지: GET /api/events/{id}
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "이벤트/배너 API")
public class EventController {

    /** 이벤트 서비스 (비즈니스 로직 처리) */
    private final EventService eventService;

    // ========================================================================
    // 공개 API (인증 불필요)
    // ========================================================================

    /**
     * 활성 배너 목록 조회
     *
     * 메인 페이지 배너 슬라이더에 표시할 이벤트 목록을 조회합니다.
     * 현재 시점에 노출 가능한(isActive=true, 기간 내) 이벤트만 반환합니다.
     *
     * [프론트엔드 요청]
     * GET /api/events/banners
     *
     * [응답 예시]
     * [
     *   {
     *     "id": 1,
     *     "title": "신규 가입 이벤트",
     *     "description": "프리미엄 30일 무료!",
     *     "imageUrl": "https://...",
     *     "linkUrl": "/events/1",
     *     "startDate": "2025-01-01T00:00:00",
     *     "endDate": "2025-01-31T23:59:59",
     *     "isActive": true
     *   },
     *   ...
     * ]
     *
     * @return 활성 배너 목록
     */
    @GetMapping("/banners")
    @Operation(summary = "활성 배너 목록 조회", description = "메인 페이지 배너 슬라이더용")
    public ResponseEntity<List<EventBannerDTO>> getBanners() {
        return ResponseEntity.ok(eventService.getActiveBanners());
    }

    /**
     * 이벤트 상세 조회
     *
     * 특정 이벤트의 상세 정보를 조회합니다.
     * 이벤트 상세 페이지에서 사용합니다.
     *
     * [프론트엔드 요청]
     * GET /api/events/1
     *
     * [응답 예시]
     * {
     *   "id": 1,
     *   "title": "신규 가입 이벤트",
     *   "description": "프리미엄 30일 무료!",
     *   "content": "## 신규 가입 이벤트\n\n다모여에 처음 가입하시는 분들께...",
     *   "imageUrl": "https://...",
     *   "linkUrl": "/events/1",
     *   "type": "PROMOTION",
     *   "startDate": "2025-01-01T00:00:00",
     *   "endDate": "2025-01-31T23:59:59",
     *   "isActive": true,
     *   "tags": ["신규가입", "프리미엄", "무료체험"]
     * }
     *
     * @param eventId 이벤트 ID
     * @return 이벤트 상세 정보
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "이벤트 상세 조회", description = "이벤트 상세 페이지용")
    public ResponseEntity<EventDetailDTO> getEventDetail(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventDetail(eventId));
    }

    /**
     * 전체 이벤트 목록 조회
     *
     * 모든 이벤트를 조회합니다. (관리자 페이지용)
     * 활성화 여부, 기간과 관계없이 모든 이벤트를 반환합니다.
     *
     * @return 전체 이벤트 목록
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 이벤트 목록 조회", description = "관리자 페이지용")
    public ResponseEntity<List<EventDetailDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // ========================================================================
    // 관리자 API
    // ========================================================================

    /**
     * 이벤트 생성 (관리자용)
     *
     * 새로운 이벤트를 생성합니다.
     *
     * [프론트엔드 요청]
     * POST /api/events
     * Authorization: Bearer {accessToken}
     * {
     *   "title": "신규 가입 이벤트",
     *   "description": "프리미엄 30일 무료!",
     *   ...
     * }
     *
     * @param request 이벤트 생성 요청 DTO
     * @return 생성된 이벤트 ID { "id": 1 }
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 생성", description = "관리자 전용")
    public ResponseEntity<Map<String, Long>> createEvent(@Valid @RequestBody EventCreateRequest request) {
        Long eventId = eventService.createEvent(request);
        return ResponseEntity.ok(Map.of("id", eventId));
    }

    /**
     * 이벤트 수정 (관리자용)
     *
     * 제목, 설명, 이미지 URL, 링크 URL, 타입, 기간 등을 수정합니다.
     *
     * [프론트엔드 요청]
     * PUT /api/events/1
     * Authorization: Bearer {accessToken}
     * {
     *   "title": "수정된 제목",
     *   "imageUrl": "https://new-image.jpg",
     *   "startDate": "2026-04-01T00:00:00",
     *   "endDate": "2026-04-30T23:59:59"
     * }
     *
     * @param eventId 이벤트 ID
     * @param request 이벤트 수정 요청 DTO
     * @return 수정된 이벤트 상세 정보
     */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 수정", description = "관리자 전용")
    public ResponseEntity<EventDetailDTO> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    /**
     * 이벤트 삭제 (관리자용)
     *
     * 특정 이벤트를 삭제합니다.
     *
     * [프론트엔드 요청]
     * DELETE /api/events/1
     * Authorization: Bearer {accessToken}
     *
     * @param eventId 이벤트 ID
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 삭제", description = "관리자 전용")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 이벤트 활성화/비활성화 토글 (관리자용)
     *
     * 이벤트의 활성화 상태를 토글합니다.
     *
     * [프론트엔드 요청]
     * PATCH /api/events/1/toggle
     * Authorization: Bearer {accessToken}
     *
     * @param eventId 이벤트 ID
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @PatchMapping("/{eventId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "이벤트 활성화/비활성화 토글", description = "관리자 전용")
    public ResponseEntity<Map<String, String>> toggleEventActive(@PathVariable Long eventId) {
        eventService.toggleEventActive(eventId);
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }
}
