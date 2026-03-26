package com.damoyeo.api.domain.meeting.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 정모 수정 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 정모 수정 시 프론트엔드에서 전송하는 데이터를 담는 객체입니다.
 *
 * [Partial Update (부분 수정)]
 * 모든 필드가 선택 사항입니다.
 * null인 필드는 서비스에서 무시하고 기존 값을 유지합니다.
 *
 * [예시]
 * - 제목만 변경: { "title": "새 제목" }
 * - 일시와 장소 변경: { "meetingDate": "...", "location": "..." }
 * - 상태 변경 (취소): { "status": "CANCELLED" }
 *
 * [권한]
 * 정모 생성자 또는 모임 관리자(OWNER, MANAGER)만 수정 가능
 *
 * [사용 위치]
 * - MeetingController.modify()
 * - MeetingServiceImpl.modify()
 *
 * [프론트엔드 요청 예시]
 * PUT /api/meetings/123
 * {
 *   "title": "5월 첫째 주 러닝 (장소 변경)",
 *   "location": "서울 송파구 올림픽공원",
 *   "latitude": 37.5200,
 *   "longitude": 127.1200
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeetingModifyRequest {

    /**
     * 정모 제목 (선택)
     *
     * null이면 기존 값 유지
     */
    private String title;

    /**
     * 정모 설명 (선택)
     *
     * null이면 기존 값 유지
     */
    private String description;

    // ========================================================================
    // 위치 정보 (선택)
    // ========================================================================

    /**
     * 정모 장소 (주소 텍스트)
     *
     * null이면 기존 값 유지
     * 장소 변경 시 latitude, longitude도 함께 변경하는 것을 권장
     */
    private String address;

    /**
     * 위도 (Latitude)
     */
    private Double latitude;

    /**
     * 경도 (Longitude)
     */
    private Double longitude;

    // ========================================================================
    // 일시 및 인원 정보 (선택)
    // ========================================================================

    /**
     * 정모 일시 (선택)
     *
     * @Future: 값이 있을 경우 현재 시간 이후여야 함
     *
     * null이면 기존 값 유지
     * 일정 변경 시 참석자들에게 알림 발송 권장 (Phase 2)
     */
    @Future(message = "정모 일시는 현재 시간 이후여야 합니다.")
    private LocalDateTime meetingDate;

    /**
     * 최대 참석 인원 (선택)
     *
     * Integer 타입으로 null 허용 (null이면 기존 값 유지)
     *
     * [주의]
     * 현재 참석 인원보다 작게 변경하면 문제가 될 수 있음
     * (추후 검증 로직 추가 고려)
     */
    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다.")
    @Max(value = 500, message = "최대 인원은 500명 이하여야 합니다.")
    private Integer maxAttendees;

    /**
     * 참가비 (선택)
     *
     * Integer 타입으로 null 허용 (null이면 기존 값 유지)
     *
     * [주의]
     * 참가비 변경 시 기존 참석자들에게 알림 발송 권장
     */
    @Min(value = 0, message = "참가비는 0원 이상이어야 합니다.")
    private Integer fee;

    /**
     * 정모 상태 (선택)
     *
     * 사용 가능한 값: SCHEDULED, ONGOING, COMPLETED, CANCELLED
     *
     * [사용 예]
     * - 정모 취소: { "status": "CANCELLED" }
     * - 정모 시작: { "status": "ONGOING" }
     * - 정모 종료: { "status": "COMPLETED" }
     *
     * null이면 기존 값 유지
     */
    private String status;
}
