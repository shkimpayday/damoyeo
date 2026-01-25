package com.damoyeo.api.domain.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 모임 생성 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 모임 생성 폼에서 전송된 데이터를 담는 객체입니다.
 *
 * [프론트엔드 요청 예시]
 * POST /api/groups
 * {
 *   "name": "강남 러닝 크루",
 *   "description": "매주 토요일 아침 한강에서 러닝합니다",
 *   "categoryId": 1,
 *   "location": "서울특별시 강남구",
 *   "latitude": 37.4979,
 *   "longitude": 127.0276,
 *   "maxMembers": 30,
 *   "isPublic": true
 * }
 *
 * [유효성 검사]
 * @Valid와 함께 사용하면 자동으로 검증됩니다.
 * 검증 실패 시 GlobalExceptionHandler에서 에러 응답을 생성합니다.
 *
 * [사용 위치]
 * - GroupController.create()의 @RequestBody
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateRequest {

    /**
     * 모임 이름 (필수)
     *
     * @NotBlank: null, 빈 문자열, 공백만 있는 문자열 모두 불가
     *
     * 예: "강남 러닝 크루"
     */
    @NotBlank(message = "모임 이름은 필수입니다.")
    private String name;

    /**
     * 모임 소개 (선택)
     *
     * null 허용 - 나중에 수정 가능
     */
    private String description;

    /**
     * 카테고리 ID (필수)
     *
     * DataInitializer에서 생성된 18개 카테고리 중 하나를 선택해야 합니다.
     * 프론트엔드에서 카테고리 목록을 조회한 후 선택합니다.
     *
     * @NotNull: null 불가 (카테고리 선택은 필수)
     */
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    /**
     * 대표 이미지 URL (선택)
     *
     * 이미지 업로드 후 받은 URL을 전달합니다.
     * null이면 프론트엔드에서 기본 이미지를 표시합니다.
     */
    private String coverImage;

    // ========================================================================
    // 위치 정보 (모두 선택)
    // ========================================================================
    // 위치 기반 검색을 위해 위도/경도를 저장합니다.
    // 프론트엔드에서 지도 API(카카오맵 등)를 통해 값을 얻습니다.

    /** 모임 주소 (사람이 읽을 수 있는 형태) */
    private String location;

    /** 위도 */
    private Double latitude;

    /** 경도 */
    private Double longitude;

    /**
     * 최대 멤버 수 (기본값: 50)
     *
     * @Min(2): 모임은 최소 2명 이상
     * @Max(1000): 너무 큰 모임은 관리가 어려우므로 제한
     *
     * @Builder.Default: Builder 패턴 사용 시에도 기본값 적용
     */
    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다.")
    @Max(value = 1000, message = "최대 인원은 1000명 이하여야 합니다.")
    @Builder.Default
    private int maxMembers = 50;

    /**
     * 공개 여부 (기본값: true)
     *
     * true: 검색 결과에 노출, 누구나 가입 신청 가능
     * false: 초대받은 사람만 접근 가능 (Phase 2에서 구현 예정)
     */
    @Builder.Default
    private boolean isPublic = true;
}
