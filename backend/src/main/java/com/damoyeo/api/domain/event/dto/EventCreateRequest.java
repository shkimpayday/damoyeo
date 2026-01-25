package com.damoyeo.api.domain.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 이벤트 생성 요청 DTO
 * ============================================================================
 *
 * [역할]
 * 관리자가 새 이벤트를 생성할 때 사용하는 요청 DTO입니다.
 *
 * [사용 위치]
 * - EventController.createEvent()
 * - 관리자 페이지에서 이벤트 등록 시
 *
 * [프론트엔드 요청 예시]
 * POST /api/events
 * {
 *   "title": "신규 가입 이벤트",
 *   "description": "프리미엄 30일 무료!",
 *   "content": "## 신규 가입 이벤트\n\n...",
 *   "imageUrl": "https://...",
 *   "linkUrl": "/events/1",
 *   "type": "PROMOTION",
 *   "startDate": "2025-01-01T00:00:00",
 *   "endDate": "2025-01-31T23:59:59",
 *   "displayOrder": 1,
 *   "tags": "신규가입,프리미엄,무료체험"
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequest {

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

    /** 이벤트 유형 (기본값: PROMOTION) */
    private String type;

    /** 이벤트 시작일시 (필수) */
    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    /** 이벤트 종료일시 (필수) */
    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

    /** 표시 순서 (기본값: 0) */
    private Integer displayOrder;

    /** 태그 (쉼표로 구분) */
    private String tags;
}
