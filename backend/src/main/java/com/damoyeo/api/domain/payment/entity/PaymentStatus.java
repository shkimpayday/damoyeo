package com.damoyeo.api.domain.payment.entity;

/**
 * 결제 상태 열거형
 *
 * [상태 흐름]
 * READY → APPROVED (성공)
 *       → CANCELLED (취소)
 *       → FAILED (실패)
 *
 * [카카오페이 상태 매핑]
 * - READY: 결제 준비 완료 (redirect URL 발급됨)
 * - APPROVED: 결제 승인 완료
 * - CANCELLED: 사용자가 결제 취소
 * - FAILED: 결제 실패
 * - REFUNDED: 환불 완료
 */
public enum PaymentStatus {

    /**
     * 결제 준비 완료
     * 카카오페이 결제창 URL이 발급된 상태
     */
    READY,

    /**
     * 결제 승인 완료
     * 사용자가 결제를 완료하고 승인된 상태
     */
    APPROVED,

    /**
     * 결제 취소됨
     * 사용자가 결제 진행 중 취소한 경우
     */
    CANCELLED,

    /**
     * 결제 실패
     * 결제 과정에서 오류가 발생한 경우
     */
    FAILED,

    /**
     * 환불 완료
     * 결제 승인 후 환불 처리된 경우
     */
    REFUNDED
}
