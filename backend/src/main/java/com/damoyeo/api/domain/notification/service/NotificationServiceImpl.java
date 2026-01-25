package com.damoyeo.api.domain.notification.service;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.notification.dto.NotificationDTO;
import com.damoyeo.api.domain.notification.entity.Notification;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.domain.notification.repository.NotificationRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import com.damoyeo.api.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 알림 서비스 구현체
 * ============================================================================
 *
 * [역할]
 * NotificationService 인터페이스의 실제 구현을 담당합니다.
 * 알림 관련 비즈니스 로직을 처리합니다.
 *
 * [주요 기능]
 * - 알림 발송 (다른 서비스에서 호출)
 * - 알림 목록 조회 (페이지네이션)
 * - 읽지 않은 알림 개수 조회
 * - 읽음 처리 (개별/전체)
 *
 * [트랜잭션 정책]
 * - 클래스 레벨 @Transactional: 모든 메서드에 트랜잭션 적용
 * - 조회 메서드: @Transactional(readOnly = true)로 성능 최적화
 *
 * [사용 위치]
 * - NotificationController에서 주입받아 사용
 * - GroupServiceImpl, MeetingServiceImpl 등에서 알림 발송 시 사용
 *
 * [Spring 어노테이션 설명]
 * - @Service: 서비스 계층 빈으로 등록
 * - @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * - @Transactional: 트랜잭션 관리
 * - @Slf4j: 로깅
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    // ========================================================================
    // 의존성 주입 (DI)
    // ========================================================================

    /** 알림 레포지토리 */
    private final NotificationRepository notificationRepository;

    /** 회원 레포지토리 (이메일로 회원 조회) */
    private final MemberRepository memberRepository;

    // ========================================================================
    // 알림 발송
    // ========================================================================

    /**
     * 알림 발송
     *
     * 새로운 알림을 생성하고 DB에 저장합니다.
     * 다른 서비스(GroupService, MeetingService 등)에서 이벤트 발생 시 호출합니다.
     *
     * [사용 예시]
     * // 가입 승인 시 (GroupServiceImpl)
     * notificationService.send(
     *     member,
     *     NotificationType.JOIN_APPROVED,
     *     "가입이 승인되었습니다",
     *     "강남 러닝 크루에 가입되었습니다.",
     *     groupId
     * );
     *
     * @param member 알림을 받을 회원
     * @param type 알림 유형
     * @param title 알림 제목
     * @param message 알림 내용
     * @param relatedId 관련 리소스 ID
     */
    @Override
    public void send(Member member, NotificationType type, String title, String message, Long relatedId) {
        // 알림 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .member(member)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build();

        notificationRepository.save(notification);
        log.info("Notification sent to {}: {}", member.getEmail(), title);

        // TODO: Phase 2에서 실시간 알림 추가 (WebSocket/SSE)
        // TODO: Phase 3에서 푸시 알림 추가 (Firebase/APNs)
    }

    // ========================================================================
    // 알림 조회
    // ========================================================================

    /**
     * 알림 목록 조회 (페이지네이션)
     *
     * 현재 사용자의 알림 목록을 최신순으로 조회합니다.
     *
     * @Transactional(readOnly = true): 조회 전용 트랜잭션
     * - 더티 체킹 비활성화로 성능 향상
     * - DB 복제본(slave) 사용 가능
     *
     * @param email 사용자 이메일
     * @param pageRequestDTO 페이지 정보 (page, size)
     * @return 페이지네이션된 알림 목록
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<NotificationDTO> getNotifications(String email, PageRequestDTO pageRequestDTO) {
        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 알림 목록 조회 (페이지네이션)
        Page<Notification> result = notificationRepository.findByMemberIdOrderByCreatedAtDesc(
                member.getId(),
                pageRequestDTO.getPageable("createdAt")  // createdAt으로 정렬
        );

        // 3. 엔티티 → DTO 변환
        List<NotificationDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        // 4. PageResponseDTO 빌드
        return PageResponseDTO.<NotificationDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) result.getTotalElements())
                .build();
    }

    /**
     * 읽지 않은 알림 개수 조회
     *
     * 현재 사용자의 읽지 않은 알림(isRead = false) 개수를 반환합니다.
     *
     * [프론트엔드 활용]
     * 헤더의 알림 벨 아이콘에 배지로 표시
     * 예: 🔔③
     *
     * @param email 사용자 이메일
     * @return 읽지 않은 알림 개수
     */
    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(String email) {
        Member member = getMemberByEmail(email);
        return notificationRepository.countUnread(member.getId());
    }

    // ========================================================================
    // 읽음 처리
    // ========================================================================

    /**
     * 개별 알림 읽음 처리
     *
     * 특정 알림을 읽음으로 표시합니다.
     *
     * [권한 확인]
     * 본인의 알림만 읽음 처리 가능합니다.
     * 다른 사람의 알림을 읽음 처리하려고 하면 403 에러를 반환합니다.
     *
     * @param notificationId 알림 ID
     * @param email 요청자 이메일 (권한 확인용)
     */
    @Override
    public void markAsRead(Long notificationId, String email) {
        // 1. 회원 조회
        Member member = getMemberByEmail(email);

        // 2. 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> CustomException.notFound("알림을 찾을 수 없습니다."));

        // 3. 권한 확인: 본인의 알림인지 확인
        if (!notification.getMember().getId().equals(member.getId())) {
            throw CustomException.forbidden("권한이 없습니다.");
        }

        // 4. 읽음 처리
        //    JPA 더티 체킹으로 자동 저장
        notification.markAsRead();
    }

    /**
     * 모든 알림 읽음 처리
     *
     * 현재 사용자의 모든 알림을 읽음으로 표시합니다.
     * 벌크 업데이트를 사용하여 효율적으로 처리합니다.
     *
     * @param email 사용자 이메일
     */
    @Override
    public void markAllAsRead(String email) {
        Member member = getMemberByEmail(email);
        // 벌크 업데이트로 한 번의 쿼리로 모든 알림을 읽음 처리
        notificationRepository.markAllAsRead(member.getId());
    }

    // ========================================================================
    // Helper 메서드 (private)
    // ========================================================================

    /**
     * 이메일로 회원 조회
     *
     * @param email 회원 이메일
     * @return 회원 엔티티
     * @throws CustomException 회원이 존재하지 않으면 404 에러
     */
    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));
    }

    /**
     * Notification 엔티티를 NotificationDTO로 변환
     *
     * @param notification 알림 엔티티
     * @return NotificationDTO
     */
    private NotificationDTO entityToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .content(notification.getMessage())
                .referenceId(notification.getRelatedId())
                .referenceType(getReferenceType(notification.getType()))
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * 알림 타입에 따른 참조 타입 결정
     *
     * @param type 알림 타입
     * @return 참조 타입 ("GROUP", "MEETING", "SYSTEM")
     */
    private String getReferenceType(NotificationType type) {
        return switch (type) {
            case NEW_MEETING, MEETING_REMINDER, MEETING_CANCELLED -> "MEETING";
            case WELCOME -> "SYSTEM";
            default -> "GROUP";
        };
    }
}
