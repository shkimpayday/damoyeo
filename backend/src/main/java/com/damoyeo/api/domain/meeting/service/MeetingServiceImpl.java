package com.damoyeo.api.domain.meeting.service;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.group.entity.GroupMember;
import com.damoyeo.api.domain.group.entity.GroupRole;
import com.damoyeo.api.domain.group.entity.JoinStatus;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.group.repository.GroupRepository;
import com.damoyeo.api.domain.meeting.dto.*;
import com.damoyeo.api.domain.meeting.entity.*;
import com.damoyeo.api.domain.meeting.repository.MeetingAttendeeRepository;
import com.damoyeo.api.domain.meeting.repository.MeetingRepository;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.domain.notification.service.NotificationService;
import com.damoyeo.api.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 정모 서비스 구현체
 * ============================================================================
 *
 * [역할]
 * MeetingService 인터페이스의 실제 구현을 담당합니다.
 * 정모 관련 비즈니스 로직을 처리합니다.
 *
 * [주요 기능]
 * - 정모 CRUD (생성, 조회, 수정, 취소)
 * - 정모 목록 조회 (모임별, 다가오는, 내 정모)
 * - 참석 관리 (등록, 취소, 목록 조회)
 *
 * [트랜잭션 정책]
 * - 클래스 레벨 @Transactional: 모든 메서드에 트랜잭션 적용
 * - 조회 메서드: @Transactional(readOnly = true)로 성능 최적화
 *
 * [사용 위치]
 * - MeetingController에서 주입받아 사용
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
public class MeetingServiceImpl implements MeetingService {

    // ========================================================================
    // 의존성 주입 (DI)
    // ========================================================================

    /** 정모 레포지토리 */
    private final MeetingRepository meetingRepository;

    /** 정모 참석자 레포지토리 */
    private final MeetingAttendeeRepository attendeeRepository;

    /** 모임 레포지토리 (모임 존재 여부 확인용) */
    private final GroupRepository groupRepository;

    /** 모임 멤버 레포지토리 (권한 확인용) */
    private final GroupMemberRepository groupMemberRepository;

    /** 회원 레포지토리 (이메일로 회원 조회) */
    private final MemberRepository memberRepository;

    /** 알림 서비스 (정모 관련 알림 발송) */
    private final NotificationService notificationService;

    // ========================================================================
    // CRUD 기본 기능
    // ========================================================================

    /**
     * 정모 생성
     *
     * [처리 흐름]
     * 1. 요청한 사용자가 해당 모임의 승인된 멤버인지 확인
     * 2. 정모 엔티티 생성 및 저장
     * 3. 생성자를 자동으로 참석 등록 (ATTENDING)
     *
     * @param email 정모 생성자의 이메일
     * @param request 정모 생성 정보
     * @return 생성된 정모 정보
     */
    @Override
    public MeetingDTO create(String email, MeetingCreateRequest request) {
        // 1. 회원 조회
        Member creator = getMemberByEmail(email);

        // 2. 모임 조회 및 존재 여부 확인
        Group group = groupRepository.findByIdWithDetails(request.getGroupId())
                .orElseThrow(() -> CustomException.notFound("모임을 찾을 수 없습니다."));

        // 3. 모임 멤버인지 확인 (승인된 멤버만 정모 생성 가능)
        checkGroupMember(group.getId(), creator.getId());

        // 4. 정모 엔티티 생성
        Meeting meeting = Meeting.builder()
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .meetingDate(request.getMeetingDate())
                .maxAttendees(request.getMaxAttendees())
                .fee(request.getFee())
                .creator(creator)
                .build();

        // 5. 정모 저장
        Meeting saved = meetingRepository.save(meeting);

        // 6. 생성자를 자동으로 참석 등록
        //    정모를 만든 사람은 자동으로 참석하는 것이 자연스러움
        MeetingAttendee creatorAttendee = MeetingAttendee.builder()
                .meeting(saved)
                .member(creator)
                .status(AttendStatus.ATTENDING)
                .build();
        attendeeRepository.save(creatorAttendee);

        // 7. 모임 전체 멤버에게 새 정모 알림 발송 (생성자 제외)
        sendNewMeetingNotification(group, saved, creator);

        log.info("Meeting created: {} by {}", saved.getTitle(), email);
        return entityToDTO(saved, email);
    }

