package com.damoyeo.api.domain.admin.service;

import com.damoyeo.api.domain.admin.dto.AdminGroupDTO;
import com.damoyeo.api.domain.admin.dto.AdminMemberDTO;
import com.damoyeo.api.domain.admin.dto.DashboardStatsDTO;
import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.group.entity.GroupStatus;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.group.repository.GroupRepository;
import com.damoyeo.api.domain.meeting.repository.MeetingRepository;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.entity.MemberRole;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.payment.entity.Payment;
import com.damoyeo.api.domain.payment.entity.PaymentStatus;
import com.damoyeo.api.domain.payment.entity.PaymentType;
import com.damoyeo.api.domain.payment.repository.PaymentRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 관리자 서비스 구현체
 *
 * 관리자 전용 기능의 비즈니스 로직을 구현합니다.
 *
 * [트랜잭션 관리]
 * - 조회: readOnly = true (최적화)
 * - 수정: 기본 트랜잭션 (쓰기 가능)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingRepository meetingRepository;
    private final PaymentRepository paymentRepository;

    // 대시보드

    /**
     * 대시보드 통계 조회
     *
     * [통계 항목]
     * - 전체 회원 수
     * - 전체 모임 수 (ACTIVE)
     * - 전체 정모 수
     * - 오늘 신규 가입자 수
     * - 활성 모임 수
     * - 예정된 정모 수
     */
    @Override
    public DashboardStatsDTO getDashboardStats() {
        // 전체 회원 수
        long totalMembers = memberRepository.count();

        // 전체 모임 수 (ACTIVE 상태만)
        long totalGroups = groupRepository.count();

        // 전체 정모 수
        long totalMeetings = meetingRepository.count();

        // 오늘 신규 가입자 수
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(todayStart, todayEnd);

        // 활성 모임 수
        long activeGroups = groupRepository.countByStatus(GroupStatus.ACTIVE);

        // 예정된 정모 수 (현재 시간 이후)
        LocalDateTime now = LocalDateTime.now();
        long upcomingMeetings = meetingRepository.countUpcomingMeetings(now);

        return DashboardStatsDTO.builder()
                .totalMembers(totalMembers)
                .totalGroups(totalGroups)
                .totalMeetings(totalMeetings)
                .todayNewMembers(todayNewMembers)
                .activeGroups(activeGroups)
                .upcomingMeetings(upcomingMeetings)
                .build();
    }

    // 회원 관리

    /**
     * 회원 목록 조회 (페이지네이션 + 검색)
     *
     * [검색 조건]
     * - 이메일 또는 닉네임에 키워드 포함
     *
     * [정렬]
     * - 가입일 내림차순 (최신 가입 회원이 먼저)
     */
    @Override
    public PageResponseDTO<AdminMemberDTO> getMembers(String keyword, PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("createdAt").descending()
        );

        Page<Member> result;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색 (이메일 또는 닉네임)
            result = memberRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            // 전체 조회
            result = memberRepository.findAllWithRoles(pageable);
        }

        // Entity → DTO 변환
        List<AdminMemberDTO> dtoList = result.getContent().stream()
                .map(this::memberToAdminDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<AdminMemberDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 회원 역할 변경 (토글 방식)
     *
     * [처리 방식]
     * - 해당 역할이 이미 있으면 제거
     * - 해당 역할이 없으면 추가
     *
     * [주의]
     * - USER 역할은 항상 유지 (기본 권한)
     */
    @Override
    @Transactional
    public void updateMemberRole(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        MemberRole targetRole = MemberRole.valueOf(role);

        if (member.getMemberRoleList().contains(targetRole)) {
            // 역할 제거
            member.getMemberRoleList().remove(targetRole);
            log.info("회원 역할 제거: memberId={}, role={}", memberId, role);
        } else {
            // 역할 추가
            member.addRole(targetRole);
            log.info("회원 역할 추가: memberId={}, role={}", memberId, role);
        }
    }

    // 모임 관리

    /**
     * 모임 목록 조회 (페이지네이션 + 검색 + 상태 필터)
     *
     * [검색 조건]
     * - 모임 이름에 키워드 포함
     *
     * [필터]
     * - 상태: ACTIVE, INACTIVE, DELETED
     *
     * [정렬]
     * - 생성일 내림차순 (최근 생성된 모임이 먼저)
     */
    @Override
    public PageResponseDTO<AdminGroupDTO> getGroups(String keyword, String status, PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                Sort.by("createdAt").descending()
        );

        Page<Group> result;

        // 상태 필터 파싱
        GroupStatus groupStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                groupStatus = GroupStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상태값: {}", status);
            }
        }

        // 검색 조건에 따라 분기
        if (keyword != null && !keyword.trim().isEmpty() && groupStatus != null) {
            // 키워드 + 상태 필터
            result = groupRepository.searchByKeywordAndStatus(keyword.trim(), groupStatus, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드만
            result = groupRepository.searchByKeywordAdmin(keyword.trim(), pageable);
        } else if (groupStatus != null) {
            // 상태만
            result = groupRepository.findAllByStatus(groupStatus, pageable);
        } else {
            // 전체 조회
            result = groupRepository.findAllAdmin(pageable);
        }

        // Entity → DTO 변환
        List<AdminGroupDTO> dtoList = result.getContent().stream()
                .map(this::groupToAdminDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<AdminGroupDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 모임 상태 변경
     *
     * [가능한 상태]
     * - ACTIVE: 활성
     * - INACTIVE: 비활성
     * - DELETED: 삭제됨 (소프트 삭제)
     */
    @Override
    @Transactional
    public void updateGroupStatus(Long groupId, String status) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("모임을 찾을 수 없습니다: " + groupId));

        GroupStatus newStatus = GroupStatus.valueOf(status);
        group.changeStatus(newStatus);

        log.info("모임 상태 변경: groupId={}, status={}", groupId, status);
    }

    // Private Helper Methods

    /**
     * Member 엔티티 → AdminMemberDTO 변환
     *
     * [프리미엄 정보 포함]
     * - 활성 프리미엄 결제 내역 조회
     * - 시작일, 종료일, 남은 일수 계산
     */
    private AdminMemberDTO memberToAdminDTO(Member member) {
        // 회원이 가입한 모임 수 조회
        int groupCount = groupMemberRepository.countByMemberId(member.getId());

        // 프리미엄 정보 조회
        LocalDateTime now = LocalDateTime.now();
        log.info("프리미엄 조회: memberId={}, email={}, now={}", member.getId(), member.getEmail(), now);

        Optional<Payment> activePremium = paymentRepository.findLatestActivePremium(
                member.getId(), now);

        log.info("프리미엄 조회 결과: memberId={}, found={}", member.getId(), activePremium.isPresent());
        if (activePremium.isPresent()) {
            Payment p = activePremium.get();
            log.info("프리미엄 상세: paymentId={}, endDate={}, status={}",
                    p.getId(), p.getPremiumEndDate(), p.getStatus());
        }

        // 역할 목록 구성:
        // PREMIUM 역할은 활성 프리미엄 결제가 존재할 때만 포함시킵니다.
        // 결제가 자연만료되어도 memberRoleList에는 PREMIUM이 남아 있을 수 있으므로 필터링합니다.
        List<String> roleNames = member.getMemberRoleList().stream()
                .filter(role -> role != MemberRole.PREMIUM || activePremium.isPresent())
                .map(MemberRole::name)
                .collect(Collectors.toList());

        AdminMemberDTO.AdminMemberDTOBuilder builder = AdminMemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .roleNames(roleNames)
                .social(member.isSocial())
                .createdAt(member.getCreatedAt())
                .groupCount(groupCount);

        // 프리미엄 정보 추가
        if (activePremium.isPresent()) {
            Payment payment = activePremium.get();
            long daysRemaining = Duration.between(
                    LocalDateTime.now(),
                    payment.getPremiumEndDate()
            ).toDays();

            builder.isPremium(true)
                    .premiumType(payment.getPaymentType().name())
                    .premiumStartDate(payment.getPremiumStartDate())
                    .premiumEndDate(payment.getPremiumEndDate())
                    .premiumDaysRemaining(Math.max(0, daysRemaining));
        } else {
            builder.isPremium(false)
                    .premiumDaysRemaining(0);
        }

        return builder.build();
    }

    /**
     * Group 엔티티 → AdminGroupDTO 변환
     */
    private AdminGroupDTO groupToAdminDTO(Group group) {
        // 모임의 현재 멤버 수 조회
        int memberCount = groupMemberRepository.countApprovedMembers(group.getId());

        return AdminGroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .categoryName(group.getCategory() != null ? group.getCategory().getName() : "미분류")
                .ownerNickname(group.getOwner().getNickname())
                .ownerEmail(group.getOwner().getEmail())
                .memberCount(memberCount)
                .maxMembers(group.getMaxMembers())
                .status(group.getStatus().name())
                .isPublic(group.isPublic())
                .createdAt(group.getCreatedAt())
                .build();
    }

    // 프리미엄 관리

    /**
     * 회원에게 프리미엄 부여 (일수 지정)
     *
     * [처리]
     * 1. 새로운 Payment 엔티티 생성 (APPROVED 상태)
     * 2. 회원에게 PREMIUM 역할 부여
     */
    @Override
    @Transactional
    public void grantPremium(Long memberId, int days) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        // 기존 활성 프리미엄이 있는지 확인
        Optional<Payment> existingPremium = paymentRepository.findLatestActivePremium(
                memberId, LocalDateTime.now());

        LocalDateTime startDate;
        LocalDateTime endDate;

        if (existingPremium.isPresent()) {
            // 기존 프리미엄 연장
            startDate = existingPremium.get().getPremiumStartDate();
            endDate = existingPremium.get().getPremiumEndDate().plusDays(days);
            existingPremium.get().extendPremium(endDate);
            log.info("프리미엄 연장: memberId={}, 추가일수={}, newEndDate={}", memberId, days, endDate);
        } else {
            // 새 프리미엄 부여
            startDate = LocalDateTime.now();
            endDate = startDate.plusDays(days);

            String orderId = "ADMIN_GRANT_" + memberId + "_" + System.currentTimeMillis();

            Payment payment = Payment.builder()
                    .member(member)
                    .paymentType(days >= 365 ? PaymentType.PREMIUM_YEARLY : PaymentType.PREMIUM_MONTHLY)
                    .status(PaymentStatus.APPROVED)
                    .amount(0) // 관리자 부여 (무료)
                    .orderId(orderId)
                    .build();

            // 직접 프리미엄 기간 설정
            payment.approve("ADMIN_GRANT", LocalDateTime.now());
            payment.setPremiumDates(startDate, endDate);

            paymentRepository.save(payment);
            log.info("프리미엄 부여: memberId={}, days={}, endDate={}", memberId, days, endDate);
        }

        // PREMIUM 역할 추가 (없으면)
        if (!member.getMemberRoleList().contains(MemberRole.PREMIUM)) {
            member.addRole(MemberRole.PREMIUM);
        }
    }

    /**
     * 프리미엄 기간 연장/감소
     *
     * @param memberId 대상 회원 ID
     * @param days 추가/감소할 일수 (음수면 감소)
     */
    @Override
    @Transactional
    public void adjustPremiumDays(Long memberId, int days) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        Optional<Payment> activePremium = paymentRepository.findLatestActivePremium(
                memberId, LocalDateTime.now());

        if (activePremium.isEmpty()) {
            throw new IllegalStateException("활성화된 프리미엄이 없습니다.");
        }

        Payment payment = activePremium.get();
        LocalDateTime currentEndDate = payment.getPremiumEndDate();
        LocalDateTime newEndDate = currentEndDate.plusDays(days);

        // 종료일이 현재보다 이전이면 프리미엄 해제
        if (newEndDate.isBefore(LocalDateTime.now())) {
            revokePremium(memberId);
            return;
        }

        payment.extendPremium(newEndDate);
        log.info("프리미엄 기간 조정: memberId={}, days={}, newEndDate={}", memberId, days, newEndDate);
    }

    /**
     * 프리미엄 즉시 해제
     *
     * [처리]
     * 1. 활성 프리미엄 결제의 종료일을 현재 시간으로 설정
     * 2. 회원에서 PREMIUM 역할 제거
     */
    @Override
    @Transactional
    public void revokePremium(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));

        Optional<Payment> activePremium = paymentRepository.findLatestActivePremium(
                memberId, LocalDateTime.now());

        if (activePremium.isPresent()) {
            // 종료일을 현재로 설정하여 만료 처리
            Payment payment = activePremium.get();
            payment.extendPremium(LocalDateTime.now().minusSeconds(1));
            log.info("프리미엄 해제 (결제 만료 처리): memberId={}", memberId);
        }

        // PREMIUM 역할 제거
        if (member.getMemberRoleList().contains(MemberRole.PREMIUM)) {
            member.getMemberRoleList().remove(MemberRole.PREMIUM);
            log.info("프리미엄 역할 제거: memberId={}", memberId);
        }
    }
}
