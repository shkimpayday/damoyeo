package com.damoyeo.api.domain.support.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 상담 메시지 DTO
 * ============================================================================
 *
 * @author damoyeo
 * @since 2025-03-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportMessageDTO {

    /**
     * 메시지 ID
     */
    private Long id;

    /**
     * 소속 상담 채팅 ID
     */
    private Long supportChatId;

    /**
     * 발신자 정보
     */
    private MemberSummaryDTO sender;

    /**
     * 메시지 내용
     */
    private String message;

    /**
     * 관리자 메시지 여부
     *
     * @JsonProperty: Jackson이 boolean 필드명 'isAdmin'을 'admin'으로 직렬화하는 것을 방지
     */
    @JsonProperty("isAdmin")
    private boolean isAdmin;

    /**
     * 전송 시각
     */
    private LocalDateTime createdAt;
}
