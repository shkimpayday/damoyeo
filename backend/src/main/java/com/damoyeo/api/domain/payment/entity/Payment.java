package com.damoyeo.api.domain.payment.entity;

import com.damoyeo.api.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 결제 엔티티
 * ============================================================================
 *
 * [역할]
 * 사용자의 결제 정보를 저장합니다.
 * 카카오페이 결제 프로세스의 모든 단계를 추적합니다.
 *
 * [결제 프로세스]
 * 1. 결제 준비 (ready) → tid 발급
 * 2. 사용자 결제 진행 → pg_token 수신
 * 3. 결제 승인 (approve) → 결제 완료
 *
 * [연관 관계]
 * - Member (N:1): 결제한 회원
 */
@Entity
@Table(name = "payment",
        indexes = {
                @Index(name = "idx_payment_member", columnList = "member_id"),
                @Index(name = "idx_payment_tid", columnList = "tid"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_created_at", columnList = "created_at")
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

    /**
     * 결제 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 결제한 회원
     * - 지연 로딩: 결제 목록 조회 시 N+1 방지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 결제 유형
     * - PREMIUM_MONTHLY: 월간 구독
     * - PREMIUM_YEARLY: 연간 구독
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentType paymentType;

    /**
     * 결제 상태
     * - READY: 준비
     * - APPROVED: 승인
     * - CANCELLED: 취소
     * - FAILED: 실패
     * - REFUNDED: 환불
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * 결제 금액
     */
    @Column(nullable = false)
    private int amount;

    /**
     * 카카오페이 거래 고유번호
     * 결제 준비 시 발급받음
     */
    @Column(length = 50)
    private String tid;

    /**
     * 주문번호 (내부 관리용)
     * 형식: PAY_{memberId}_{timestamp}
     */
    @Column(name = "order_id", length = 50, unique = true)
    private String orderId;

    /**
     * 결제 수단
     * 예: CARD, MONEY (카카오페이머니)
     */
    @Column(name = "payment_method_type", length = 20)
    private String paymentMethodType;

    /**
     * 결제 승인 시간
     * 카카오페이에서 반환하는 승인 시간
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 프리미엄 시작일
     * 결제 승인 시점
     */
    @Column(name = "premium_start_date")
    private LocalDateTime premiumStartDate;

    /**
     * 프리미엄 종료일
     * paymentType의 durationDays 기준으로 계산
     */
    @Column(name = "premium_end_date")
    private LocalDateTime premiumEndDate;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // 비즈니스 메서드
    // ========================================================================

    /**
     * 결제 준비 완료 처리
     *
     * @param tid 카카오페이 거래 고유번호
     */
    public void ready(String tid) {
        this.tid = tid;
        this.status = PaymentStatus.READY;
    }

    /**
     * 결제 승인 처리
     *
     * @param paymentMethodType 결제 수단 (CARD, MONEY)
     * @param approvedAt 승인 시간
     */
    public void approve(String paymentMethodType, LocalDateTime approvedAt) {
        this.status = PaymentStatus.APPROVED;
        this.paymentMethodType = paymentMethodType;
        this.approvedAt = approvedAt;
        this.premiumStartDate = approvedAt;
        this.premiumEndDate = approvedAt.plusDays(this.paymentType.getDurationDays());
    }

    /**
     * 결제 취소 처리
     */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * 결제 실패 처리
     */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    /**
     * 환불 처리
     */
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    /**
     * 프리미엄 활성 여부 확인
     *
     * @return true: 프리미엄 활성, false: 만료됨
     */
    public boolean isPremiumActive() {
        if (this.status != PaymentStatus.APPROVED) {
            return false;
        }
        return this.premiumEndDate != null && LocalDateTime.now().isBefore(this.premiumEndDate);
    }

    // ========================================================================
    // 관리자 전용 메서드
    // ========================================================================

    /**
     * 프리미엄 기간 직접 설정 (관리자용)
     *
     * @param startDate 시작일
     * @param endDate 종료일
     */
    public void setPremiumDates(LocalDateTime startDate, LocalDateTime endDate) {
        this.premiumStartDate = startDate;
        this.premiumEndDate = endDate;
    }

    /**
     * 프리미엄 기간 연장/감소 (관리자용)
     *
     * @param newEndDate 새로운 종료일
     */
    public void extendPremium(LocalDateTime newEndDate) {
        this.premiumEndDate = newEndDate;
    }
}
