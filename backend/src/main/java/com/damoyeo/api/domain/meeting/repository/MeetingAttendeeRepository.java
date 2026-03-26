package com.damoyeo.api.domain.meeting.repository;

import com.damoyeo.api.domain.meeting.entity.AttendStatus;
import com.damoyeo.api.domain.meeting.entity.MeetingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * 정모 참석자(MeetingAttendee) 레포지토리
 * ============================================================================
 *
 * [역할]
 * MeetingAttendee 엔티티의 데이터베이스 접근을 담당합니다.
 * 정모와 회원 간의 N:M 관계를 관리합니다.
 *
 * [주요 기능]
 * - 정모의 전체 참석자 목록 조회
 * - 상태별 참석자 목록 조회 (ATTENDING, MAYBE, NOT_ATTENDING)
 * - 특정 회원의 참석 정보 조회
 * - 중복 참석 등록 확인
 *
 * [사용 위치]
 * - MeetingServiceImpl에서 주입받아 사용
 *
 * [N+1 문제 방지]
 * fetch join을 사용하여 member 정보를 함께 조회합니다.
 */
public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, Long> {

    /**
     * 정모의 전체 참석자 목록 조회
     *
     * [N+1 문제 방지]
     * member를 fetch join으로 함께 조회하여
     * 참석자 목록 표시 시 추가 쿼리가 발생하지 않습니다.
     *
     * @param meetingId 정모 ID
     * @return 전체 참석자 목록 (ATTENDING, MAYBE, NOT_ATTENDING 모두 포함)
     *
     * [프론트엔드 UI]
     * 정모 상세 페이지의 "참석자" 탭에서 사용
     * 상태별로 그룹핑하여 표시 가능
     *
     * 호출 위치: MeetingServiceImpl.getAttendees()
     */
    @Query("select ma from MeetingAttendee ma " +
            "left join fetch ma.member " +
            "where ma.meeting.id = :meetingId")
    List<MeetingAttendee> findByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 상태별 참석자 목록 조회
     *
     * 특정 참석 상태의 참석자만 조회합니다.
     *
     * @param meetingId 정모 ID
     * @param status 조회할 참석 상태 (ATTENDING, MAYBE, NOT_ATTENDING)
     * @return 해당 상태의 참석자 목록
     *
     * [사용 예]
     * - findByMeetingIdAndStatus(1L, AttendStatus.ATTENDING)
     *   → 참석 예정인 사람만 조회
     *
     * [프론트엔드 UI]
     * "참석 예정 15명" 버튼 클릭 시 해당 상태 참석자만 표시
     */
    @Query("select ma from MeetingAttendee ma " +
            "left join fetch ma.member " +
            "where ma.meeting.id = :meetingId and ma.status = :status")
    List<MeetingAttendee> findByMeetingIdAndStatus(@Param("meetingId") Long meetingId,
                                                   @Param("status") AttendStatus status);

    /**
     * 특정 회원의 참석 정보 조회
     *
     * 특정 정모에 대한 특정 회원의 참석 정보를 조회합니다.
     *
     * @param meetingId 정모 ID
     * @param memberId 회원 ID
     * @return 참석 정보 (없으면 Optional.empty())
     *
     * [사용 목적]
     * - 참석 상태 변경 시 기존 참석 정보 조회
     * - 현재 사용자의 참석 상태 확인 (UI에서 버튼 상태 표시)
     *
     * 호출 위치: MeetingServiceImpl.attend(), cancelAttend(), entityToDTO()
     */
    Optional<MeetingAttendee> findByMeetingIdAndMemberId(Long meetingId, Long memberId);

    /**
     * 참석 등록 여부 확인
     *
     * 특정 회원이 특정 정모에 이미 참석 등록했는지 확인합니다.
     *
     * @param meetingId 정모 ID
     * @param memberId 회원 ID
     * @return true: 이미 등록됨, false: 미등록
     *
     * [사용 목적]
     * 중복 참석 등록을 방지하기 위해 사용합니다.
     * (실제 로직에서는 findByMeetingIdAndMemberId를 더 많이 사용)
     */
    boolean existsByMeetingIdAndMemberId(Long meetingId, Long memberId);

    // ========================================================================
    // 정모 채팅 권한 확인
    // ========================================================================

    /**
     * 회원이 정모에 참석 예정인지 확인
     *
     * [용도]
     * 정모 채팅방 접근 권한 검증에 사용됩니다.
     * ATTENDING 상태인 참석자만 채팅에 참여할 수 있습니다.
     *
     * @param meetingId 정모 ID
     * @param memberId 회원 ID
     * @return true: 참석 예정, false: 미참석 또는 다른 상태
     *
     * 호출 위치: ChatServiceImpl.validateMeetingChatAccess()
     */
    @Query("select case when count(ma) > 0 then true else false end " +
            "from MeetingAttendee ma " +
            "where ma.meeting.id = :meetingId and ma.member.id = :memberId " +
            "and ma.status = 'ATTENDING'")
    boolean isAttending(@Param("meetingId") Long meetingId, @Param("memberId") Long memberId);
}
