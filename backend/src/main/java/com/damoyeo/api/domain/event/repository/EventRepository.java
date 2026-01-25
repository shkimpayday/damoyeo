package com.damoyeo.api.domain.event.repository;

import com.damoyeo.api.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 * 이벤트 레포지토리
 * ============================================================================
 *
 * [역할]
 * Event 엔티티의 데이터베이스 접근을 담당합니다.
 * JpaRepository를 상속받아 기본 CRUD와 커스텀 쿼리를 제공합니다.
 *
 * [제공 기능]
 * - 활성화된 배너 목록 조회 (메인 페이지 슬라이더용)
 * - 전체 이벤트 목록 조회 (관리자용)
 *
 * [사용 위치]
 * - EventServiceImpl
 * - DataInitializer (초기 데이터 생성)
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * 현재 노출 가능한 활성 배너 목록 조회
     *
     * 메인 페이지 배너 슬라이더에 표시할 이벤트를 조회합니다.
     * 조건:
     * 1. isActive = true (활성화됨)
     * 2. startDate <= 현재시간 <= endDate (기간 내)
     *
     * displayOrder 오름차순으로 정렬합니다.
     *
     * [사용 위치]
     * EventServiceImpl.getActiveBanners()
     *
     * [프론트엔드 호출]
     * GET /api/events/banners
     *
     * @param now 현재 시간
     * @return 노출 가능한 이벤트 목록
     */
    @Query("SELECT e FROM Event e " +
           "WHERE e.isActive = true " +
           "AND e.startDate <= :now " +
           "AND e.endDate >= :now " +
           "ORDER BY e.displayOrder ASC")
    List<Event> findActiveBanners(@Param("now") LocalDateTime now);

    /**
     * 전체 이벤트 목록 조회 (표시순서 정렬)
     *
     * 관리자 페이지에서 모든 이벤트를 관리할 때 사용합니다.
     *
     * @return 전체 이벤트 목록 (displayOrder 순)
     */
    List<Event> findAllByOrderByDisplayOrderAsc();

    /**
     * 활성화된 이벤트 개수 조회
     *
     * @return 활성화된 이벤트 수
     */
    @Query("SELECT COUNT(e) FROM Event e " +
           "WHERE e.isActive = true " +
           "AND e.startDate <= :now " +
           "AND e.endDate >= :now")
    int countActiveBanners(@Param("now") LocalDateTime now);
}
