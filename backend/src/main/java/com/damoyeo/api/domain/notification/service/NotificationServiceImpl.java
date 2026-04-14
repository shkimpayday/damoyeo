package com.damoyeo.api.domain.notification.service;

import com.damoyeo.api.domain.meeting.entity.AttendStatus;
import com.damoyeo.api.domain.meeting.entity.Meeting;
import com.damoyeo.api.domain.meeting.entity.MeetingAttendee;
import com.damoyeo.api.domain.meeting.repository.MeetingAttendeeRepository;
import com.damoyeo.api.domain.meeting.repository.MeetingRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현체
 *
 * NotificationService 인터페이스의 실제 구현을 담당합니다.
 * 알림 관련 비즈니스 로직을 처리합니다.
 *
 * - 알림 발송 (다른 서비스에서 호출)
 * - 알림 목록 조회 (페이지네이션)
 * - 읽지 않은 알림 개수 조회
 * - 읽음 처리 (개별/전체)
 *
 * - 클래스 레벨 @Transactional: 모든 메서드에 트랜잭션 적용
 * - 조회 메서드: @Transactional(readOnly = true)로 성능 최적화
 *
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

    // 의존성 주입 (DI)

    /** 알림 레포지토리 */
    private final NotificationRepository notificationRepository;

    /** 회원 레포지토리 (이메일로 회원 조회) */
    private final MemberRepository memberRepository;

    /** 정모 레포지토리 (리마인더 대상 정모 조회) */
    private final MeetingRepository meetingRepository;

    /** 정모 참석자 레포지토리 (참석 예정 정모 조회) */
    private final MeetingAttendeeRepository meetingAttendeeRepository;

    /** 날짜 포맷터 (리마인더 메시지용) */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 알림 발송

    /**
     * 알림 발송
     *
     * 새로운 알림을 생성하고 DB에 저장합니다.
     * 다른 서비스(GroupService, MeetingService 등)에서 이벤트 발생 시 호출합니다.
     *
     * // 새 멤버 가입 시 (GroupServiceImpl)
     * notificationService.send(
     *     owner,
     *     NotificationType.NEW_MEMBER,
     *     "새 멤버 가입",
     *     "홍길동님이 강남 러닝 크루에 가입했습니다.",
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
    }

    // 알림 조회

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
    @Transactional
    public PageResponseDTO<NotificationDTO> getNotifications(String email, PageRequestDTO pageRequestDTO) {
        Member member = getMemberByEmail(email);

        generateMeetingReminders(member);

        Page<Notification> result = notificationRepository.findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(
                member.getId(),
                pageRequestDTO.getPageable("createdAt")  // createdAt으로 정렬
        );

        List<NotificationDTO> dtoList = result.getContent().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<NotificationDTO>builder()
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

    // 읽음 처리

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
        Member member = getMemberByEmail(email);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> CustomException.notFound("알림을 찾을 수 없습니다."));

        if (!notification.getMember().getId().equals(member.getId())) {
            throw CustomException.forbidden("권한이 없습니다.");
        }

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

    @Override
    public void delete(Long notificationId, String email) {
        Member member = getMemberByEmail(email);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> CustomException.notFound("알림을 찾을 수 없습니다."));

        if(!notification.getMember().getId().equals(member.getId())) {
            throw CustomException.forbidden("권한이 없습니다");
        }

        notification.delete();

    }

    // Helper 메서드 (private)

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
     * [참조 타입 분류]
     * - GROUP: 모임 관련 알림 (새 멤버, 멤버 탈퇴, 역할 변경, 강퇴, 모임 해체)
     * - MEETING: 정모 관련 알림 (새 정모, 리마인더, 취소)
     * - SYSTEM: 시스템 알림 (회원가입 환영)
     *
     * @param type 알림 타입
     * @return 참조 타입 ("GROUP", "MEETING", "SYSTEM")
     */
    private String getReferenceType(NotificationType type) {
        return switch (type) {
            case NEW_MEETING, MEETING_REMINDER, MEETING_CANCELLED, MEETING_UPDATED, MEETING_IMMINENT -> "MEETING";
            case WELCOME -> "SYSTEM";
            case NEW_MEMBER, MEMBER_LEFT, ROLE_CHANGED, GROUP_UPDATE, MEMBER_KICKED, GROUP_DISBANDED -> "GROUP";
        };
    }

    // 정모 리마인더 동적 생성 (조회 시점 생성 방식)

    /**
     * 정모 리마인더 알림 동적 생성
     *
     * 알림 목록 조회 시점에 필요한 리마인더 알림을 자동으로 생성합니다.
     * 이미 생성된 리마인더는 중복 생성되지 않습니다.
     *
     * [리마인더 종류]
     * 1. D-1 리마인더: 내일 진행되는 정모 (오늘 00:00 ~ 내일 23:59:59)
     * 2. 3시간 전 리마인더: 3시간 이내에 시작하는 정모
     *
     * [동작 방식]
     * 1. 회원의 참석 예정(ATTENDING) 정모 목록 조회
     * 2. 각 정모에 대해 리마인더 조건 확인
     * 3. 해당 리마인더가 없으면 DB에 생성
     *
     * @param member 회원 엔티티
     */
    private void generateMeetingReminders(Member member) {
        LocalDateTime now = LocalDateTime.now();

        // 내가 참석 예정인 미래 정모 목록 조회
        List<Meeting> myUpcomingMeetings = meetingRepository.findMyUpcomingMeetings(member.getId(), now);

        for (Meeting meeting : myUpcomingMeetings) {
            LocalDateTime meetingDate = meeting.getMeetingDate();

            if (isWithinHours(meetingDate, now, 24) && !isWithinHours(meetingDate, now, 3)) {
                createReminderIfNotExists(member, meeting, NotificationType.MEETING_REMINDER);
            }

            if (isWithinHours(meetingDate, now, 3)) {
                createReminderIfNotExists(member, meeting, NotificationType.MEETING_IMMINENT);
            }
        }
    }

    /**
     * 정모 시작까지 남은 시간이 특정 시간 이내인지 확인
     *
     * @param meetingDate 정모 시작 시간
     * @param now 현재 시간
     * @param hours 확인할 시간 (시간 단위)
     * @return true: hours 이내, false: hours 초과
     */
    private boolean isWithinHours(LocalDateTime meetingDate, LocalDateTime now, int hours) {
        LocalDateTime threshold = now.plusHours(hours);
        return meetingDate.isBefore(threshold) && meetingDate.isAfter(now);
    }

    /**
     * 리마인더 알림이 없으면 생성
     *
     * 중복 생성을 방지하기 위해 먼저 존재 여부를 확인합니다.
     *
     * @param member 회원 엔티티
     * @param meeting 정모 엔티티
     * @param type 알림 타입 (MEETING_REMINDER 또는 MEETING_IMMINENT)
     */
    private void createReminderIfNotExists(Member member, Meeting meeting, NotificationType type) {
        // 이미 해당 리마인더가 있는지 확인
        boolean exists = notificationRepository.existsByMemberIdAndTypeAndRelatedId(
                member.getId(), type, meeting.getId());

        if (!exists) {
            String title;
            String message;

            if (type == NotificationType.MEETING_REMINDER) {
                title = "정모 리마인더";
                message = buildDayBeforeReminderMessage(meeting);
            } else {
                title = "정모 임박";
                message = String.format("'%s'이(가) 곧 시작됩니다.", meeting.getTitle());
            }

            Notification notification = Notification.builder()
                    .member(member)
                    .type(type)
                    .title(title)
                    .message(message)
                    .relatedId(meeting.getId())
                    .build();

            notificationRepository.save(notification);
            log.info("Meeting reminder created: {} for meeting {} to member {}",
                    type, meeting.getId(), member.getEmail());
        }
    }

    /**
     * D-1 리마인더 메시지 생성
     *
     * @param meeting 정모 엔티티
     * @return 리마인더 메시지
     */
    private String buildDayBeforeReminderMessage(Meeting meeting) {
        StringBuilder sb = new StringBuilder();
        sb.append("내일 '").append(meeting.getTitle()).append("'이(가) 있습니다.");

        // 시간 정보 추가
        if (meeting.getMeetingDate() != null) {
            String time = meeting.getMeetingDate().format(TIME_FORMATTER);
            sb.append(" (").append(time).append(")");
        }

        // 장소 정보 추가
        if (meeting.getLocation() != null && !meeting.getLocation().isEmpty()) {
            sb.append(" 장소: ").append(meeting.getLocation());
        }

        return sb.toString();
    }
}
