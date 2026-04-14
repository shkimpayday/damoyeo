package com.damoyeo.api.domain.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 모임 수정 요청 DTO
 *
 * 모임 정보 수정 폼에서 전송된 데이터를 담는 객체입니다.
 *
 * [GroupCreateRequest와의 차이점]
 * - 모든 필드가 선택적(null 허용)
 * - null인 필드는 수정하지 않음 (기존 값 유지)
 * - @NotBlank, @NotNull이 없음
 *
 * [프론트엔드 요청 예시]
 * PUT /api/groups/{id}
 * {
 *   "name": "새로운 모임 이름",  // 이름만 변경
 *   "description": null,         // 기존 값 유지
 *   "maxMembers": 100            // 정원 변경
 * }
 *
 * - GroupController.modify()의 @RequestBody
 * - GroupServiceImpl.modify()에서 null 체크 후 선택적 업데이트
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupModifyRequest {

    /**
     * 모임 이름 (선택)
     *
     * null이면 기존 이름 유지
     */
    private String name;

    /**
     * 모임 소개 (선택)
     *
     * null이면 기존 소개 유지
     * 빈 문자열("")을 보내면 소개를 삭제할 수 있음
     */
    private String description;

    /**
     * 카테고리 ID (선택)
     *
     * null이면 기존 카테고리 유지
     */
    private Long categoryId;

    /**
     * 대표 이미지 URL (선택) - 기존 이미지 URL 유지용
     *
     * null이면 기존 이미지 유지
     */
    private String coverImage;

    /**
     * 대표 이미지 파일 (선택) - 새 이미지 업로드용
     *
     * MultipartFile로 파일을 직접 받습니다.
     */
    private MultipartFile coverImageFile;

    // 위치 정보

    /**
     * 모임 주소 (선택)
     *
     * 위치 정보 변경 시 location, latitude, longitude를 함께 전송하는 것이 좋습니다.
     */
    private String location;

    /** 위도 (선택) */
    private Double latitude;

    /** 경도 (선택) */
    private Double longitude;

    /**
     * 최대 멤버 수 (선택)
     *
     * Integer 타입인 이유:
     * - int는 null이 불가능하여 기본값 0이 전달됨
     * - Integer는 null 가능하여 "변경 안 함"을 표현 가능
     *
     * @Min/@Max: null이 아닌 경우에만 검증됩니다.
     *
     * [주의]
     * 현재 멤버 수보다 작은 값으로 변경하려 하면
     * GroupServiceImpl에서 예외를 발생시켜야 합니다. (비즈니스 규칙)
     */
    @Min(value = 2, message = "최소 인원은 2명 이상이어야 합니다.")
    @Max(value = 1000, message = "최대 인원은 1000명 이하여야 합니다.")
    private Integer maxMembers;

    /**
     * 공개 여부 (선택)
     *
     * Boolean 타입인 이유:
     * - boolean은 null이 불가능 (기본값 false가 전달됨)
     * - Boolean은 null 가능하여 "변경 안 함"을 표현 가능
     */
    private Boolean isPublic;
}
