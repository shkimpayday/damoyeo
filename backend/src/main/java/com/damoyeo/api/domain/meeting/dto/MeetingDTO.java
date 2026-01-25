package com.damoyeo.api.domain.meeting.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 정모 응답 DTO
 * ============================================================================
 *
 * [역할]
 * 정모 정보를 프론트엔드에 전달하는 데이터 전송 객체입니다.
 * Meeting 엔티티의 정보와 연관 정보(모임명, 생성자 정보)를 담습니다.
 *
 * [포함 정보]
 * - 정모 기본 정보 (제목, 설명, 위치, 일시, 정원, 참가비)
 * - 소속 모임 정보 (groupId, groupName)
 * - 생성자 정보 (createdBy - 중첩 객체)
 * - 현재 사용자의 참석 상태 (myStatus)
 *
 * [사용 위치]
 * - MeetingController의 모든 조회 응답
 * - MeetingServiceImpl.entityToDTO()에서 생성
 *
 * [프론트엔드 타입]
 * TypeScript: MeetingDTO (meeting.d.ts)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDTO {

    // ========================================================================
    // 정모 기본 정보
    // ========================================================================

    /**
     * 정모 ID
     */
    private Long id;

    /**
     * 소속 모임 ID
     *
     * 이 정모가 어느 모임에서 진행되는지를 나타냅니다.
     */
    private Long groupId;

    /**
     * 소속 모임 이름
     *
     * 정모 카드나 목록에서 모임명을 표시하기 위해 포함합니다.
     * 예: "강남 러닝 크루"
     */
    private String groupName;

    /**
     * 정모 제목
     *
     * 예: "5월 첫째 주 러닝"
     */
    private String title;

    /**
     * 정모 설명
     *
     * 정모에 대한 상세 설명 (선택 사항)
     */
    private String description;

    // ========================================================================
    // 위치 정보
    // ========================================================================

    /**
     * 정모 장소 (주소 텍스트)
     *
     * 사람이 읽을 수 있는 주소 형태
     * 예: "서울특별시 서초구 반포대로 11길"
     */
    private String address;

    /**
     * 위치 정보 (중첩 객체)
     */
    private LocationDTO location;

    // ========================================================================
    // 일시 및 인원 정보
    // ========================================================================

    /**
     * 정모 일시
     *
     * 정모가 진행되는 날짜와 시간
     * 예: 2024-05-04T10:00:00
     */
    private LocalDateTime meetingDate;

    /**
     * 최대 참석 인원
     *
     * 정모에 참석할 수 있는 최대 인원
     * 예: 20
     */
    private int maxAttendees;

    /**
     * 현재 참석 예정 인원
     *
     * ATTENDING 상태인 참석자 수
     * MeetingRepository.countAttendees()로 계산됩니다.
     *
     * [프론트엔드 표시]
     * "참석 15/20명" 형태로 표시
     */
    private int currentAttendees;

    /**
     * 참가비 (원)
     *
     * 정모 참석에 필요한 비용
     * 0이면 무료
     */
    private int fee;

    /**
     * 정모 상태
     *
     * - SCHEDULED: 예정됨
     * - ONGOING: 진행 중
     * - COMPLETED: 완료됨
     * - CANCELLED: 취소됨
     *
     * MeetingStatus enum의 name() 값
     */
    private String status;

    // ========================================================================
    // 생성자 정보 (중첩 객체)
    // ========================================================================

    /**
     * 정모 생성자 정보
     *
     * 정모를 만든 사람의 정보
     */
    private MemberSummaryDTO createdBy;

    // ========================================================================
    // 현재 사용자 관련 정보
    // ========================================================================

    /**
     * 현재 로그인한 사용자의 참석 상태
     *
     * - ATTENDING: 참석 예정
     * - MAYBE: 미정
     * - NOT_ATTENDING: 불참
     * - null: 미등록 (참석 신청 전)
     *
     * [프론트엔드 활용]
     * 이 값에 따라 UI에서 버튼 상태를 다르게 표시합니다.
     * 예: myStatus가 "ATTENDING"이면 "참석 취소" 버튼 표시
     *     myStatus가 null이면 "참석하기" 버튼 표시
     *
     * [주의]
     * 비로그인 사용자는 null입니다.
     */
    private String myStatus;  // ATTENDING, MAYBE, NOT_ATTENDING, null

    /**
     * 정모 생성일시
     *
     * BaseEntity에서 상속받은 createdAt
     */
    private LocalDateTime createdAt;

    // ========================================================================
    // 위치 정보 내부 클래스
    // ========================================================================

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationDTO {
        private Double lat;
        private Double lng;
    }
}
