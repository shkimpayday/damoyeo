package com.damoyeo.api.domain.event.entity;

import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이벤트/배너(Event) 엔티티
 *
 * 메인 페이지 상단에 표시되는 이벤트 배너 정보를 저장합니다.
 * 프로모션, 공지사항, 특별 이벤트 등을 관리합니다.
 *
 * [이벤트 유형] (EventType)
 * - PROMOTION: 프로모션/할인 이벤트
 * - NOTICE: 공지사항
 * - SPECIAL: 특별 이벤트 (설날, 크리스마스 등)
 * - FEATURE: 기능 소개
 *
 * - EventController: 배너 목록 조회, 상세 조회
 * - 프론트엔드 메인 페이지 배너 슬라이더
 *
 * [프론트엔드 UI]
 * ┌─────────────────────────────────────┐
 * │  [배너 이미지]                        │
 * │  신규 가입 이벤트                     │
 * │  프리미엄 30일 무료!                  │
 * │                          ● ○ ○ ○ ○ │
 * └─────────────────────────────────────┘
 *
 * [DB 테이블]
 * event 테이블
 */
@Entity
@Table(name = "event")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Event extends BaseEntity {

    /**
     * 이벤트 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트 제목
     *
     * 배너에 표시되는 짧은 제목
     * 예: "신규 가입 이벤트", "친구 초대 이벤트"
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 이벤트 간략 설명
     *
     * 배너에 표시되는 부가 설명
     * 예: "프리미엄 30일 무료!", "포인트 최대 25,000P"
     */
    @Column(length = 200)
    private String description;

    /**
     * 이벤트 상세 내용
     *
     * 이벤트 상세 페이지에 표시되는 본문 내용
     * Markdown 형식 지원
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 배너 이미지 URL
     *
     * 메인 페이지 배너 슬라이더에 표시되는 이미지
     * 권장 비율: 2:1 (800x400)
     */
    @Column(nullable = false, length = 500)
    private String imageUrl;

    /**
     * 이벤트 상세 페이지 링크
     *
     * 배너 클릭 시 이동할 URL
     * 내부 링크: "/events/1"
     * 외부 링크: "https://example.com"
     */
    @Column(length = 500)
    private String linkUrl;

    /**
     * 이벤트 유형
     *
     * EventType enum 값을 문자열로 저장
     * 프론트엔드에서 배너 스타일 분기에 활용
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventType type = EventType.PROMOTION;

    /**
     * 이벤트 시작일시
     *
     * 이 시점부터 배너가 노출됨
     */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /**
     * 이벤트 종료일시
     *
     * 이 시점 이후 배너가 노출되지 않음
     */
    @Column(nullable = false)
    private LocalDateTime endDate;

    /**
     * 활성화 여부
     *
     * false면 기간에 상관없이 노출되지 않음
     * 관리자가 수동으로 비활성화할 때 사용
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    /**
     * 표시 순서
     *
     * 배너 슬라이더에서의 표시 순서
     * 숫자가 작을수록 먼저 표시
     */
    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    /**
     * 태그 목록
     *
     * 쉼표로 구분된 태그 문자열
     * 예: "신규가입,프리미엄,무료체험"
     */
    @Column(length = 500)
    private String tags;

    // 변경 메서드

    /**
     * 이벤트 활성화/비활성화 토글
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * 이벤트 정보 수정
     */
    public void update(String title, String description, String content,
                       String imageUrl, String linkUrl, EventType type,
                       LocalDateTime startDate, LocalDateTime endDate,
                       int displayOrder, String tags) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.displayOrder = displayOrder;
        this.tags = tags;
    }

    // 유틸리티 메서드

    /**
     * 현재 시점에 노출 가능한지 확인
     *
     * @return 활성화되어 있고, 현재 시간이 시작일과 종료일 사이면 true
     */
    public boolean isDisplayable() {
        if (!isActive) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }
}
