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
 * 정모(Meeting) 레포지토리
 *
 * Meeting 엔티티의 데이터베이스 접근을 담당합니다.
 *
 * - 정모 상세 조회 (fetch join으로 연관 엔티티 로딩)
 * - 모임별 정모 목록 조회
 * - 다가오는 정모 조회
 * - 내가 참석하는 정모 조회
 * - 참석자 수 카운트
 *
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
     * - 현재 시간 이후의 정모만 (날짜 기반 필터링)
     * - 취소된 정모(CANCELLED) 제외
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
            "where m.meetingDate > :now and m.status <> 'CANCELLED' " +
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

    // 날짜 기반 목록 조회 (예정/지난 정모 분리)

    /**
     * 특정 모임의 예정된 정모 목록 조회 (날짜 기반)
     *
     * [조건]
     * - 해당 모임에 속한 정모
     * - 현재 시간 이후의 정모 (meetingDate > now)
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 오름차순 정렬
     *
     * [설계 의도]
     * status 필드 대신 날짜 기반으로 예정/지난 정모를 구분합니다.
     * 이 방식이 스케줄러 방식보다 단순하고 정확합니다.
     *
     * @param groupId 모임 ID
     * @param now 현재 시간
     * @return 예정된 정모 목록
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group " +
            "where m.group.id = :groupId " +
            "and m.meetingDate > :now " +
            "and m.status <> 'CANCELLED' " +
            "order by m.meetingDate asc")
    List<Meeting> findUpcomingByGroupId(@Param("groupId") Long groupId,
                                        @Param("now") LocalDateTime now);

    /**
     * 특정 모임의 지난 정모 목록 조회 (날짜 기반)
     *
     * [조건]
     * - 해당 모임에 속한 정모
     * - 현재 시간 이전의 정모 (meetingDate <= now)
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 내림차순 정렬 (최근 지난 정모가 먼저)
     *
     * @param groupId 모임 ID
     * @param now 현재 시간
     * @return 지난 정모 목록
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group " +
            "where m.group.id = :groupId " +
            "and m.meetingDate <= :now " +
            "and m.status <> 'CANCELLED' " +
            "order by m.meetingDate desc")
    List<Meeting> findPastByGroupId(@Param("groupId") Long groupId,
                                    @Param("now") LocalDateTime now);

    // 리마인더 스케줄러용 쿼리

    /**
     * 1일 전 리마인더 대상 정모 조회
     *
     * [조건]
     * - 정모 시작 시간이 startTime ~ endTime 사이인 정모
     * - 취소된 정모(CANCELLED) 제외
     *
     * 매일 오전 9시에 실행하여, 내일 진행되는 정모를 조회합니다.
     * startTime = 내일 00:00, endTime = 내일 23:59:59
     *
     * @param startTime 조회 시작 시간 (내일 시작)
     * @param endTime 조회 종료 시간 (내일 끝)
     * @return 1일 전 리마인더 대상 정모 목록
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group " +
            "where m.meetingDate >= :startTime " +
            "and m.meetingDate < :endTime " +
            "and m.status <> 'CANCELLED'")
    List<Meeting> findMeetingsForDayBeforeReminder(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 3시간 전 리마인더 대상 정모 조회
     *
     * [조건]
     * - 정모 시작 시간이 startTime ~ endTime 사이인 정모
     * - 취소된 정모(CANCELLED) 제외
     *
     * 매 시간 정각에 실행하여, 3시간 후 시작하는 정모를 조회합니다.
     * startTime = now + 3시간, endTime = now + 4시간
     *
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     * @return 3시간 전 리마인더 대상 정모 목록
     */
    @Query("select m from Meeting m " +
            "left join fetch m.group " +
            "where m.meetingDate >= :startTime " +
            "and m.meetingDate < :endTime " +
            "and m.status <> 'CANCELLED'")
    List<Meeting> findMeetingsForImminentReminder(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    // 관리자용 쿼리

    /**
     * 예정된 정모 수 집계
     *
     * [용도]
     * 관리자 대시보드에서 예정된 정모 수 표시
     *
     * @param now 현재 시간
     * @return 예정된 정모 수
     */
    @Query("select count(m) from Meeting m where m.meetingDate > :now and m.status <> 'CANCELLED'")
    long countUpcomingMeetings(@Param("now") LocalDateTime now);
}
