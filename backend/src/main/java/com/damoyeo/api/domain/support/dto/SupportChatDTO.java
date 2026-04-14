package com.damoyeo.api.domain.support.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.support.entity.SupportChatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 채팅 DTO
 *
 * 상담 채팅 정보를 클라이언트에 전달합니다.
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportChatDTO {

    /**
     * 상담 채팅 ID
     */
    private Long id;

    /**
     * 상담 요청 사용자
     */
    private MemberSummaryDTO user;

    /**
     * 담당 관리자 (null 가능)
     */
    private MemberSummaryDTO admin;

    /**
     * 상담 제목
     */
    private String title;

    /**
     * 상담 상태
     */
    private SupportChatStatus status;

    /**
     * 최신 메시지 (미리보기용)
     */
    private SupportMessageDTO latestMessage;

    /**
     * 읽지 않은 메시지 개수
     */
    private int unreadCount;

    /**
     * 상담 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 상담 종료 시각
     */
    private LocalDateTime completedAt;

    /**
     * 만족도 평가
     */
    private Integer rating;
}
