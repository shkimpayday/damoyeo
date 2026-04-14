package com.damoyeo.api.domain.event.entity;

/**
 * 이벤트 유형 Enum
 *
 * 이벤트의 종류를 구분합니다.
 * 프론트엔드에서 배너 스타일이나 아이콘을 분기하는 데 활용됩니다.
 *
 * - Event 엔티티의 type 필드
 * - EventDTO의 type 필드
 *
 * [프론트엔드 활용 예시]
 * switch(event.type) {
 *   case 'PROMOTION': return <PromoBanner />;
 *   case 'NOTICE': return <NoticeBanner />;
 *   ...
 * }
 */
public enum EventType {

    /**
     * 프로모션/할인 이벤트
     *
     * 신규 가입 혜택, 포인트 이벤트, 할인 쿠폰 등
     * 아이콘: 🎁
     */
    PROMOTION,

    /**
     * 공지사항
     *
     * 서비스 업데이트, 점검 안내, 정책 변경 등
     * 아이콘: 📢
     */
    NOTICE,

    /**
     * 특별 이벤트
     *
     * 설날, 크리스마스 등 시즌 이벤트
     * 아이콘: 🎉
     */
    SPECIAL,

    /**
     * 기능 소개
     *
     * 새로운 기능 안내, 사용 가이드 등
     * 아이콘: ✨
     */
    FEATURE
}
