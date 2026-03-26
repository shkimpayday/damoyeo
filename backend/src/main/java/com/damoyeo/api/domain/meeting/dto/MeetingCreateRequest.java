package com.damoyeo.api.domain.meeting.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 정모 생성 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 정모 생성 시 프론트엔드에서 전송하는 데이터를 담는 객체입니다.
 * Bean Validation으로 입력값을 검증합니다.
 *
 * [필수 필드]
 * - groupId: 정모가 속할 모임
 * - title: 정모 제목
 * - meetingDate: 정모 일시 (미래 시간만 허용)
 *
 * [선택 필드]
 * - description, location, latitude, longitude
 * - maxAttendees (기본값: 20)
 * - fee (기본값: 0)
 *
 * [사용 위치]
 * - MeetingController.create()
 * - MeetingServiceImpl.create()
 *
 * [프론트엔드 요청 예시]
 * POST /api/meetings
 * {
 *   "groupId": 1,
 *   "title": "5월 첫째 주 러닝",
 *   "description": "반포 한강공원에서 5km 러닝합니다",
 *   "address": "서울 서초구 반포대로 11길",
 *   "latitude": 37.5080,
 *   "longitude": 126.9956,
 *   "meetingDate": "2024-05-04T10:00:00",
 *   "maxAttendees": 20,
 *   "fee": 0
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingCreateRequest {

    /**
     * 소속 모임 ID (필수)
     *
     * 정모가 어느 모임에 속하는지를 지정합니다.
     * 존재하지 않는 모임 ID면 MeetingServiceImpl에서 예외 발생
     */
    @NotNull(message = "모임 ID는 필수입니다.")
    private Long groupId;

    /**
     * 정모 제목 (필수)
     *
     * 정모를 나타내는 제목
     * 예: "5월 첫째 주 러닝", "독서 토론회 - 데미안"
     */
    @NotBlank(message = "정모 제목은 필수입니다.")
    private String title;

    /**
     * 정모 설명 (선택)
     *
     * 정모에 대한 상세 설명
     * 참석자들에게 알려줄 내용을 작성합니다.
     */
    private String description;

    // ========================================================================
    // 위치 정보 (선택)
    // ========================================================================

    /**
     * 정모 장소 (주소 텍스트)
     *
     * 사람이 읽을 수 있는 주소 형태
     */
    private String address;

    /**
     * 위도 (Latitude)
     *
     * 지도 표시용
     */
    private Double latitude;

    /**
     * 경도 (Longitude)
     *
     * 지도 표시용
     */
    private Double longitude;

    // ========================================================================
    // 일시 및 인원 정보
    // ========================================================================

    /**
     * 정모 일시 (필수)
     *
     * @Future: 현재 시간 이후의 날짜만 허용
     *
     * 프론트엔드에서 ISO 8601 형식으로 전송:
     * "2024-05-04T10:00:00"
     */
    @NotNull(message = "정모 일시는 필수입니다.")
    @Future(message = "정모 일시는 현재 시간 이후여야 합니다.")
    private LocalDateTime meetingDate;

    /**
     * 최대 참석 인원 (기본값: 20)
     *
     * @Min(2): 최소 2명 (정모는 혼자 할 수 없음)
     * @Max(500): 최대 500명 (시스템 제한)
     *
     * 정원이 차면 추가 참석 신청 불가
     */
    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다.")
    @Max(value = 500, message = "최대 인원은 500명 이하여야 합니다.")
    @Builder.Default
    private int maxAttendees = 20;

    /**
     * 참가비 (기본값: 0, 무료)
     *
     * @Min(0): 음수 불가
     *
     * 단위: 원
     * 0이면 무료 정모
     */
    @Min(value = 0, message = "참가비는 0원 이상이어야 합니다.")
    @Builder.Default
    private int fee = 0;
}
