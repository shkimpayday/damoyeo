package com.damoyeo.api.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 * 이벤트 상세 응답 DTO
 * ============================================================================
 *
 * [역할]
 * 이벤트 상세 페이지에 필요한 모든 정보를 전달합니다.
 * content(본문)와 tags(태그 목록)를 포함합니다.
 *
 * [사용 위치]
 * - EventController.getEventDetail(): 이벤트 상세 조회
 * - 프론트엔드 EventDetailPage 컴포넌트
 *
 * [프론트엔드 응답 예시]
 * {
 *   "id": 1,
 *   "title": "신규 가입 이벤트",
 *   "description": "프리미엄 30일 무료!",
 *   "content": "## 신규 가입 이벤트\n\n다모여에 처음 가입하시는 분들께...",
 *   "imageUrl": "https://...",
 *   "linkUrl": "/events/1",
 *   "type": "PROMOTION",
 *   "startDate": "2025-01-01T00:00:00",
 *   "endDate": "2025-01-31T23:59:59",
 *   "isActive": true,
 *   "tags": ["신규가입", "프리미엄", "무료체험"]
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailDTO {

    /** 이벤트 ID (PK) */
    private Long id;

    /** 이벤트 제목 */
    private String title;

    /** 이벤트 간략 설명 */
    private String description;

    /** 이벤트 상세 내용 (Markdown) */
    private String content;

    /** 배너 이미지 URL */
    private String imageUrl;

    /** 클릭 시 이동할 링크 */
    private String linkUrl;

    /** 이벤트 유형 (PROMOTION, NOTICE, SPECIAL, FEATURE) */
    private String type;

    /** 이벤트 시작일시 */
    private LocalDateTime startDate;

    /** 이벤트 종료일시 */
    private LocalDateTime endDate;

    /** 활성화 여부 */
    private boolean isActive;

    /** 태그 목록 */
    private List<String> tags;

    /** 생성일시 */
    private LocalDateTime createdAt;
}
