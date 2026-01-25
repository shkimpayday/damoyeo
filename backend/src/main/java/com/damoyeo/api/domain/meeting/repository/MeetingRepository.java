package com.damoyeo.api.domain.meeting.repository;

import com.damoyeo.api.domain.meeting.entity.Meeting;
import com.damoyeo.api.domain.meeting.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * 정모(Meeting) 레포지토리
 * ============================================================================
 *
 * [역할]
 * Meeting 엔티티의 데이터베이스 접근을 담당합니다.
 *
 * [주요 기능]
 * - 정모 상세 조회 (fetch join으로 연관 엔티티 로딩)
 * - 모임별 정모 목록 조회
 * - 다가오는 정모 조회
 * - 내가 참석하는 정모 조회
 * - 참석자 수 카운트
 *
 * [사용 위치]
 * - MeetingServiceImpl에서 주입받아 사용
 *
 * [N+1 문제 방지]
 * fetch join을 사용하여 연관 엔티티를 한 번의 쿼리로 조회합니다.
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * 정모 상세 조회 (연관 엔티티 포함)
     *
     * [N+1 문제 방지]
     * group, creator를 fetch join으로 함께 조회합니다.
     * 이렇게 하면 정모 조회 시 모임/생성자 정보를 위한 추가 쿼리가 발생하지 않습니다.
     *
     * @param id 정모 ID
     * @return 정모 엔티티 (group, creator 포함)
     *
     * [실행되는 SQL]
     * SELECT m.*, g.*, c.*
     * FROM meeting m
     * LEFT JOIN club g ON m.group_id = g.id
     * LEFT JOIN member c ON m.creator_id = c.id
     * WHERE m.id = ?
     *
     * 호출 위치: MeetingServiceImpl.getById(), modify(), delete()
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group g " +
            "left join fetch m.creator " +
            "where m.id = :id")
    Optional<Meeting> findByIdWithDetails(@Param("id") Long id);

    /**
     * 특정 모임의 정모 목록 조회
     *
     * [조건]
     * - 해당 모임(group)에 속한 정모만
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 오름차순 정렬 (가장 가까운 정모가 먼저)
     *
     * @param groupId 모임 ID
     * @return 정모 목록
     *
     * [프론트엔드 UI]
     * 모임 상세 페이지의 "정모" 탭에서 목록 표시
     *
     * 호출 위치: MeetingServiceImpl.getByGroupId()
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group " +
            "where m.group.id = :groupId and m.status <> 'CANCELLED' " +
            "order by m.meetingDate asc")
    List<Meeting> findByGroupId(@Param("groupId") Long groupId);

    /**
     * 다가오는 정모 조회 (전체)
     *
     * [조건]
     * - 현재 시간 이후의 정모만
     * - SCHEDULED 상태인 정모만
     * - 정모 날짜 오름차순 정렬
     *
     * @param now 현재 시간 (LocalDateTime.now())
     * @return 다가오는 정모 목록
     *
     * [프론트엔드 UI]
     * 메인 페이지나 "다가오는 정모" 섹션에서 사용
     *
     * 호출 위치: MeetingServiceImpl.getUpcomingMeetings()
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group g " +
            "left join fetch g.category " +
            "where m.meetingDate > :now and m.status = 'SCHEDULED' " +
            "order by m.meetingDate asc")
    List<Meeting> findUpcomingMeetings(@Param("now") LocalDateTime now);

    /**
     * 내가 참석 예정인 정모 조회
     *
     * [조건]
     * - 내가 ATTENDING 상태로 등록한 정모
     * - 현재 시간 이후의 정모만
     * - 정모 날짜 오름차순 정렬
     *
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @return 내가 참석 예정인 정모 목록
     *
     * [SQL 설명]
     * MeetingAttendee 테이블과 JOIN하여
     * 현재 사용자가 ATTENDING으로 등록한 정모만 조회합니다.
     *
     * [프론트엔드 UI]
     * "내 정모" 페이지에서 사용
     *
     * 호출 위치: MeetingServiceImpl.getMyMeetings()
     */
    @Query("select m from Meeting m " +
            "join MeetingAttendee ma on ma.meeting.id = m.id " +
            "left join fetch m.group g " +
            "where ma.member.id = :memberId and ma.status = 'ATTENDING' " +
            "and m.meetingDate > :now " +
            "order by m.meetingDate asc")
    List<Meeting> findMyUpcomingMeetings(@Param("memberId") Long memberId,
                                         @Param("now") LocalDateTime now);

    /**
     * 정모 참석자 수 카운트
     *
     * [조건]
     * ATTENDING 상태인 참석자만 카운트합니다.
     * MAYBE, NOT_ATTENDING은 제외됩니다.
     *
     * @param meetingId 정모 ID
     * @return 참석 예정 인원 수
     *
     * [사용 목적]
     * - 정원 확인: 새 참석자 등록 전 정원 초과 여부 확인
     * - UI 표시: "참석 15/20명" 형태로 표시
     *
     * 호출 위치: MeetingServiceImpl.attend(), entityToDTO()
     */
    @Query("select count(ma) from MeetingAttendee ma " +
            "where ma.meeting.id = :meetingId and ma.status = 'ATTENDING'")
    int countAttendees(@Param("meetingId") Long meetingId);
}
