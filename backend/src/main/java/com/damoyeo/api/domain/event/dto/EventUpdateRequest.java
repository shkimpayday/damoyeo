package com.damoyeo.api.domain.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이벤트 수정 요청 DTO
 *
 * 관리자가 기존 이벤트를 수정할 때 사용하는 요청 DTO입니다.
 *
 * - EventController.updateEvent()
 * - 관리자 페이지에서 이벤트 수정 시
 *
 * [프론트엔드 요청 예시]
 * PUT /api/events/1
 * Authorization: Bearer {accessToken}
 * {
 *   "title": "수정된 이벤트 제목",
 *   "description": "수정된 설명",
 *   "imageUrl": "https://new-image.jpg",
 *   "linkUrl": "/events/1",
 *   "type": "NOTICE",
 *   "startDate": "2025-03-01T00:00:00",
 *   "endDate": "2025-03-31T23:59:59",
 *   "displayOrder": 2
 * }
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateRequest {

    /** 이벤트 제목 (필수) */
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    /** 이벤트 간략 설명 */
    private String description;

    /** 이벤트 상세 내용 (Markdown) */
    private String content;

    /** 배너 이미지 URL (필수) */
    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    /** 클릭 시 이동할 링크 */
    private String linkUrl;

    /** 이벤트 유형 */
    private String type;

    /** 이벤트 시작일시 (필수) */
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    /** 이벤트 종료일시 (필수) */
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

    /** 표시 순서 */
    private Integer displayOrder;

    /** 태그 (쉼표로 구분) */
    private String tags;
}
