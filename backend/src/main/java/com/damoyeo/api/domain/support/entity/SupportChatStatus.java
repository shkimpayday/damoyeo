package com.damoyeo.api.domain.support.entity;

/**
 * ============================================================================
 * 상담 채팅 상태 Enum
 * ============================================================================
 *
 * [상태 흐름]
 * WAITING → IN_PROGRESS → COMPLETED
 *
 * @author damoyeo
 * @since 2025-03-16
 */
public enum SupportChatStatus {

    /**
     * 대기 중
     * 사용자가 상담을 요청했지만 아직 관리자가 응답하지 않은 상태
     */
    WAITING,

    /**
     * 진행 중
     * 관리자가 상담에 참여하여 대화가 진행 중인 상태
     */
    IN_PROGRESS,

    /**
     * 완료
     * 상담이 종료된 상태
     */
    COMPLETED
}
