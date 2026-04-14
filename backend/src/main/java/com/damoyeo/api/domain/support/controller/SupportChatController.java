package com.damoyeo.api.domain.support.controller;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.support.dto.*;
import com.damoyeo.api.domain.support.service.SupportChatService;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * 상담 채팅 API Controller
 *
 * - REST API: 상담 생성, 조회, 메시지 히스토리
 * - WebSocket: 실시간 상담 메시지 송수신
 *
 *
 * === 사용자 REST API ===
 * POST   /api/support                          - 상담 생성
 * GET    /api/support/my-chats                 - 내 상담 목록
 * GET    /api/support/active                   - 활성 상담 조회
 * GET    /api/support/{chatId}                 - 상담 상세 조회
 * GET    /api/support/{chatId}/messages        - 메시지 히스토리
 * POST   /api/support/{chatId}/rate            - 상담 평가
 *
 * === 관리자 REST API ===
 * GET    /api/support/admin/waiting            - 대기 중인 상담 목록
 * GET    /api/support/admin/my-assigned        - 내가 담당 중인 상담
 * GET    /api/support/admin/all                - 전체 상담 목록
 * POST   /api/support/admin/{chatId}/assign    - 상담 배정
 * POST   /api/support/admin/{chatId}/complete  - 상담 완료
 * GET    /api/support/admin/waiting-count      - 대기 중인 상담 개수
 *
 * === WebSocket ===
 * SEND      /app/support/{chatId}              - 메시지 전송 (클라이언트 → 서버)
 * SUBSCRIBE /topic/support/{chatId}            - 메시지 수신 (서버 → 클라이언트)
 * SUBSCRIBE /user/queue/errors                 - 에러 메시지 수신 (개인)
 *
 * [프론트엔드 사용 예시]
 * ```typescript
 * // 1. 상담방 구독
 * client.subscribe('/topic/support/1', (message) => {
 *   const supportMessage = JSON.parse(message.body);
 *   console.log('새 메시지:', supportMessage);
 * });
 *
 * // 2. 메시지 전송
 * client.publish({
 *   destination: '/app/support/1',
 *   body: JSON.stringify({ message: '안녕하세요' })
 * });
 * ```
 *
 */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Support Chat", description = "상담 채팅 API")
public class SupportChatController {

    private final SupportChatService supportChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 사용자 REST API

    /**
     * 새 상담 생성
     *
     * [용도]
     * 사용자가 새로운 상담을 요청합니다.
     *
     * [요청]
     * POST /api/support
     * {
     *   "title": "결제 관련 문의",
     *   "message": "결제가 완료되었는데 프리미엄이 적용되지 않았습니다."
     * }
     *
     * [응답]
     * 생성된 SupportChatDTO
     */
    @PostMapping
    @Operation(summary = "상담 생성", description = "새로운 상담을 시작합니다.")
    public ResponseEntity<SupportChatDTO> createSupportChat(
            @Valid @RequestBody CreateSupportChatRequest request,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 상담 생성 - email: {}, title: {}", member.getEmail(), request.getTitle());

        SupportChatDTO chat = supportChatService.createSupportChat(member.getEmail(), request);

        return ResponseEntity.ok(chat);
    }

    /**
     * 내 상담 목록 조회
     *
     * [용도]
     * 사용자의 상담 이력을 조회합니다.
     */
    @GetMapping("/my-chats")
    @Operation(summary = "내 상담 목록", description = "내가 요청한 상담 목록을 조회합니다.")
    public ResponseEntity<List<SupportChatDTO>> getMySupportChats(
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 내 상담 목록 조회 - email: {}", member.getEmail());

        List<SupportChatDTO> chats = supportChatService.getMySupportChats(member.getEmail());

        return ResponseEntity.ok(chats);
    }

    /**
     * 활성 상담 조회
     *
     * [용도]
     * 현재 진행 중인 상담이 있는지 확인합니다.
     * 플로팅 버튼 클릭 시 기존 상담으로 이동할지 결정하는 데 사용합니다.
     *
     * [응답]
     * - 활성 상담이 있으면: SupportChatDTO
     * - 없으면: null (204 No Content)
     */
    @GetMapping("/active")
    @Operation(summary = "활성 상담 조회", description = "현재 진행 중인 상담을 조회합니다.")
    public ResponseEntity<SupportChatDTO> getActiveSupportChat(
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 활성 상담 조회 - email: {}", member.getEmail());

        SupportChatDTO chat = supportChatService.getActiveSupportChat(member.getEmail());

        if (chat == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chat);
    }

