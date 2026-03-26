package com.damoyeo.api.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 메시지 전송 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 클라이언트가 WebSocket을 통해 메시지를 전송할 때 사용하는 요청 객체입니다.
 *
 * [검증]
 * Bean Validation을 사용하여 입력값을 검증합니다:
 * - @NotBlank: null, 빈 문자열, 공백만 있는 문자열 모두 차단
 * - @Size: 메시지 길이 제한 (최대 2000자)
 *
 * [사용 위치]
 * - ChatController.sendMessage (@MessageMapping)
 * - WebSocket STOMP 메시지의 body로 전달됩니다.
 *
 * [클라이언트 요청 예시]
 * WebSocket SEND → /app/chat/1
 * {
 *   "message": "안녕하세요!"
 * }
 *
 * [검증 실패 예시]
 * 1. 빈 메시지
 *    { "message": "" } → 400 Bad Request
 *
 * 2. 공백만 있는 메시지
 *    { "message": "   " } → 400 Bad Request
 *
 * 3. 너무 긴 메시지 (2000자 초과)
 *    { "message": "..." } → 400 Bad Request
 *
 * @author damoyeo
 * @since 2025-02-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {

    /**
     * 메시지 내용
     *
     * [검증 규칙]
     * - @NotBlank: null, 빈 문자열, 공백 문자열 불허
     * - @Size: 최소 1자, 최대 2000자
     *
     * [에러 메시지]
     * - "메시지를 입력해주세요" (NotBlank)
     * - "메시지는 2000자 이내로 작성해주세요" (Size)
     */
    @NotBlank(message = "메시지를 입력해주세요")
    @Size(min = 1, max = 2000, message = "메시지는 2000자 이내로 작성해주세요")
    private String message;
}
