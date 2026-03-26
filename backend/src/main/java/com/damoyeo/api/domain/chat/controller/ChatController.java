package com.damoyeo.api.domain.chat.controller;

import com.damoyeo.api.domain.chat.dto.ChatMessageDTO;
import com.damoyeo.api.domain.chat.dto.ChatRoomDTO;
import com.damoyeo.api.domain.chat.dto.SendMessageRequest;
import com.damoyeo.api.domain.chat.service.ChatService;
import com.damoyeo.api.domain.member.dto.MemberDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 채팅 API Controller
 * ============================================================================
 *
 * [역할]
 * - REST API: 채팅 히스토리 조회, 읽음 처리, 채팅방 목록
 * - WebSocket: 실시간 메시지 송수신 (@MessageMapping)
 *
 * [엔드포인트 목록]
 *
 * === REST API ===
 * GET    /api/chat/{groupId}/messages        - 메시지 히스토리 (페이지네이션)
 * GET    /api/chat/{groupId}/unread-count    - 읽지 않은 메시지 개수
 * POST   /api/chat/{groupId}/read            - 읽음 처리
 * GET    /api/chat/my-chats                  - 내 채팅방 목록
 *
 * === WebSocket ===
 * SEND      /app/chat/{groupId}        - 메시지 전송 (클라이언트 → 서버)
 * SUBSCRIBE /topic/chat/{groupId}      - 메시지 수신 (서버 → 클라이언트)
 * SUBSCRIBE /user/queue/errors         - 에러 메시지 수신 (개인)
 *
 * [프론트엔드 사용 예시]
 * ```typescript
 * // 1. 채팅방 구독
 * client.subscribe('/topic/chat/1', (message) => {
 *   const chatMessage = JSON.parse(message.body);
 *   console.log('새 메시지:', chatMessage);
 * });
 *
 * // 2. 메시지 전송
 * client.publish({
 *   destination: '/app/chat/1',
 *   body: JSON.stringify({ message: '안녕하세요' })
 * });
 * ```
 *
 * @author damoyeo
 * @since 2025-02-25
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ========================================================================
    // REST API - 메시지 히스토리
    // ========================================================================

    /**
     * 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 채팅방 진입 시 최근 메시지를 로드합니다.
     * 무한 스크롤로 과거 메시지를 추가 로드할 수 있습니다.
     *
     * [프론트엔드 요청]
     * GET /api/chat/1/messages?page=1&size=50
     * Authorization: Bearer {accessToken}
     *
     * [응답]
     * {
     *   "dtoList": [...],
     *   "totalCount": 123,
     *   "page": 1,
     *   "size": 50,
     *   ...
     * }
     */
    @GetMapping("/{groupId}/messages")
    @Operation(summary = "메시지 히스토리 조회", description = "모임의 채팅 메시지를 페이지네이션으로 조회합니다.")
    public ResponseEntity<PageResponseDTO<ChatMessageDTO>> getMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 메시지 히스토리 조회 - groupId: {}, page: {}", groupId, page);

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<ChatMessageDTO> messages = chatService.getMessages(
                groupId,
                member.getEmail(),
                pageRequest
        );

        return ResponseEntity.ok(messages);
    }

    /**
     * 읽지 않은 메시지 개수 조회
     *
     * [용도]
     * 채팅방 목록에서 읽지 않은 메시지 배지를 표시합니다.
     *
     * [프론트엔드 요청]
     * GET /api/chat/1/unread-count
     * Authorization: Bearer {accessToken}
     *
     * [응답]
     * 5
     */
    @GetMapping("/{groupId}/unread-count")
    @Operation(summary = "읽지 않은 메시지 개수", description = "특정 모임의 읽지 않은 메시지 개수를 조회합니다.")
    public ResponseEntity<Integer> getUnreadCount(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 읽지 않은 메시지 개수 조회 - groupId: {}", groupId);

        int count = chatService.getUnreadCount(groupId, member.getEmail());

        return ResponseEntity.ok(count);
    }

    /**
     * 메시지 읽음 처리
     *
     * [용도]
     * 채팅방에서 메시지를 읽었을 때 호출합니다.
     * 마지막으로 읽은 메시지 ID를 업데이트하여 unread count를 감소시킵니다.
     *
     * [프론트엔드 요청]
     * POST /api/chat/1/read
     * Authorization: Bearer {accessToken}
     * {
     *   "lastReadMessageId": 123
     * }
     *
     * [응답]
     * 200 OK
     */
    @PostMapping("/{groupId}/read")
    @Operation(summary = "메시지 읽음 처리", description = "마지막으로 읽은 메시지 ID를 업데이트합니다.")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long groupId,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal MemberDTO member) {

        Long lastReadMessageId = request.get("lastReadMessageId");

        log.info("[REST API] 읽음 처리 - groupId: {}, messageId: {}", groupId, lastReadMessageId);

        chatService.markAsRead(groupId, member.getEmail(), lastReadMessageId);

        return ResponseEntity.ok().build();
    }

    /**
     * 내 채팅방 목록 조회
     *
     * [용도]
     * 사용자가 속한 모든 모임의 채팅방 목록을 조회합니다.
     * 각 채팅방의 최신 메시지와 읽지 않은 메시지 개수를 포함합니다.
     *
     * [프론트엔드 요청]
     * GET /api/chat/my-chats
     * Authorization: Bearer {accessToken}
     *
     * [응답]
     * [
     *   {
     *     "groupId": 1,
     *     "groupName": "강남 러닝 크루",
     *     "latestMessage": {...},
     *     "unreadCount": 5
     *   },
     *   ...
     * ]
     */
    @GetMapping("/my-chats")
    @Operation(summary = "내 채팅방 목록", description = "내가 속한 모든 모임의 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(@AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 내 채팅방 목록 조회 - email: {}", member.getEmail());

        List<ChatRoomDTO> chatRooms = chatService.getMyChatRooms(member.getEmail());

        return ResponseEntity.ok(chatRooms);
    }

    // ========================================================================
    // WebSocket - 실시간 메시지 송수신
    // ========================================================================

    /**
     * WebSocket: 메시지 전송
     *
     * [@MessageMapping]
     * WebSocket으로 들어오는 메시지를 처리합니다.
     * 클라이언트가 /app/chat/{groupId}로 SEND하면 이 메서드가 호출됩니다.
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/chat/1
     * 2. 서버: 메시지 검증 및 저장 (ChatService.sendMessage)
     * 3. 서버: 브로드캐스트 → /topic/chat/1 (모든 구독자에게 전송)
     * 4. 에러 발생 시: 발신자에게만 에러 메시지 전송 → /user/queue/errors
     *
     * [클라이언트 사용 예시]
     * ```typescript
     * client.publish({
     *   destination: '/app/chat/1',
     *   body: JSON.stringify({ message: '안녕하세요' })
     * });
     * ```
     *
     * [@DestinationVariable]
     * URL 경로의 변수를 파라미터로 추출합니다.
     * /app/chat/{groupId} → Long groupId
     *
     * [@Payload]
     * WebSocket 메시지 body를 DTO로 변환합니다.
     *
     * [Principal]
     * JWTChannelInterceptor에서 설정한 사용자 정보
     * principal.getName() → 사용자 이메일
     */
    @MessageMapping("/chat/{groupId}")
    public void sendMessage(
            @DestinationVariable Long groupId,
            @Payload @Valid SendMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 메시지 전송 - groupId: {}, email: {}", groupId, principal.getName());

        try {
            // 1. 메시지 저장 및 검증
            ChatMessageDTO message = chatService.sendMessage(
                    groupId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 메시지 저장 완료 - id: {}", message.getId());

            // 2. 구독자들에게 브로드캐스트
            // /topic/chat/{groupId}를 구독한 모든 클라이언트에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + groupId,
                    message
            );

            log.info("[WebSocket] 브로드캐스트 완료 - destination: /topic/chat/{}", groupId);

        } catch (Exception e) {
            log.error("[WebSocket] 메시지 전송 실패: {}", e.getMessage(), e);

            // 3. 에러 발생 시 발신자에게만 에러 메시지 전송
            // /user/queue/errors를 구독한 발신자에게만 전송
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
     * [역할]
     * 사용자가 메시지를 입력 중일 때 다른 사용자들에게 알립니다.
     * "○○○님이 입력 중..." UI를 표시하기 위한 이벤트입니다.
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/chat/{groupId}/typing
     * 2. 서버: 브로드캐스트 → /topic/chat/{groupId}/typing
     * 3. 다른 클라이언트: 타이핑 인디케이터 표시 (3초간)
     *
     * [클라이언트 사용 예시]
     * ```typescript
     * client.publish({
     *   destination: '/app/chat/1/typing',
     *   body: JSON.stringify({ typing: true })
     * });
     * ```
     *
     * [주의사항]
     * - DB에 저장하지 않음 (일시적 상태)
     * - 디바운싱으로 과도한 이벤트 방지 (프론트엔드 처리)
     * - 본인에게도 브로드캐스트됨 (프론트엔드에서 필터링)
     */
    @MessageMapping("/chat/{groupId}/typing")
    public void handleTyping(
            @DestinationVariable Long groupId,
            @Payload Map<String, Object> payload,
            Principal principal) {

        log.debug("[WebSocket] 타이핑 이벤트 - groupId: {}, email: {}", groupId, principal.getName());

        try {
            // 타이핑 상태 + 사용자 이메일을 함께 전송
            Map<String, Object> typingEvent = Map.of(
                    "email", principal.getName(),
                    "typing", payload.getOrDefault("typing", true)
            );

            // 모든 구독자에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + groupId + "/typing",
                    typingEvent
            );

        } catch (Exception e) {
            log.error("[WebSocket] 타이핑 이벤트 실패: {}", e.getMessage(), e);
        }
    }

    // ========================================================================
    // REST API - 정모 채팅 (참석자 전용)
    // ========================================================================

    /**
     * 정모 메시지 히스토리 조회 (페이지네이션)
     *
     * [권한]
     * ATTENDING 상태의 참석자만 조회 가능합니다.
     *
     * [프론트엔드 요청]
     * GET /api/chat/meeting/1/messages?page=1&size=50
     * Authorization: Bearer {accessToken}
     */
    @GetMapping("/meeting/{meetingId}/messages")
    @Operation(summary = "정모 메시지 히스토리 조회", description = "정모의 채팅 메시지를 페이지네이션으로 조회합니다. (참석자 전용)")
    public ResponseEntity<PageResponseDTO<ChatMessageDTO>> getMeetingMessages(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 정모 메시지 히스토리 조회 - meetingId: {}, page: {}", meetingId, page);

        PageRequestDTO pageRequest = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResponseDTO<ChatMessageDTO> messages = chatService.getMeetingMessages(
                meetingId,
                member.getEmail(),
                pageRequest
        );

        return ResponseEntity.ok(messages);
    }

    /**
     * 정모 읽지 않은 메시지 개수 조회
     *
     * [권한]
     * ATTENDING 상태의 참석자만 조회 가능합니다.
     */
    @GetMapping("/meeting/{meetingId}/unread-count")
    @Operation(summary = "정모 읽지 않은 메시지 개수", description = "특정 정모의 읽지 않은 메시지 개수를 조회합니다. (참석자 전용)")
    public ResponseEntity<Integer> getMeetingUnreadCount(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 정모 읽지 않은 메시지 개수 조회 - meetingId: {}", meetingId);

        int count = chatService.getMeetingUnreadCount(meetingId, member.getEmail());

        return ResponseEntity.ok(count);
    }

    /**
     * 정모 메시지 읽음 처리
     *
     * [권한]
     * ATTENDING 상태의 참석자만 가능합니다.
     */
    @PostMapping("/meeting/{meetingId}/read")
    @Operation(summary = "정모 메시지 읽음 처리", description = "마지막으로 읽은 메시지 ID를 업데이트합니다. (참석자 전용)")
    public ResponseEntity<Void> markMeetingAsRead(
            @PathVariable Long meetingId,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal MemberDTO member) {

        Long lastReadMessageId = request.get("lastReadMessageId");

        log.info("[REST API] 정모 읽음 처리 - meetingId: {}, messageId: {}", meetingId, lastReadMessageId);

        chatService.markMeetingAsRead(meetingId, member.getEmail(), lastReadMessageId);

        return ResponseEntity.ok().build();
    }

    // ========================================================================
    // WebSocket - 정모 실시간 메시지 송수신
    // ========================================================================

    /**
     * WebSocket: 정모 메시지 전송
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/meeting-chat/1
     * 2. 서버: 참석자 검증 및 메시지 저장
     * 3. 서버: 브로드캐스트 → /topic/meeting-chat/1
     * 4. 에러 발생 시: 발신자에게만 에러 메시지 전송
     *
     * [권한]
     * ATTENDING 상태의 참석자만 메시지 전송 가능합니다.
     */
    @MessageMapping("/meeting-chat/{meetingId}")
    public void sendMeetingMessage(
            @DestinationVariable Long meetingId,
            @Payload @Valid SendMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 정모 메시지 전송 - meetingId: {}, email: {}", meetingId, principal.getName());

        try {
            // 1. 메시지 저장 및 검증
            ChatMessageDTO message = chatService.sendMeetingMessage(
                    meetingId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 정모 메시지 저장 완료 - id: {}", message.getId());

            // 2. 구독자들에게 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/topic/meeting-chat/" + meetingId,
                    message
            );

            log.info("[WebSocket] 정모 브로드캐스트 완료 - destination: /topic/meeting-chat/{}", meetingId);

        } catch (Exception e) {
            log.error("[WebSocket] 정모 메시지 전송 실패: {}", e.getMessage(), e);

            // 에러 발생 시 발신자에게만 에러 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * WebSocket: 정모 타이핑 인디케이터
     *
     * [처리 흐름]
     * 1. 클라이언트: SEND → /app/meeting-chat/{meetingId}/typing
     * 2. 서버: 브로드캐스트 → /topic/meeting-chat/{meetingId}/typing
     */
    @MessageMapping("/meeting-chat/{meetingId}/typing")
    public void handleMeetingTyping(
            @DestinationVariable Long meetingId,
            @Payload Map<String, Object> payload,
            Principal principal) {

        log.debug("[WebSocket] 정모 타이핑 이벤트 - meetingId: {}, email: {}", meetingId, principal.getName());

        try {
            Map<String, Object> typingEvent = Map.of(
                    "email", principal.getName(),
                    "typing", payload.getOrDefault("typing", true)
            );

            messagingTemplate.convertAndSend(
                    "/topic/meeting-chat/" + meetingId + "/typing",
                    typingEvent
            );

        } catch (Exception e) {
            log.error("[WebSocket] 정모 타이핑 이벤트 실패: {}", e.getMessage(), e);
        }
    }
}
