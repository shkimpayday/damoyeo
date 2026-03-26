package com.damoyeo.api.domain.payment.entity;

/**
 * ============================================================================
 * 결제 유형 열거형
 * ============================================================================
 *
 * [결제 유형]
 * - PREMIUM_MONTHLY: 프리미엄 월간 구독 (3,900원)
 * - PREMIUM_YEARLY: 프리미엄 연간 구독 (39,000원)
 *
 * [확장 가능]
 * 추후 다른 유료 서비스 추가 시 유형 추가
 */
public enum PaymentType {

    /**
     * 프리미엄 월간 구독
     * - 가격: 3,900원
     * - 기간: 30일
     */
    PREMIUM_MONTHLY("프리미엄 월간 구독", 3900, 30),

    /**
     * 프리미엄 연간 구독
     * - 가격: 39,000원 (2개월 무료)
     * - 기간: 365일
     */
    PREMIUM_YEARLY("프리미엄 연간 구독", 39000, 365);

    private final String displayName;
    private final int price;
    private final int durationDays;

    PaymentType(String displayName, int price, int durationDays) {
        this.displayName = displayName;
        this.price = price;
        this.durationDays = durationDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrice() {
        return price;
    }

    public int getDurationDays() {
        return durationDays;
    }
}
