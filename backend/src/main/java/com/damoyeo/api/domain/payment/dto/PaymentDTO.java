package com.damoyeo.api.domain.payment.dto;

import com.damoyeo.api.domain.payment.entity.PaymentStatus;
import com.damoyeo.api.domain.payment.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 결제 정보 DTO
 * ============================================================================
 *
 * [용도]
 * 결제 내역 조회 시 클라이언트에 전달하는 DTO입니다.
 *
 * [포함 정보]
 * - 결제 기본 정보 (ID, 금액, 상태)
 * - 결제 유형 정보 (월간/연간)
 * - 프리미엄 기간 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    /**
     * 결제 ID
     */
    private Long id;

    /**
     * 결제 유형
     */
    private PaymentType paymentType;

    /**
     * 결제 유형 표시명
     * 예: "프리미엄 월간 구독"
     */
    private String paymentTypeName;

    /**
     * 결제 상태
     */
    private PaymentStatus status;

    /**
     * 결제 상태 표시명
     * 예: "결제 완료", "결제 취소"
     */
    private String statusName;

    /**
     * 결제 금액
     */
    private int amount;

    /**
     * 결제 수단
     * 예: "카드", "카카오페이머니"
     */
    private String paymentMethod;

    /**
     * 주문번호
     */
    private String orderId;

    /**
     * 프리미엄 시작일
     */
    private LocalDateTime premiumStartDate;

    /**
     * 프리미엄 종료일
     */
    private LocalDateTime premiumEndDate;

    /**
     * 프리미엄 활성 여부
     */
    private boolean premiumActive;

    /**
     * 결제 승인 시간
     */
    private LocalDateTime approvedAt;

    /**
     * 결제 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 상태 표시명 반환
     */
    public static String getStatusDisplayName(PaymentStatus status) {
        return switch (status) {
            case READY -> "결제 대기";
            case APPROVED -> "결제 완료";
            case CANCELLED -> "결제 취소";
            case FAILED -> "결제 실패";
            case REFUNDED -> "환불 완료";
        };
    }

    /**
     * 결제 수단 표시명 반환
     */
    public static String getPaymentMethodDisplayName(String methodType) {
        if (methodType == null) return null;
        return switch (methodType) {
            case "CARD" -> "카드";
            case "MONEY" -> "카카오페이머니";
            default -> methodType;
        };
    }
}
