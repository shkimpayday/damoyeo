package com.damoyeo.api.domain.meeting.service;

import com.damoyeo.api.domain.meeting.dto.*;

import java.util.List;

/**
 * ============================================================================
 * 정모 서비스 인터페이스
 * ============================================================================
 *
 * [역할]
 * 정모 관련 비즈니스 로직의 계약(contract)을 정의합니다.
 *
 * [왜 인터페이스를 분리하는가?]
 * 1. 구현과 계약 분리: 나중에 다른 구현체로 교체 가능
 * 2. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 3. 의존성 역전: Controller는 인터페이스에만 의존
 *
 * [기능 분류]
 * - CRUD: 정모 생성/조회/수정/취소
 * - 목록 조회: 모임별, 다가오는, 내 정모
 * - 참석 관리: 등록, 취소, 목록 조회
 *
 * [사용 위치]
 * - MeetingController에서 주입받아 사용
 * - MeetingServiceImpl에서 구현
 */
public interface MeetingService {

    // ========================================================================
    // CRUD 기본 기능
    // ========================================================================

    /**
     * 정모 생성
     *
     * [처리 흐름]
     * 1. 요청한 사용자가 해당 모임의 멤버인지 확인
     * 2. 정모 엔티티 생성 및 저장
     * 3. 생성자를 자동으로 참석 등록 (ATTENDING)
     *
     * [권한]
     * 해당 모임의 승인된 멤버(APPROVED)만 가능
     *
     * @param email 정모 생성자의 이메일
     * @param request 정모 생성 정보
     * @return 생성된 정모 정보
     *
     * Controller: POST /api/meetings
     */
    MeetingDTO create(String email, MeetingCreateRequest request);

    /**
     * 정모 상세 조회
     *
     * [특징]
     * - email이 주어지면 현재 사용자의 참석 상태(myStatus) 포함
     * - 연관 엔티티(group, creator)를 fetch join으로 조회
     *
     * @param id 정모 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 정모 상세 정보
     *
     * Controller: GET /api/meetings/{id}
     */
    MeetingDTO getById(Long id, String email);

    /**
     * 정모 정보 수정
     *
     * [권한]
     * - 정모 생성자
     * - 또는 모임 관리자 (OWNER, MANAGER)
     *
     * [특징]
     * request의 null 필드는 기존 값 유지 (Partial Update)
     *
     * @param id 정모 ID
     * @param email 요청자 이메일 (권한 확인용)
     * @param request 수정할 정보
     * @return 수정된 정모 정보
     *
     * Controller: PUT /api/meetings/{id}
     */
    MeetingDTO modify(Long id, String email, MeetingModifyRequest request);

    /**
     * 정모 취소 (소프트 삭제)
     *
     * [권한]
     * - 정모 생성자
     * - 또는 모임 관리자 (OWNER, MANAGER)
     *
     * [동작]
     * 실제로 DB에서 삭제하지 않고 status를 CANCELLED로 변경합니다.
     *
     * @param id 정모 ID
     * @param email 요청자 이메일 (권한 확인용)
     *
     * Controller: DELETE /api/meetings/{id}
     */
    void delete(Long id, String email);

    // ========================================================================
    // 목록 조회
    // ========================================================================

    /**
     * 특정 모임의 정모 목록 조회 (전체)
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - 취소된 정모(CANCELLED) 제외
     * - 정모 날짜 오름차순 정렬
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 정모 목록
     *
     * Controller: GET /api/meetings/group/{groupId}
     */
    List<MeetingDTO> getByGroupId(Long groupId, String email);

    /**
     * 특정 모임의 예정된 정모 목록 조회
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - SCHEDULED, ONGOING 상태만
     * - 정모 날짜 오름차순 정렬
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 예정된 정모 목록
     *
     * Controller: GET /api/meetings/group/{groupId}/upcoming
     */
    List<MeetingDTO> getUpcomingByGroupId(Long groupId, String email);

    /**
     * 특정 모임의 지난 정모 목록 조회
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - COMPLETED 상태만
     * - 정모 날짜 내림차순 정렬 (최근 완료된 정모가 먼저)
     *
     * @param groupId 모임 ID
     * @param email 조회하는 사용자의 이메일 (null 가능)
     * @return 지난 정모 목록
     *
     * Controller: GET /api/meetings/group/{groupId}/past
     */
    List<MeetingDTO> getPastByGroupId(Long groupId, String email);

    /**
     * 다가오는 정모 목록 조회 (전체)
     *
     * [조건]
     * - 현재 시간 이후의 정모만
     * - SCHEDULED 상태인 정모만
     * - 정모 날짜 오름차순 정렬
     *
     * @return 다가오는 정모 목록
     *
     * Controller: GET /api/meetings/upcoming
     */
    List<MeetingDTO> getUpcomingMeetings();

    /**
     * 내가 참석 예정인 정모 목록 조회
     *
     * [조건]
     * - 내가 ATTENDING 상태로 등록한 정모만
     * - 현재 시간 이후의 정모만
     * - 정모 날짜 오름차순 정렬
     *
     * @param email 사용자 이메일
     * @return 내 정모 목록
     *
     * Controller: GET /api/meetings/my
     */
    List<MeetingDTO> getMyMeetings(String email);

    // ========================================================================
    // 참석 관리
    // ========================================================================

    /**
     * 정모 참석 등록
     *
     * [처리 흐름]
     * 1. 사용자가 해당 모임의 멤버인지 확인
     * 2. 이미 등록한 경우: 상태만 변경
     * 3. 신규 등록 시: 정원 확인 후 등록
     *
     * [권한]
     * 해당 모임의 승인된 멤버(APPROVED)만 가능
     *
     * @param meetingId 정모 ID
     * @param email 참석자 이메일
     * @param status 참석 상태 (ATTENDING, MAYBE, NOT_ATTENDING)
     *
     * Controller: POST /api/meetings/{id}/attend
     */
    void attend(Long meetingId, String email, String status);

    /**
     * 정모 참석 취소
     *
     * [동작]
     * 참석 정보를 DB에서 완전히 삭제합니다.
     *
     * @param meetingId 정모 ID
     * @param email 참석자 이메일
     *
     * Controller: DELETE /api/meetings/{id}/attend
     */
    void cancelAttend(Long meetingId, String email);

    /**
     * 정모 참석자 목록 조회
     *
     * [특징]
     * 모든 상태(ATTENDING, MAYBE, NOT_ATTENDING)의 참석자를 포함합니다.
     * 프론트엔드에서 상태별로 그룹핑하여 표시할 수 있습니다.
     *
     * @param meetingId 정모 ID
     * @return 참석자 목록
     *
     * Controller: GET /api/meetings/{id}/attendees
     */
    List<MeetingAttendeeDTO> getAttendees(Long meetingId);
}
