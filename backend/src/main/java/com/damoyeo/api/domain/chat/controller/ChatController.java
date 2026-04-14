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
 * 채팅 컨트롤러.
 * REST API(히스토리 조회, 읽음 처리, 채팅방 목록)와
 * WebSocket(@MessageMapping)을 통한 실시간 메시지 송수신을 담당한다.
 *
 * WebSocket 엔드포인트:
 * - SEND      /app/chat/{groupId}       메시지 전송
 * - SUBSCRIBE /topic/chat/{groupId}     메시지 수신
 * - SUBSCRIBE /user/queue/errors        에러 수신 (개인)
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;


    /** GET /api/chat/{groupId}/messages - 메시지 히스토리 조회 (페이지네이션) */
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

    /** GET /api/chat/{groupId}/unread-count - 읽지 않은 메시지 개수 */
    @GetMapping("/{groupId}/unread-count")
    @Operation(summary = "읽지 않은 메시지 개수", description = "특정 모임의 읽지 않은 메시지 개수를 조회합니다.")
    public ResponseEntity<Integer> getUnreadCount(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 읽지 않은 메시지 개수 조회 - groupId: {}", groupId);

        int count = chatService.getUnreadCount(groupId, member.getEmail());

        return ResponseEntity.ok(count);
    }

    /** POST /api/chat/{groupId}/read - 메시지 읽음 처리 */
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

    /** GET /api/chat/my-chats - 내 채팅방 목록 (최신 메시지 + 읽지 않은 수 포함) */
    @GetMapping("/my-chats")
    @Operation(summary = "내 채팅방 목록", description = "내가 속한 모든 모임의 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(@AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 내 채팅방 목록 조회 - email: {}", member.getEmail());

        List<ChatRoomDTO> chatRooms = chatService.getMyChatRooms(member.getEmail());

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * WebSocket: SEND /app/chat/{groupId} → 검증·저장 후 /topic/chat/{groupId}로 브로드캐스트.
     * 에러 시 발신자의 /user/queue/errors로 전송.
     */
    @MessageMapping("/chat/{groupId}")
    public void sendMessage(
            @DestinationVariable Long groupId,
            @Payload @Valid SendMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 메시지 전송 - groupId: {}, email: {}", groupId, principal.getName());

        try {
            ChatMessageDTO message = chatService.sendMessage(
                    groupId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 메시지 저장 완료 - id: {}", message.getId());

            // /topic/chat/{groupId}를 구독한 모든 클라이언트에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + groupId,
                    message
            );

            log.info("[WebSocket] 브로드캐스트 완료 - destination: /topic/chat/{}", groupId);

        } catch (Exception e) {
            log.error("[WebSocket] 메시지 전송 실패: {}", e.getMessage(), e);

            // /user/queue/errors를 구독한 발신자에게만 전송
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * WebSocket: SEND /app/chat/{groupId}/typing → /topic/chat/{groupId}/typing 브로드캐스트.
     * DB 저장 없이 임시 이벤트로만 처리. 프론트엔드에서 3초 타이머로 제거.
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


    /** GET /api/chat/meeting/{meetingId}/messages - 정모 메시지 히스토리 (참석자 전용) */
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

    /** GET /api/chat/meeting/{meetingId}/unread-count - 정모 읽지 않은 메시지 개수 (참석자 전용) */
    @GetMapping("/meeting/{meetingId}/unread-count")
    @Operation(summary = "정모 읽지 않은 메시지 개수", description = "특정 정모의 읽지 않은 메시지 개수를 조회합니다. (참석자 전용)")
    public ResponseEntity<Integer> getMeetingUnreadCount(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("[REST API] 정모 읽지 않은 메시지 개수 조회 - meetingId: {}", meetingId);

        int count = chatService.getMeetingUnreadCount(meetingId, member.getEmail());

        return ResponseEntity.ok(count);
    }

    /** POST /api/chat/meeting/{meetingId}/read - 정모 메시지 읽음 처리 (참석자 전용) */
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

    /** WebSocket: SEND /app/meeting-chat/{meetingId} → /topic/meeting-chat/{meetingId} 브로드캐스트. 참석자 전용. */
    @MessageMapping("/meeting-chat/{meetingId}")
    public void sendMeetingMessage(
            @DestinationVariable Long meetingId,
            @Payload @Valid SendMessageRequest request,
            Principal principal) {

        log.info("[WebSocket] 정모 메시지 전송 - meetingId: {}, email: {}", meetingId, principal.getName());

        try {
            ChatMessageDTO message = chatService.sendMeetingMessage(
                    meetingId,
                    principal.getName(),
                    request.getMessage()
            );

            log.info("[WebSocket] 정모 메시지 저장 완료 - id: {}", message.getId());

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

    /** WebSocket: SEND /app/meeting-chat/{meetingId}/typing → /topic/meeting-chat/{meetingId}/typing 브로드캐스트. */
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
