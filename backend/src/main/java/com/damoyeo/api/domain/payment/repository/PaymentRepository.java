package com.damoyeo.api.domain.payment.repository;

import com.damoyeo.api.domain.payment.entity.Payment;
import com.damoyeo.api.domain.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * 결제 Repository
 * ============================================================================
 *
 * [역할]
 * Payment 엔티티에 대한 DB 접근을 담당합니다.
 *
 * [주요 쿼리]
 * - 회원별 결제 내역 조회
 * - 활성 프리미엄 결제 조회
 * - 만료 예정 프리미엄 조회 (알림용)
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ========================================================================
    // 기본 조회
    // ========================================================================

    /**
     * 주문번호로 결제 조회
     *
     * @param orderId 주문번호
     * @return 결제 정보
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * 카카오페이 TID로 결제 조회
     *
     * @param tid 카카오페이 거래 고유번호
     * @return 결제 정보
     */
    Optional<Payment> findByTid(String tid);

    // ========================================================================
    // 회원별 조회
    // ========================================================================

    /**
     * 회원의 결제 내역 조회 (최신순)
     *
     * @param memberId 회원 ID
     * @param pageable 페이지 정보
     * @return 결제 내역 목록
     */
    @Query("select p from Payment p where p.member.id = :memberId order by p.createdAt desc")
    Page<Payment> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 회원의 승인된 결제 내역 조회
     *
     * @param memberId 회원 ID
     * @return 승인된 결제 목록
     */
    @Query("select p from Payment p " +
            "where p.member.id = :memberId " +
            "and p.status = 'APPROVED' " +
            "order by p.createdAt desc")
    List<Payment> findApprovedByMemberId(@Param("memberId") Long memberId);

    // ========================================================================
    // 프리미엄 상태 조회
    // ========================================================================

    /**
     * 회원의 활성 프리미엄 결제 조회
     *
     * [조건]
     * - 상태가 APPROVED
     * - 프리미엄 종료일이 현재 시간 이후
     *
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @return 활성 프리미엄 결제 (있으면)
     */
    @Query("select p from Payment p " +
            "where p.member.id = :memberId " +
            "and p.status = 'APPROVED' " +
            "and p.premiumEndDate > :now " +
            "order by p.premiumEndDate desc")
    List<Payment> findActivePremiumByMemberId(@Param("memberId") Long memberId,
                                               @Param("now") LocalDateTime now);

    /**
     * 회원의 가장 최근 활성 프리미엄 결제 조회
     *
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @return 가장 최근 활성 프리미엄 결제
     */
    default Optional<Payment> findLatestActivePremium(Long memberId, LocalDateTime now) {
        List<Payment> payments = findActivePremiumByMemberId(memberId, now);
        return payments.isEmpty() ? Optional.empty() : Optional.of(payments.get(0));
    }

    /**
     * 프리미엄 만료 예정 결제 조회 (알림용)
     *
     * [용도]
     * 만료 3일 전 알림 발송용
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 만료 예정 결제 목록
     */
    @Query("select p from Payment p " +
            "where p.status = 'APPROVED' " +
            "and p.premiumEndDate >= :startTime " +
            "and p.premiumEndDate < :endTime")
    List<Payment> findExpiringPremiums(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    // ========================================================================
    // 통계
    // ========================================================================

    /**
     * 특정 기간 내 승인된 결제 수
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 결제 수
     */
    @Query("select count(p) from Payment p " +
            "where p.status = 'APPROVED' " +
            "and p.approvedAt >= :startTime " +
            "and p.approvedAt < :endTime")
    long countApprovedBetween(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 기간 내 총 결제 금액
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 총 결제 금액
     */
    @Query("select coalesce(sum(p.amount), 0) from Payment p " +
            "where p.status = 'APPROVED' " +
            "and p.approvedAt >= :startTime " +
            "and p.approvedAt < :endTime")
    long sumAmountBetween(@Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    /**
     * 현재 활성 프리미엄 회원 수
     *
     * @param now 현재 시간
     * @return 활성 프리미엄 회원 수
     */
    @Query("select count(distinct p.member.id) from Payment p " +
            "where p.status = 'APPROVED' " +
            "and p.premiumEndDate > :now")
    long countActivePremiumMembers(@Param("now") LocalDateTime now);

    // ========================================================================
    // 관리자용
    // ========================================================================

    /**
     * 전체 결제 내역 조회 (관리자용)
     *
     * @param pageable 페이지 정보
     * @return 결제 내역 목록
     */
    @Query("select p from Payment p " +
            "left join fetch p.member " +
            "order by p.createdAt desc")
    Page<Payment> findAllWithMember(Pageable pageable);

    /**
     * 상태별 결제 내역 조회 (관리자용)
     *
     * @param status 결제 상태
     * @param pageable 페이지 정보
     * @return 결제 내역 목록
     */
    @Query("select p from Payment p " +
            "left join fetch p.member " +
            "where p.status = :status " +
            "order by p.createdAt desc")
    Page<Payment> findByStatus(@Param("status") PaymentStatus status, Pageable pageable);
}
