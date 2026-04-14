package com.damoyeo.api.domain.payment.service;

import com.damoyeo.api.domain.payment.dto.KakaoPayReadyRequest;
import com.damoyeo.api.domain.payment.dto.KakaoPayReadyResponse;
import com.damoyeo.api.domain.payment.dto.PaymentDTO;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

import java.util.Map;

/**
 * 결제 서비스 인터페이스
 *
 * 카카오페이 결제 연동 및 프리미엄 회원 관리 비즈니스 로직을 정의합니다.
 *
 * [결제 흐름]
 * 1. ready() - 결제 준비 요청 → redirect URL 반환
 * 2. 사용자가 카카오페이 결제 진행
 * 3. approve() - 결제 승인 (pg_token으로)
 * 4. 프리미엄 회원 역할 부여
 *
 * [취소/실패]
 * - cancel() - 사용자가 결제 취소
 * - fail() - 결제 실패
 */
public interface PaymentService {

    // 카카오페이 결제 프로세스

    /**
     * 결제 준비 (카카오페이 ready API 호출)
     *
     * @param email 회원 이메일
     * @param request 결제 준비 요청 (paymentType)
     * @return 결제 준비 응답 (redirect URL 포함)
     */
    KakaoPayReadyResponse ready(String email, KakaoPayReadyRequest request);

    /**
     * 결제 승인 (카카오페이 approve API 호출)
     *
     * @param email 회원 이메일
     * @param pgToken 결제 승인 토큰 (카카오페이에서 전달)
     * @param orderId 주문번호
     * @return 결제 완료 정보
     */
    PaymentDTO approve(String email, String pgToken, String orderId);

    /**
     * 결제 취소 처리
     *
     * @param orderId 주문번호
     */
    void cancel(String orderId);

    /**
     * 결제 실패 처리
     *
     * @param orderId 주문번호
     */
    void fail(String orderId);

    // 결제 내역 조회

    /**
     * 내 결제 내역 조회
     *
     * @param email 회원 이메일
     * @param pageRequestDTO 페이지 요청 정보
     * @return 결제 내역 목록
     */
    PageResponseDTO<PaymentDTO> getMyPayments(String email, PageRequestDTO pageRequestDTO);

    /**
     * 결제 상세 조회
     *
     * @param email 회원 이메일
     * @param paymentId 결제 ID
     * @return 결제 상세 정보
     */
    PaymentDTO getPaymentDetail(String email, Long paymentId);

    // 프리미엄 상태 관리

    /**
     * 프리미엄 상태 확인
     *
     * @param email 회원 이메일
     * @return 프리미엄 상태 정보 (isPremium, endDate 등)
     */
    Map<String, Object> getPremiumStatus(String email);

    /**
     * 프리미엄 회원 여부 확인
     *
     * @param email 회원 이메일
     * @return true: 프리미엄 회원, false: 일반 회원
     */
    boolean isPremiumMember(String email);

    // 관리자 기능

    /**
     * 전체 결제 내역 조회 (관리자용)
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 결제 내역 목록
     */
    PageResponseDTO<PaymentDTO> getAllPayments(PageRequestDTO pageRequestDTO);

    /**
     * 결제 통계 조회 (관리자용)
     *
     * @return 결제 통계 (총 매출, 프리미엄 회원 수 등)
     */
    Map<String, Object> getPaymentStats();
}
