package com.damoyeo.api.domain.payment.dto;

import com.damoyeo.api.domain.payment.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 카카오페이 결제 준비 요청 DTO
 * ============================================================================
 *
 * [용도]
 * 클라이언트에서 결제 준비 요청 시 사용하는 DTO입니다.
 *
 * [필수 정보]
 * - paymentType: 결제 유형 (PREMIUM_MONTHLY, PREMIUM_YEARLY)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyRequest {

    /**
     * 결제 유형
     * - PREMIUM_MONTHLY: 프리미엄 월간 구독 (3,900원)
     * - PREMIUM_YEARLY: 프리미엄 연간 구독 (39,000원)
     */
    private PaymentType paymentType;
}
