package com.damoyeo.api.domain.payment.controller;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.payment.dto.KakaoPayReadyRequest;
import com.damoyeo.api.domain.payment.dto.KakaoPayReadyResponse;
import com.damoyeo.api.domain.payment.dto.PaymentDTO;
import com.damoyeo.api.domain.payment.service.PaymentService;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 결제 컨트롤러
 *
 * 카카오페이 결제 관련 API를 제공합니다.
 *
 * [API 목록]
 * - POST /api/payments/ready - 결제 준비
 * - GET /api/payments/approve - 결제 승인 (카카오페이 콜백)
 * - GET /api/payments/cancel - 결제 취소 (카카오페이 콜백)
 * - GET /api/payments/fail - 결제 실패 (카카오페이 콜백)
 * - GET /api/payments - 내 결제 내역 조회
 * - GET /api/payments/{id} - 결제 상세 조회
 * - GET /api/payments/premium-status - 프리미엄 상태 확인
 *
 * [관리자 API]
 * - GET /api/payments/admin - 전체 결제 내역 조회
 * - GET /api/payments/admin/stats - 결제 통계 조회
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    // 카카오페이 결제 프로세스

    /**
     * 결제 준비
     *
     * [프로세스]
     * 1. 프론트엔드에서 결제 유형(월간/연간) 선택
     * 2. 이 API 호출
     * 3. 카카오페이 redirect URL 반환
     * 4. 프론트엔드에서 해당 URL로 이동
     *
     * @param memberDTO 인증된 회원 정보
     * @param request 결제 준비 요청 (paymentType)
     * @return 카카오페이 결제 페이지 URL
     */
    @Operation(summary = "결제 준비", description = "카카오페이 결제를 준비하고 redirect URL을 반환합니다.")
    @PostMapping("/ready")
    public ResponseEntity<KakaoPayReadyResponse> ready(
            @AuthenticationPrincipal MemberDTO memberDTO,
            @RequestBody KakaoPayReadyRequest request) {

        log.info("결제 준비 요청: email={}, paymentType={}", memberDTO.getEmail(), request.getPaymentType());

        KakaoPayReadyResponse response = paymentService.ready(memberDTO.getEmail(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 승인 (카카오페이 콜백)
     *
     * [프로세스]
     * 1. 사용자가 카카오페이에서 결제 완료
     * 2. 카카오페이가 이 URL로 redirect (pg_token 포함)
     * 3. 결제 승인 처리
     * 4. 프리미엄 역할 부여
     *
     * @param memberDTO 인증된 회원 정보
     * @param pgToken 카카오페이 결제 승인 토큰
     * @param orderId 주문번호
     * @return 결제 완료 정보
     */
    @Operation(summary = "결제 승인", description = "카카오페이 결제를 승인합니다.")
    @GetMapping("/approve")
    public ResponseEntity<PaymentDTO> approve(
            @AuthenticationPrincipal MemberDTO memberDTO,
            @Parameter(description = "카카오페이 결제 승인 토큰") @RequestParam("pg_token") String pgToken,
            @Parameter(description = "주문번호") @RequestParam("order_id") String orderId) {

        log.info("결제 승인 요청: email={}, orderId={}", memberDTO.getEmail(), orderId);

        PaymentDTO result = paymentService.approve(memberDTO.getEmail(), pgToken, orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 결제 취소 (카카오페이 콜백)
     *
     * 사용자가 카카오페이 결제창에서 취소 버튼을 클릭한 경우
     *
     * @param orderId 주문번호
     * @return 취소 결과
     */
    @Operation(summary = "결제 취소", description = "결제 취소를 처리합니다.")
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancel(
            @Parameter(description = "주문번호") @RequestParam("order_id") String orderId) {

        log.info("결제 취소 요청: orderId={}", orderId);

        paymentService.cancel(orderId);
        return ResponseEntity.ok(Map.of(
                "status", "cancelled",
                "message", "결제가 취소되었습니다."
        ));
    }

    /**
     * 결제 실패 (카카오페이 콜백)
     *
     * 결제 과정에서 오류가 발생한 경우
     *
     * @param orderId 주문번호
     * @return 실패 결과
     */
    @Operation(summary = "결제 실패", description = "결제 실패를 처리합니다.")
    @GetMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(
            @Parameter(description = "주문번호") @RequestParam("order_id") String orderId) {

        log.info("결제 실패 요청: orderId={}", orderId);

        paymentService.fail(orderId);
        return ResponseEntity.ok(Map.of(
                "status", "failed",
                "message", "결제가 실패했습니다."
        ));
    }

    // 결제 내역 조회

    /**
     * 내 결제 내역 조회
     *
     * @param memberDTO 인증된 회원 정보
     * @param pageRequestDTO 페이지 요청 정보
     * @return 결제 내역 목록
     */
    @Operation(summary = "내 결제 내역 조회", description = "로그인한 회원의 결제 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponseDTO<PaymentDTO>> getMyPayments(
            @AuthenticationPrincipal MemberDTO memberDTO,
            PageRequestDTO pageRequestDTO) {

        PageResponseDTO<PaymentDTO> result = paymentService.getMyPayments(
                memberDTO.getEmail(), pageRequestDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 결제 상세 조회
     *
     * @param memberDTO 인증된 회원 정보
     * @param paymentId 결제 ID
     * @return 결제 상세 정보
     */
    @Operation(summary = "결제 상세 조회", description = "결제 상세 정보를 조회합니다.")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentDetail(
            @AuthenticationPrincipal MemberDTO memberDTO,
            @PathVariable Long paymentId) {

        PaymentDTO result = paymentService.getPaymentDetail(memberDTO.getEmail(), paymentId);
        return ResponseEntity.ok(result);
    }

    // 프리미엄 상태 관리

    /**
     * 프리미엄 상태 확인
     *
     * @param memberDTO 인증된 회원 정보
     * @return 프리미엄 상태 정보
     */
    @Operation(summary = "프리미엄 상태 확인", description = "현재 프리미엄 구독 상태를 확인합니다.")
    @GetMapping("/premium-status")
    public ResponseEntity<Map<String, Object>> getPremiumStatus(
            @AuthenticationPrincipal MemberDTO memberDTO) {

        Map<String, Object> status = paymentService.getPremiumStatus(memberDTO.getEmail());
        return ResponseEntity.ok(status);
    }

    // 관리자 API

    /**
     * 전체 결제 내역 조회 (관리자용)
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 결제 내역 목록
     */
    @Operation(summary = "전체 결제 내역 조회 (관리자)", description = "모든 결제 내역을 조회합니다.")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDTO<PaymentDTO>> getAllPayments(PageRequestDTO pageRequestDTO) {
        PageResponseDTO<PaymentDTO> result = paymentService.getAllPayments(pageRequestDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 결제 통계 조회 (관리자용)
     *
     * @return 결제 통계
     */
    @Operation(summary = "결제 통계 조회 (관리자)", description = "결제 관련 통계를 조회합니다.")
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPaymentStats() {
        Map<String, Object> stats = paymentService.getPaymentStats();
        return ResponseEntity.ok(stats);
    }
}