    /**
     * 상담 상세 조회
     *
     * [권한]
     * 상담 요청자 또는 관리자만 조회 가능합니다.
     */
    @GetMapping("/{chatId}")
    @Operation(summary = "상담 상세 조회", description = "상담의 상세 정보를 조회합니다.")
    public ResponseEntity<SupportChatDTO> getSupportChatDetail(
            @PathVariable Long chatId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 상담 상세 조회 - chatId: {}, email: {}", chatId, member.getEmail());

        SupportChatDTO chat = supportChatService.getSupportChatDetail(chatId, member.getEmail());

        return ResponseEntity.ok(chat);
    }

    /**
     * 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 상담방 진입 시 이전 메시지를 로드합니다.
     */
    @GetMapping("/{chatId}/messages")
    @Operation(summary = "메시지 히스토리 조회", description = "상담의 메시지 히스토리를 페이지네이션으로 조회합니다.")
    public ResponseEntity<PageResponseDTO<SupportMessageDTO>> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 메시지 히스토리 조회 - chatId: {}, page: {}", chatId, page);

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<SupportMessageDTO> messages = supportChatService.getMessages(
                chatId, member.getEmail(), pageRequest);

        return ResponseEntity.ok(messages);
    }

    /**
     * 상담 평가
     *
     * [요청]
     * POST /api/support/{chatId}/rate
     * {
     *   "rating": 5
     * }
     */
    @PostMapping("/{chatId}/rate")
    @Operation(summary = "상담 평가", description = "완료된 상담에 대해 만족도를 평가합니다.")
    public ResponseEntity<Void> rateSupportChat(
            @PathVariable Long chatId,
            @RequestBody Map<String, Integer> request,
            @AuthenticationPrincipal MemberDTO member) {

        Integer rating = request.get("rating");

        log.info("[REST API] 상담 평가 - chatId: {}, rating: {}", chatId, rating);

        supportChatService.rateSupportChat(chatId, member.getEmail(), rating);

        return ResponseEntity.ok().build();
    }

    // 관리자 REST API

    /**
     * 대기 중인 상담 목록 (관리자용)
     *
     * [권한]
     * ADMIN 역할 필요
     */
    @GetMapping("/admin/waiting")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "대기 중인 상담 목록", description = "대기 중인 상담 목록을 조회합니다. (관리자 전용)")
    public ResponseEntity<PageResponseDTO<SupportChatDTO>> getWaitingSupportChats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("[REST API] 대기 중인 상담 목록 조회");

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<SupportChatDTO> chats = supportChatService.getWaitingSupportChats(pageRequest);

        return ResponseEntity.ok(chats);
    }

    /**
     * 내가 담당 중인 상담 목록 (관리자용)
     *
     * [권한]
     * ADMIN 역할 필요
     */
    @GetMapping("/admin/my-assigned")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "내가 담당 중인 상담", description = "내가 담당 중인 상담 목록을 조회합니다. (관리자 전용)")
    public ResponseEntity<PageResponseDTO<SupportChatDTO>> getMyAssignedChats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 내 담당 상담 목록 조회 - email: {}", member.getEmail());

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<SupportChatDTO> chats = supportChatService.getMyAssignedChats(
                member.getEmail(), pageRequest);

        return ResponseEntity.ok(chats);
    }

    /**
     * 전체 상담 목록 (관리자용)
     *
     * [권한]
     * ADMIN 역할 필요
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 상담 목록", description = "전체 상담 목록을 조회합니다. (관리자 전용)")
    public ResponseEntity<PageResponseDTO<SupportChatDTO>> getAllSupportChats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("[REST API] 전체 상담 목록 조회");

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<SupportChatDTO> chats = supportChatService.getAllSupportChats(pageRequest);

        return ResponseEntity.ok(chats);
    }

    /**
     * 상담 배정 (관리자가 상담을 가져감)
     *
     * [권한]
     * ADMIN 역할 필요
     */
    @PostMapping("/admin/{chatId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상담 배정", description = "대기 중인 상담을 내가 담당합니다. (관리자 전용)")
    public ResponseEntity<SupportChatDTO> assignSupportChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 상담 배정 - chatId: {}, admin: {}", chatId, member.getEmail());

        SupportChatDTO chat = supportChatService.assignSupportChat(chatId, member.getEmail());

        // WebSocket으로 사용자에게 알림
        messagingTemplate.convertAndSend(
                "/topic/support/" + chatId,
                Map.of("type", "ADMIN_ASSIGNED", "chat", chat)
        );

        return ResponseEntity.ok(chat);
    }

    /**
     * 상담 완료 처리
     *
     * [권한]
     * 해당 상담의 담당 관리자만 가능
     */
    @PostMapping("/admin/{chatId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "상담 완료", description = "상담을 완료 처리합니다. (관리자 전용)")
    public ResponseEntity<SupportChatDTO> completeSupportChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 상담 완료 - chatId: {}, admin: {}", chatId, member.getEmail());

        SupportChatDTO chat = supportChatService.completeSupportChat(chatId, member.getEmail());

        // WebSocket으로 사용자에게 알림
        messagingTemplate.convertAndSend(
                "/topic/support/" + chatId,
                Map.of("type", "CHAT_COMPLETED", "chat", chat)
        );

        return ResponseEntity.ok(chat);
    }

    /**
     * 대기 중인 상담 개수 조회 (관리자용)
     *
     * [용도]
     * 관리자 대시보드에서 대기 중인 상담 배지를 표시합니다.
     */
    @GetMapping("/admin/waiting-count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "대기 중인 상담 개수", description = "대기 중인 상담 개수를 조회합니다. (관리자 전용)")
    public ResponseEntity<Long> getWaitingCount() {

        long count = supportChatService.getWaitingCount();

        return ResponseEntity.ok(count);
    }

    // WebSocket - 실시간 메시지 송수신

    /**
     * WebSocket: 사용자 메시지 전송
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/support/1
     * 2. 서버: 메시지 저장
     * 3. 서버: 브로드캐스트 → /topic/support/1
     */
    @MessageMapping("/support/{chatId}")
    public void sendUserMessage(
            @DestinationVariable Long chatId,
            @Payload @Valid SendSupportMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 상담 메시지 전송 - chatId: {}, email: {}", chatId, principal.getName());

        try {
            // 메시지 저장
            SupportMessageDTO message = supportChatService.sendUserMessage(
                    chatId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 상담 메시지 저장 완료 - id: {}", message.getId());

            // 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/support/" + chatId,
                    message
            );

            log.info("[WebSocket] 상담 브로드캐스트 완료 - destination: /topic/support/{}", chatId);

        } catch (Exception e) {
            log.error("[WebSocket] 상담 메시지 전송 실패: {}", e.getMessage(), e);

            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * WebSocket: 관리자 메시지 전송
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/support-admin/1
     * 2. 서버: 권한 검증 및 메시지 저장
     * 3. 서버: 브로드캐스트 → /topic/support/1
     */
    @MessageMapping("/support-admin/{chatId}")
    public void sendAdminMessage(
            @DestinationVariable Long chatId,
            @Payload @Valid SendSupportMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 관리자 상담 메시지 전송 - chatId: {}, admin: {}", chatId, principal.getName());

        try {
            // 관리자 메시지 저장
            SupportMessageDTO message = supportChatService.sendAdminMessage(
                    chatId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 관리자 상담 메시지 저장 완료 - id: {}", message.getId());

            // 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/support/" + chatId,
                    message
            );

            log.info("[WebSocket] 관리자 상담 브로드캐스트 완료 - destination: /topic/support/{}", chatId);

        } catch (Exception e) {
            log.error("[WebSocket] 관리자 상담 메시지 전송 실패: {}", e.getMessage(), e);

            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * WebSocket: 타이핑 인디케이터
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/support/{chatId}/typing
     * 2. 서버: 브로드캐스트 → /topic/support/{chatId}/typing
     */
    @MessageMapping("/support/{chatId}/typing")
    public void handleTyping(
            @DestinationVariable Long chatId,
            @Payload Map<String, Object> payload,
            Principal principal) {

        log.debug("[WebSocket] 상담 타이핑 이벤트 - chatId: {}, email: {}", chatId, principal.getName());

        try {
            Map<String, Object> typingEvent = Map.of(
                    "email", principal.getName(),
                    "typing", payload.getOrDefault("typing", true),
                    "isAdmin", payload.getOrDefault("isAdmin", false)
            );

            messagingTemplate.convertAndSend(
                    "/topic/support/" + chatId + "/typing",
                    typingEvent
            );

        } catch (Exception e) {
            log.error("[WebSocket] 상담 타이핑 이벤트 실패: {}", e.getMessage(), e);
        }
    }
}
