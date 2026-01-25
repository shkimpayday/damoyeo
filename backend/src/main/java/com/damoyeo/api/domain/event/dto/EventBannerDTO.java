package com.damoyeo.api.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 이벤트 배너 응답 DTO (목록용)
 * ============================================================================
 *
 * [역할]
 * 메인 페이지 배너 슬라이더에 필요한 최소한의 정보만 전달합니다.
 * 상세 내용(content)은 제외하여 응답 크기를 줄입니다.
 *
 * [사용 위치]
 * - EventController.getBanners(): 배너 목록 조회
 * - 프론트엔드 BannerSlider 컴포넌트
 *
 * [프론트엔드 응답 예시]
 * {
 *   "id": 1,
 *   "title": "신규 가입 이벤트",
 *   "description": "프리미엄 30일 무료!",
 *   "imageUrl": "https://...",
 *   "linkUrl": "/events/1",
 *   "startDate": "2025-01-01T00:00:00",
 *   "endDate": "2025-01-31T23:59:59",
 *   "isActive": true
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventBannerDTO {

    /** 이벤트 ID (PK) */
    private Long id;

    /** 이벤트 제목 */
    private String title;

    /** 이벤트 간략 설명 */
    private String description;

    /** 배너 이미지 URL */
    private String imageUrl;

    /** 클릭 시 이동할 링크 */
    private String linkUrl;

    /** 이벤트 시작일시 */
    private LocalDateTime startDate;

    /** 이벤트 종료일시 */
    private LocalDateTime endDate;

    /** 활성화 여부 */
    private boolean isActive;
}
