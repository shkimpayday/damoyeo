package com.damoyeo.api.domain.notification.controller;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.notification.dto.NotificationDTO;
import com.damoyeo.api.domain.notification.service.NotificationService;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================================
 * 알림(Notification) REST API 컨트롤러
 * ============================================================================
 *
 * [역할]
 * 알림 관련 HTTP 요청을 처리하는 REST API 엔드포인트를 제공합니다.
 *
 * [기본 URL]
 * /api/notifications
 *
 * [API 목록]
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │ Method │ URL                          │ 설명                │ 인증 │
 * ├────────────────────────────────────────────────────────────────────────┤
 * │ GET    │ /api/notifications           │ 알림 목록 조회      │ O   │
 * │ GET    │ /api/notifications/unread/count │ 읽지 않은 개수   │ O   │
 * │ PATCH  │ /api/notifications/{id}/read │ 개별 읽음 처리      │ O   │
 * │ PATCH  │ /api/notifications/read-all  │ 전체 읽음 처리      │ O   │
 * └────────────────────────────────────────────────────────────────────────┘
 * (모든 API 인증 필수)
 *
 * [Swagger 문서]
 * @Tag로 API 그룹화, @Operation으로 각 API 설명
 * http://localhost:8080/swagger-ui.html 에서 확인 가능
 *
 * [사용 어노테이션 설명]
 * - @RestController: @Controller + @ResponseBody (JSON 응답)
 * - @RequestMapping: 기본 URL 경로 설정
 * - @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * - @AuthenticationPrincipal: JWT에서 추출한 사용자 정보 주입
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    /** 알림 서비스 (비즈니스 로직 처리) */
    private final NotificationService notificationService;

    // ========================================================================
    // 알림 조회
    // ========================================================================

    /**
     * 알림 목록 조회 (페이지네이션)
     *
     * 현재 로그인한 사용자의 알림 목록을 조회합니다.
     * 최신순으로 정렬됩니다.
     *
     * [프론트엔드 요청]
     * GET /api/notifications?page=1&size=10
     * Authorization: Bearer {accessToken}
     *
     * [응답 예시]
     * {
     *   "dtoList": [
     *     {
     *       "id": 1,
     *       "type": "NEW_MEMBER",
     *       "title": "새 멤버 가입",
     *       "message": "홍길동님이 강남 러닝 크루에 가입했습니다.",
     *       "relatedId": 5,
     *       "isRead": false,
     *       "createdAt": "2024-05-01T10:30:00"
     *     }
     *   ],
     *   "pageNumList": [1, 2, 3],
     *   "totalCount": 25,
     *   ...
     * }
     *
     * @param member JWT에서 추출한 사용자 정보
     * @param pageRequestDTO 페이지 정보 (page, size)
     * @return 페이지네이션된 알림 목록
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회")
    public ResponseEntity<PageResponseDTO<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal MemberDTO member,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(notificationService.getNotifications(member.getEmail(), pageRequestDTO));
    }

    /**
     * 읽지 않은 알림 개수 조회
     *
     * 헤더의 알림 벨에 배지로 표시할 개수를 조회합니다.
     *
     * [프론트엔드 요청]
     * GET /api/notifications/unread/count
     * Authorization: Bearer {accessToken}
     *
     * [응답 예시]
     * { "count": 5 }
     *
     * [프론트엔드 UI]
     * ┌────────────────┐
     * │  🔔⑤ 홍길동 님 │  ← 배지에 5 표시
     * └────────────────┘
     *
     * @param member JWT 사용자 정보
     * @return 읽지 않은 알림 개수 { "count": N }
     */
    @GetMapping("/unread/count")
    @Operation(summary = "읽지 않은 알림 개수")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(member.getEmail())));
    }

    // ========================================================================
    // 읽음 처리
    // ========================================================================

    /**
     * 개별 알림 읽음 처리
     *
     * 특정 알림을 읽음으로 표시합니다.
     * 알림을 클릭했을 때 호출합니다.
     *
     * [권한]
     * 본인의 알림만 읽음 처리 가능
     *
     * [프론트엔드 요청]
     * PATCH /api/notifications/123/read
     * Authorization: Bearer {accessToken}
     *
     * [응답 예시]
     * { "result": "SUCCESS" }
     *
     * @param id 알림 ID
     * @param member JWT 사용자 정보
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        notificationService.markAsRead(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 모든 알림 읽음 처리
     *
     * 현재 사용자의 모든 알림을 읽음으로 표시합니다.
     * "모두 읽음" 버튼을 클릭했을 때 호출합니다.
     *
     * [프론트엔드 요청]
     * PATCH /api/notifications/read-all
     * Authorization: Bearer {accessToken}
     *
     * [응답 예시]
     * { "result": "SUCCESS" }
     *
     * [프론트엔드 UI]
     * ┌─────────────────────────────────────┐
     * │  🔔 알림                 [모두 읽음] │← 이 버튼 클릭 시
     * └─────────────────────────────────────┘
     *
     * @param member JWT 사용자 정보
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal MemberDTO member) {
        notificationService.markAllAsRead(member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    @PatchMapping("/{id}/delete")
    @Operation(summary = "알림 삭제처리")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member
    ) {
        notificationService.delete(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }
}
