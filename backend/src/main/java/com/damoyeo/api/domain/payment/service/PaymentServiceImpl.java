package com.damoyeo.api.domain.payment.service;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.payment.dto.*;
import com.damoyeo.api.domain.payment.entity.Payment;
import com.damoyeo.api.domain.payment.entity.PaymentStatus;
import com.damoyeo.api.domain.payment.entity.PaymentType;
import com.damoyeo.api.domain.payment.repository.PaymentRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import com.damoyeo.api.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 결제 서비스 구현체
 * ============================================================================
 *
 * [역할]
 * 카카오페이 결제 연동 및 프리미엄 회원 관리 비즈니스 로직을 구현합니다.
 *
 * [카카오페이 API 흐름]
 * 1. /v1/payment/ready - 결제 준비
 * 2. 사용자 결제 진행 (redirect)
 * 3. /v1/payment/approve - 결제 승인
 *
 * [환경 설정]
 * application.properties에 다음 설정 필요:
 * - kakao.pay.admin-key: 카카오페이 Admin Key
 * - kakao.pay.cid: 가맹점 코드 (테스트: TC0ONETIME)
 * - kakao.pay.ready-url: 결제 준비 API URL
 * - kakao.pay.approve-url: 결제 승인 API URL
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate;

    /**
     * 카카오페이 Admin Key (DEV Key)
     */
    @Value("${kakao.pay.secret-key:}")
    private String kakaoPaySecretKey;

    /**
     * 카맹점 코드 (테스트: TC0ONETIME)
     */
    @Value("${kakao.pay.cid:TC0ONETIME}")
    private String cid;

    /**
     * 결제 준비 API URL
     */
    @Value("${kakao.pay.ready-url:https://open-api.kakaopay.com/online/v1/payment/ready}")
    private String readyUrl;

    /**
     * 결제 승인 API URL
     */
    @Value("${kakao.pay.approve-url:https://open-api.kakaopay.com/online/v1/payment/approve}")
    private String approveUrl;

    /**
     * 프론트엔드 URL (콜백용)
     */
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * 테스트 모드 여부
     * true: 실제 카카오페이 API 호출 없이 바로 성공 페이지로 이동
     */
    @Value("${kakao.pay.test-mode:false}")
    private boolean testMode;

    // ========================================================================
    // 카카오페이 결제 프로세스
    // ========================================================================

    /**
     * 결제 준비 (카카오페이 ready API 호출)
     *
     * [처리 흐름]
     * 1. 회원 조회
     * 2. 주문번호 생성
     * 3. Payment 엔티티 생성 (READY 상태)
     * 4. 카카오페이 ready API 호출 (또는 테스트 모드)
     * 5. tid 저장 및 redirect URL 반환
     */
    @Override
    @Transactional
    public KakaoPayReadyResponse ready(String email, KakaoPayReadyRequest request) {
        log.info("결제 준비 시작: email={}, paymentType={}, testMode={}", email, request.getPaymentType(), testMode);

        // 1. 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + email));

        // 2. 이미 활성 프리미엄이 있는지 확인
        Optional<Payment> activePremium = paymentRepository.findLatestActivePremium(
                member.getId(), LocalDateTime.now());
        if (activePremium.isPresent()) {
            throw new CustomException("이미 활성화된 프리미엄 구독이 있습니다. " +
                    "만료일: " + activePremium.get().getPremiumEndDate().toLocalDate(),
                    HttpStatus.BAD_REQUEST);
        }

        PaymentType paymentType = request.getPaymentType();

        // 3. 주문번호 생성 (PAY_{memberId}_{timestamp})
        String orderId = "PAY_" + member.getId() + "_" + System.currentTimeMillis();

        // 4. Payment 엔티티 생성
        Payment payment = Payment.builder()
                .member(member)
                .paymentType(paymentType)
                .status(PaymentStatus.READY)
                .amount(paymentType.getPrice())
                .orderId(orderId)
                .build();

        paymentRepository.save(payment);

        // 5. 테스트 모드: 카카오페이 API 호출 없이 바로 성공 URL 반환
        if (testMode) {
            log.info("테스트 모드: 카카오페이 API 호출 생략, orderId={}", orderId);
            String testTid = "TEST_TID_" + System.currentTimeMillis();
            payment.ready(testTid);

            KakaoPayReadyResponse testResponse = KakaoPayReadyResponse.builder()
                    .tid(testTid)
                    .nextRedirectPcUrl(frontendUrl + "/payment/success?order_id=" + orderId + "&pg_token=test_token")
                    .nextRedirectMobileUrl(frontendUrl + "/payment/success?order_id=" + orderId + "&pg_token=test_token")
                    .nextRedirectAppUrl(frontendUrl + "/payment/success?order_id=" + orderId + "&pg_token=test_token")
                    .createdAt(LocalDateTime.now().toString())
                    .build();

            return testResponse;
        }

        // 6. 실제 카카오페이 ready API 호출
        HttpHeaders headers = createKakaoPayHeaders();

        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        params.put("partner_order_id", orderId);
        params.put("partner_user_id", email);
        params.put("item_name", paymentType.getDisplayName());
        params.put("quantity", "1");
        params.put("total_amount", String.valueOf(paymentType.getPrice()));
        params.put("tax_free_amount", "0");
        params.put("approval_url", frontendUrl + "/payment/success?order_id=" + orderId);
        params.put("cancel_url", frontendUrl + "/payment/cancel?order_id=" + orderId);
        params.put("fail_url", frontendUrl + "/payment/fail?order_id=" + orderId);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoPayReadyResponse> response = restTemplate.exchange(
                    readyUrl,
                    HttpMethod.POST,
                    entity,
                    KakaoPayReadyResponse.class
            );

            KakaoPayReadyResponse readyResponse = response.getBody();

            if (readyResponse != null && readyResponse.getTid() != null) {
                // tid 저장
                payment.ready(readyResponse.getTid());
                log.info("결제 준비 완료: orderId={}, tid={}", orderId, readyResponse.getTid());
            }

            return readyResponse;

        } catch (Exception e) {
            log.error("카카오페이 결제 준비 실패: {}", e.getMessage());
            payment.fail();
            throw new CustomException("결제 준비에 실패했습니다: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 결제 승인 (카카오페이 approve API 호출)
     *
     * [처리 흐름]
     * 1. 주문번호로 Payment 조회
     * 2. 카카오페이 approve API 호출 (또는 테스트 모드)
     * 3. Payment 상태 업데이트 (APPROVED)
     * 4. 회원에게 PREMIUM 역할 부여
     */
    @Override
    @Transactional
    public PaymentDTO approve(String email, String pgToken, String orderId) {
        log.info("결제 승인 시작: email={}, orderId={}, testMode={}", email, orderId, testMode);

        // 1. 주문번호로 Payment 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + orderId));

        // 본인 결제인지 확인
        if (!payment.getMember().getEmail().equals(email)) {
            throw new CustomException("본인의 결제만 승인할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 이미 승인된 결제인 경우 기존 결제 정보 반환 (멱등성 보장)
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            log.info("이미 승인된 결제 반환: orderId={}", orderId);
            return toPaymentDTO(payment);
        }

        // 2. 테스트 모드: 카카오페이 API 호출 없이 바로 승인 처리
        if (testMode) {
            log.info("테스트 모드: 카카오페이 approve API 호출 생략, orderId={}", orderId);

            // Payment 상태 업데이트
            payment.approve("TEST_PAYMENT", LocalDateTime.now());

            // 회원에게 PREMIUM 역할 부여
            Member member = payment.getMember();
            if (!member.getMemberRoleList().contains(MemberRole.PREMIUM)) {
                member.addRole(MemberRole.PREMIUM);
                log.info("프리미엄 역할 부여 (테스트 모드): memberId={}", member.getId());
            }

            log.info("결제 승인 완료 (테스트 모드): orderId={}, amount={}", orderId, payment.getAmount());
            return toPaymentDTO(payment);
        }

        // 3. 실제 카카오페이 approve API 호출
        HttpHeaders headers = createKakaoPayHeaders();

        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", payment.getTid());
        params.put("partner_order_id", orderId);
        params.put("partner_user_id", email);
        params.put("pg_token", pgToken);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoPayApproveResponse> response = restTemplate.exchange(
                    approveUrl,
                    HttpMethod.POST,
                    entity,
                    KakaoPayApproveResponse.class
            );

            KakaoPayApproveResponse approveResponse = response.getBody();

            if (approveResponse != null) {
                // Payment 상태 업데이트
                LocalDateTime approvedAt = parseDateTime(approveResponse.getApprovedAt());
                payment.approve(approveResponse.getPaymentMethodType(), approvedAt);

                // 회원에게 PREMIUM 역할 부여
                Member member = payment.getMember();
                if (!member.getMemberRoleList().contains(MemberRole.PREMIUM)) {
                    member.addRole(MemberRole.PREMIUM);
                    log.info("프리미엄 역할 부여: memberId={}", member.getId());
                }

                log.info("결제 승인 완료: orderId={}, amount={}", orderId, payment.getAmount());
            }

            return toPaymentDTO(payment);

        } catch (Exception e) {
            log.error("카카오페이 결제 승인 실패: {}", e.getMessage());
            payment.fail();
            throw new CustomException("결제 승인에 실패했습니다: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 결제 취소 처리
     */
    @Override
    @Transactional
    public void cancel(String orderId) {
        log.info("결제 취소 처리: orderId={}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + orderId));

        payment.cancel();
        log.info("결제 취소 완료: orderId={}", orderId);
    }

    /**
     * 결제 실패 처리
     */
    @Override
    @Transactional
    public void fail(String orderId) {
        log.info("결제 실패 처리: orderId={}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + orderId));

        payment.fail();
        log.info("결제 실패 처리 완료: orderId={}", orderId);
    }

    // ========================================================================
    // 결제 내역 조회
    // ========================================================================

    /**
     * 내 결제 내역 조회
     */
    @Override
    public PageResponseDTO<PaymentDTO> getMyPayments(String email, PageRequestDTO pageRequestDTO) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + email));

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize()
        );

        Page<Payment> result = paymentRepository.findByMemberId(member.getId(), pageable);

        List<PaymentDTO> dtoList = result.getContent().stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<PaymentDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 결제 상세 조회
     */
    @Override
    public PaymentDTO getPaymentDetail(String email, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + paymentId));

        // 본인 결제인지 확인
        if (!payment.getMember().getEmail().equals(email)) {
            throw new CustomException("본인의 결제만 조회할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        return toPaymentDTO(payment);
    }

    // ========================================================================
    // 프리미엄 상태 관리
    // ========================================================================

    /**
     * 프리미엄 상태 확인
     */
    @Override
    public Map<String, Object> getPremiumStatus(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + email));

        Map<String, Object> status = new HashMap<>();

        Optional<Payment> activePremium = paymentRepository.findLatestActivePremium(
                member.getId(), LocalDateTime.now());

        if (activePremium.isPresent()) {
            Payment payment = activePremium.get();
            status.put("isPremium", true);
            status.put("premiumType", payment.getPaymentType().name());
            status.put("premiumTypeName", payment.getPaymentType().getDisplayName());
            status.put("startDate", payment.getPremiumStartDate());
            status.put("endDate", payment.getPremiumEndDate());
            status.put("daysRemaining", calculateDaysRemaining(payment.getPremiumEndDate()));
        } else {
            status.put("isPremium", false);
            status.put("premiumType", null);
            status.put("startDate", null);
            status.put("endDate", null);
            status.put("daysRemaining", 0);
        }

        return status;
    }

    /**
     * 프리미엄 회원 여부 확인
     */
    @Override
    public boolean isPremiumMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + email));

        return paymentRepository.findLatestActivePremium(
                member.getId(), LocalDateTime.now()).isPresent();
    }

    // ========================================================================
    // 관리자 기능
    // ========================================================================

    /**
     * 전체 결제 내역 조회 (관리자용)
     */
    @Override
    public PageResponseDTO<PaymentDTO> getAllPayments(PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize()
        );

        Page<Payment> result = paymentRepository.findAllWithMember(pageable);

        List<PaymentDTO> dtoList = result.getContent().stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<PaymentDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 결제 통계 조회 (관리자용)
     */
    @Override
    public Map<String, Object> getPaymentStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 오늘 결제 건수/금액
        stats.put("todayCount", paymentRepository.countApprovedBetween(todayStart, now));
        stats.put("todayAmount", paymentRepository.sumAmountBetween(todayStart, now));

        // 이번 달 결제 건수/금액
        stats.put("monthCount", paymentRepository.countApprovedBetween(monthStart, now));
        stats.put("monthAmount", paymentRepository.sumAmountBetween(monthStart, now));

        // 현재 활성 프리미엄 회원 수
        stats.put("activePremiumMembers", paymentRepository.countActivePremiumMembers(now));

        // 전체 결제 건수
        stats.put("totalPayments", paymentRepository.count());

        return stats;
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    /**
     * 카카오페이 API 요청 헤더 생성
     */
    private HttpHeaders createKakaoPayHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + kakaoPaySecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Payment 엔티티 → DTO 변환
     */
    private PaymentDTO toPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentType(payment.getPaymentType())
                .paymentTypeName(payment.getPaymentType().getDisplayName())
                .status(payment.getStatus())
                .statusName(PaymentDTO.getStatusDisplayName(payment.getStatus()))
                .amount(payment.getAmount())
                .paymentMethod(PaymentDTO.getPaymentMethodDisplayName(payment.getPaymentMethodType()))
                .orderId(payment.getOrderId())
                .premiumStartDate(payment.getPremiumStartDate())
                .premiumEndDate(payment.getPremiumEndDate())
                .premiumActive(payment.isPremiumActive())
                .approvedAt(payment.getApprovedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * 카카오페이 날짜 문자열 파싱
     * 형식: 2024-01-15T10:30:00
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 남은 일수 계산
     */
    private long calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) {
            return 0;
        }
        return java.time.Duration.between(now, endDate).toDays();
    }
}