    /**
     * 정모 상세 조회
     *
     * [권한 체크]
     * 모임에 가입한 멤버만 정모 상세를 볼 수 있습니다.
     * 비로그인 또는 모임 멤버가 아닌 경우 403 에러를 반환합니다.
     *
     * @Transactional(readOnly = true): 조회 전용 트랜잭션
     * - 더티 체킹 비활성화로 성능 향상
     * - DB 복제본(slave) 사용 가능
     *
     * @param id 정모 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 정모 상세 정보 (myStatus, canEdit 포함)
     * @throws CustomException 비로그인 또는 모임 멤버가 아닌 경우 403 에러
     */
    @Override
    @Transactional(readOnly = true)
    public MeetingDTO getById(Long id, String email) {
        Meeting meeting = meetingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("정모를 찾을 수 없습니다."));

        // 비로그인 사용자는 정모 상세를 볼 수 없음
        if (email == null) {
            throw CustomException.forbidden("모임 멤버만 정모를 볼 수 있습니다. 로그인해주세요.");
        }

        // 모임 멤버인지 확인
        Member member = getMemberByEmail(email);
        checkGroupMember(meeting.getGroup().getId(), member.getId());

        return entityToDTO(meeting, email);
    }

    /**
     * 정모 정보 수정
     *
     * [Partial Update 패턴]
     * request의 각 필드가 null이 아닌 경우에만 해당 필드를 업데이트합니다.
     * 이를 통해 클라이언트는 변경하고 싶은 필드만 전송할 수 있습니다.
     *
     * @param id 정모 ID
     * @param email 요청자 이메일 (권한 확인용)
     * @param request 수정할 정보 (null인 필드는 무시)
     * @return 수정된 정모 정보
     */
    @Override
    public MeetingDTO modify(Long id, String email, MeetingModifyRequest request) {
        // 1. 정모 조회
        Meeting meeting = meetingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("정모를 찾을 수 없습니다."));

        // 2. 권한 확인 (생성자 또는 모임 관리자)
        checkMeetingManager(meeting, email);

        // 3. 장소/시간 변경 여부 추적 (알림 발송 여부 결정용)
        boolean isLocationOrTimeChanged = false;
        if (request.getAddress() != null && !request.getAddress().equals(meeting.getLocation())) {
            isLocationOrTimeChanged = true;
        }
        if (request.getMeetingDate() != null && !request.getMeetingDate().equals(meeting.getMeetingDate())) {
            isLocationOrTimeChanged = true;
        }

        // 4. Partial Update: null이 아닌 필드만 업데이트
        if (request.getTitle() != null) {
            meeting.changeTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.changeDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            meeting.changeLocation(request.getAddress(), request.getLatitude(), request.getLongitude());
        }
        if (request.getMeetingDate() != null) {
            meeting.changeMeetingDate(request.getMeetingDate());
        }
        if (request.getMaxAttendees() != null) {
            meeting.changeMaxAttendees(request.getMaxAttendees());
        }
        if (request.getFee() != null) {
            meeting.changeFee(request.getFee());
        }
        if (request.getStatus() != null) {
            meeting.changeStatus(MeetingStatus.valueOf(request.getStatus()));
        }

        // 5. 장소/시간이 변경된 경우 참석 예정자에게 알림 발송
        if (isLocationOrTimeChanged) {
            sendMeetingUpdatedNotification(meeting);
        }

        // 6. JPA 더티 체킹으로 자동 저장 (save() 호출 불필요)
        return entityToDTO(meeting, email);
    }

    /**
     * 정모 취소 (소프트 삭제)
     *
     * [Soft Delete 패턴]
     * 데이터를 물리적으로 삭제하지 않고, status를 CANCELLED로 변경합니다.
     * 이렇게 하면 취소된 정모도 기록으로 남아있어 추후 확인 가능합니다.
     *
     * @param id 정모 ID
     * @param email 요청자 이메일 (권한 확인용)
     */
    @Override
    public void delete(Long id, String email) {
        Meeting meeting = meetingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> CustomException.notFound("정모를 찾을 수 없습니다."));

        // 권한 확인 (생성자 또는 모임 관리자)
        checkMeetingManager(meeting, email);

        // 참석 예정자에게 정모 취소 알림 발송 (상태 변경 전에 알림 발송)
        sendMeetingCancelledNotification(meeting);

        // 상태를 CANCELLED로 변경 (소프트 삭제)
        meeting.changeStatus(MeetingStatus.CANCELLED);
        log.info("Meeting cancelled: {} by {}", meeting.getTitle(), email);
    }

    // ========================================================================
    // 목록 조회
    // ========================================================================

    /**
     * 특정 모임의 정모 목록 조회 (전체)
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 정모 목록 (취소된 정모 제외)
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingDTO> getByGroupId(Long groupId, String email) {
        return meetingRepository.findByGroupId(groupId).stream()
                .map(m -> entityToDTO(m, email))
                .collect(Collectors.toList());
    }

    /**
     * 특정 모임의 예정된 정모 목록 조회 (날짜 기반)
     *
     * [조건]
     * - 현재 시간 이후의 정모만 (meetingDate > now)
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 오름차순 정렬
     *
     * [설계 의도]
     * status 필드 대신 날짜 기반으로 예정/지난 정모를 구분합니다.
     * 스케줄러 방식보다 단순하고 실시간으로 정확한 결과를 제공합니다.
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 예정된 정모 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingDTO> getUpcomingByGroupId(Long groupId, String email) {
        return meetingRepository.findUpcomingByGroupId(groupId, LocalDateTime.now()).stream()
                .map(m -> entityToDTO(m, email))
                .collect(Collectors.toList());
    }

    /**
     * 특정 모임의 지난 정모 목록 조회 (날짜 기반)
     *
     * [조건]
     * - 현재 시간 이전의 정모만 (meetingDate <= now)
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 내림차순 정렬 (최근 지난 정모가 먼저)
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 지난 정모 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingDTO> getPastByGroupId(Long groupId, String email) {
        return meetingRepository.findPastByGroupId(groupId, LocalDateTime.now()).stream()
                .map(m -> entityToDTO(m, email))
                .collect(Collectors.toList());
    }

    /**
     * 다가오는 정모 목록 조회 (전체)
     *
     * 현재 시간 이후의 SCHEDULED 상태 정모들을 조회합니다.
     * 로그인하지 않은 사용자도 볼 수 있으므로 myStatus는 포함되지 않습니다.
     *
     * @return 다가오는 정모 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingDTO> getUpcomingMeetings() {
        return meetingRepository.findUpcomingMeetings(LocalDateTime.now()).stream()
                .map(m -> entityToDTO(m, null))  // email = null (비로그인)
                .collect(Collectors.toList());
    }

    /**
     * 내가 참석 예정인 정모 목록 조회
     *
     * 내가 ATTENDING 상태로 등록한 미래의 정모들을 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 내 정모 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingDTO> getMyMeetings(String email) {
        Member member = getMemberByEmail(email);
        return meetingRepository.findMyUpcomingMeetings(member.getId(), LocalDateTime.now()).stream()
                .map(m -> entityToDTO(m, email))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 참석 관리
    // ========================================================================

    /**
     * 정모 참석 등록
     *
     * [처리 흐름]
     * 1. 사용자가 해당 모임의 승인된 멤버인지 확인
     * 2. 이미 등록한 경우: 상태만 변경 (중복 등록 방지)
     * 3. 신규 등록 시: 정원 확인 후 등록
     *
     * [정원 확인]
     * ATTENDING 상태로 등록할 때만 정원을 확인합니다.
     * MAYBE나 NOT_ATTENDING은 정원에 포함되지 않습니다.
     *
     * @param meetingId 정모 ID
     * @param email 참석자 이메일
     * @param status 참석 상태 (ATTENDING, MAYBE, NOT_ATTENDING)
     */
    @Override
    public void attend(Long meetingId, String email, String status) {
        // 1. 정모 조회
        Meeting meeting = meetingRepository.findByIdWithDetails(meetingId)
                .orElseThrow(() -> CustomException.notFound("정모를 찾을 수 없습니다."));
        Member member = getMemberByEmail(email);

        // 2. 모임 멤버인지 확인 (승인된 멤버만 참석 가능)
        checkGroupMember(meeting.getGroup().getId(), member.getId());

        // 3. 참석 상태 파싱 (기본값: ATTENDING)
        AttendStatus attendStatus = status != null ? AttendStatus.valueOf(status) : AttendStatus.ATTENDING;

        // 4. 이미 등록했는지 확인 (중복 등록 방지)
        var existing = attendeeRepository.findByMeetingIdAndMemberId(meetingId, member.getId());
        if (existing.isPresent()) {
            // 이미 등록한 경우: 상태만 변경
            existing.get().changeStatus(attendStatus);
            log.info("Attendance updated for meeting {} by {}", meetingId, email);
            return;
        }

        // 5. 정원 확인 (ATTENDING인 경우만)
        //    MAYBE, NOT_ATTENDING은 정원에 포함되지 않음
        if (attendStatus == AttendStatus.ATTENDING) {
            int currentCount = meetingRepository.countAttendees(meetingId);
            if (currentCount >= meeting.getMaxAttendees()) {
                throw new CustomException("정모 정원이 가득 찼습니다.", HttpStatus.BAD_REQUEST);
            }
        }

        // 6. 새 참석 정보 등록
        MeetingAttendee attendee = MeetingAttendee.builder()
                .meeting(meeting)
                .member(member)
                .status(attendStatus)
                .build();

        attendeeRepository.save(attendee);
        log.info("Attendance registered for meeting {} by {} (status: {})", meetingId, email, attendStatus);
    }

    /**
     * 정모 참석 취소
     *
     * 참석 정보를 DB에서 완전히 삭제합니다.
     *
     * @param meetingId 정모 ID
     * @param email 참석자 이메일
     */
    @Override
    public void cancelAttend(Long meetingId, String email) {
        Member member = getMemberByEmail(email);

        // 참석 정보 조회
        MeetingAttendee attendee = attendeeRepository.findByMeetingIdAndMemberId(meetingId, member.getId())
                .orElseThrow(() -> CustomException.notFound("참석 정보를 찾을 수 없습니다."));

        // 참석 정보 삭제
        attendeeRepository.delete(attendee);
        log.info("Attendance cancelled for meeting {} by {}", meetingId, email);
    }

    /**
     * 정모 참석자 목록 조회
     *
     * 모든 상태의 참석자를 포함합니다.
     * 프론트엔드에서 상태별로 그룹핑하여 표시할 수 있습니다.
     *
     * @param meetingId 정모 ID
     * @return 참석자 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<MeetingAttendeeDTO> getAttendees(Long meetingId) {
        return attendeeRepository.findByMeetingId(meetingId).stream()
                .map(this::attendeeToDTO)
                .collect(Collectors.toList());
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
     * 모임 멤버 여부 확인
     *
     * 사용자가 해당 모임의 승인된 멤버(APPROVED)인지 확인합니다.
     *
     * @param groupId 모임 ID
     * @param memberId 회원 ID
     * @throws CustomException 멤버가 아니면 403 에러
     */
    private void checkGroupMember(Long groupId, Long memberId) {
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> CustomException.forbidden("모임 멤버만 정모를 생성할 수 있습니다."));

        // 승인된 멤버인지 확인
        if (gm.getStatus() != JoinStatus.APPROVED) {
            throw CustomException.forbidden("승인된 멤버만 정모를 생성할 수 있습니다.");
        }
    }

    /**
     * 정모 관리 권한 확인
     *
     * 정모를 수정/취소할 수 있는 권한이 있는지 확인합니다.
     *
     * [권한이 있는 사람]
     * 1. 정모 생성자
     * 2. 모임장 (OWNER)
     * 3. 운영진 (MANAGER)
     *
     * @param meeting 정모 엔티티
     * @param email 요청자 이메일
     * @throws CustomException 권한이 없으면 403 에러
     */
    private void checkMeetingManager(Meeting meeting, String email) {
        Member member = getMemberByEmail(email);

        // 1. 정모 생성자인지 확인
        if (meeting.getCreator().getEmail().equals(email)) {
            return;  // 생성자는 항상 권한 있음
        }

        // 2. 모임 관리자인지 확인 (OWNER 또는 MANAGER)
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroup().getId(), member.getId())
                .orElseThrow(() -> CustomException.forbidden("권한이 없습니다."));

        if (gm.getRole() != GroupRole.OWNER && gm.getRole() != GroupRole.MANAGER) {
            throw CustomException.forbidden("권한이 없습니다.");
        }
    }

    /**
     * Meeting 엔티티를 MeetingDTO로 변환
     *
     * [변환 내용]
     * - 정모 기본 정보
     * - 소속 모임 정보 (groupId, groupName)
     * - 생성자 정보 (createdBy - 중첩 객체)
     * - 위치 정보 (location - 중첩 객체)
     * - 현재 참석 인원 수 (countAttendees)
     * - 현재 사용자의 참석 상태 (myStatus)
     *
     * @param meeting 정모 엔티티
     * @param email 현재 사용자 이메일 (null 가능)
     * @return MeetingDTO
     */
    private MeetingDTO entityToDTO(Meeting meeting, String email) {
        // 현재 참석 인원 수 조회
        int attendeeCount = meetingRepository.countAttendees(meeting.getId());

        MeetingDTO.MeetingDTOBuilder builder = MeetingDTO.builder()
                .id(meeting.getId())
                .groupId(meeting.getGroup().getId())
                .groupName(meeting.getGroup().getName())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .address(meeting.getLocation())
                .meetingDate(meeting.getMeetingDate())
                .maxAttendees(meeting.getMaxAttendees())
                .currentAttendees(attendeeCount)
                .fee(meeting.getFee())
                .status(meeting.getStatus().name())
                .createdBy(MemberSummaryDTO.from(meeting.getCreator()))
                .createdAt(meeting.getCreatedAt());

        // 위치 정보를 중첩 객체로 변환
        if (meeting.getLatitude() != null && meeting.getLongitude() != null) {
            builder.location(MeetingDTO.LocationDTO.builder()
                    .lat(meeting.getLatitude())
                    .lng(meeting.getLongitude())
                    .build());
        }

        // 현재 사용자의 참석 상태 및 수정 권한 설정
        // 로그인한 사용자인 경우에만 myStatus와 canEdit을 조회
        if (email != null) {
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member != null) {
                // myStatus 설정
                attendeeRepository.findByMeetingIdAndMemberId(meeting.getId(), member.getId())
                        .ifPresent(a -> builder.myStatus(a.getStatus().name()));

                // canEdit 계산: 정모 생성자 또는 모임 OWNER/MANAGER
                boolean canEdit = calculateCanEdit(meeting, member);
                builder.canEdit(canEdit);
            }
        }

        return builder.build();
    }

    /**
     * 정모 수정 권한 계산
     *
     * [권한이 있는 경우]
     * 1. 정모 생성자
     * 2. 모임장 (OWNER)
     * 3. 운영진 (MANAGER)
     *
     * @param meeting 정모 엔티티
     * @param member 현재 사용자
     * @return 수정 권한이 있으면 true, 없으면 false
     */
    private boolean calculateCanEdit(Meeting meeting, Member member) {
        // 1. 정모 생성자인지 확인
        if (meeting.getCreator().getId().equals(member.getId())) {
            return true;
        }

        // 2. 모임 관리자인지 확인 (OWNER 또는 MANAGER)
        return groupMemberRepository.findByGroupIdAndMemberId(meeting.getGroup().getId(), member.getId())
                .map(gm -> gm.getRole() == GroupRole.OWNER || gm.getRole() == GroupRole.MANAGER)
                .orElse(false);
    }

    /**
     * MeetingAttendee 엔티티를 MeetingAttendeeDTO로 변환
     *
     * @param attendee 참석자 엔티티
     * @return MeetingAttendeeDTO
     */
    private MeetingAttendeeDTO attendeeToDTO(MeetingAttendee attendee) {
        return MeetingAttendeeDTO.builder()
                .id(attendee.getId())
                .member(MemberSummaryDTO.from(attendee.getMember()))
                .status(attendee.getStatus().name())
                .registeredAt(attendee.getCreatedAt())
                .build();
    }

    // ========================================================================
    // 알림 발송 Helper 메서드
    // ========================================================================

    /**
     * 새 정모 생성 알림 발송
     *
     * 모임의 모든 승인된 멤버에게 새 정모 알림을 발송합니다.
     * 정모 생성자는 알림 대상에서 제외됩니다.
     *
     * @param group 모임 엔티티
     * @param meeting 생성된 정모 엔티티
     * @param creator 정모 생성자 (알림 대상 제외)
     */
    private void sendNewMeetingNotification(Group group, Meeting meeting, Member creator) {
        // 모임의 승인된 멤버 목록 조회
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndStatus(
                group.getId(), JoinStatus.APPROVED);

        String title = "새 정모";
        String message = String.format("'%s'에 새 정모 '%s'이(가) 등록되었습니다.",
                group.getName(), meeting.getTitle());

        // 생성자를 제외한 모든 멤버에게 알림 발송
        for (GroupMember gm : members) {
            if (!gm.getMember().getId().equals(creator.getId())) {
                notificationService.send(
                        gm.getMember(),
                        NotificationType.NEW_MEETING,
                        title,
                        message,
                        meeting.getId()
                );
            }
        }

        log.info("New meeting notification sent to {} members for meeting {}",
                members.size() - 1, meeting.getId());
    }

    /**
     * 정모 정보 변경 알림 발송
     *
     * 정모의 장소/시간이 변경된 경우, 참석 예정자(ATTENDING)에게 알림을 발송합니다.
     *
     * @param meeting 수정된 정모 엔티티
     */
    private void sendMeetingUpdatedNotification(Meeting meeting) {
        // 참석 예정자(ATTENDING) 목록 조회
        List<MeetingAttendee> attendees = attendeeRepository.findByMeetingIdAndStatus(
                meeting.getId(), AttendStatus.ATTENDING);

        String title = "정모 변경";
        String message = String.format("'%s' 일정이 변경되었습니다. 확인해주세요.", meeting.getTitle());

        for (MeetingAttendee attendee : attendees) {
            notificationService.send(
                    attendee.getMember(),
                    NotificationType.MEETING_UPDATED,
                    title,
                    message,
                    meeting.getId()
            );
        }

        log.info("Meeting updated notification sent to {} attendees for meeting {}",
                attendees.size(), meeting.getId());
    }

    /**
     * 정모 취소 알림 발송
     *
     * 정모가 취소된 경우, 참석 예정자(ATTENDING)에게 알림을 발송합니다.
     *
     * @param meeting 취소된 정모 엔티티
     */
    private void sendMeetingCancelledNotification(Meeting meeting) {
        // 참석 예정자(ATTENDING) 목록 조회
        List<MeetingAttendee> attendees = attendeeRepository.findByMeetingIdAndStatus(
                meeting.getId(), AttendStatus.ATTENDING);

        String title = "정모 취소";
        String message = String.format("'%s'이(가) 취소되었습니다.", meeting.getTitle());

        for (MeetingAttendee attendee : attendees) {
            notificationService.send(
                    attendee.getMember(),
                    NotificationType.MEETING_CANCELLED,
                    title,
                    message,
                    meeting.getId()
            );
        }

        log.info("Meeting cancelled notification sent to {} attendees for meeting {}",
                attendees.size(), meeting.getId());
    }
}
