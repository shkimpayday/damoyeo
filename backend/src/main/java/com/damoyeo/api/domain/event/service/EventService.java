package com.damoyeo.api.domain.event.service;

import com.damoyeo.api.domain.event.dto.EventBannerDTO;
import com.damoyeo.api.domain.event.dto.EventCreateRequest;
import com.damoyeo.api.domain.event.dto.EventDetailDTO;

import java.util.List;

/**
 * ============================================================================
 * 이벤트 서비스 인터페이스
 * ============================================================================
 *
 * [역할]
 * 이벤트 관련 비즈니스 로직의 계약(contract)을 정의합니다.
 *
 * [왜 인터페이스를 분리하는가?]
 * 1. 구현과 계약 분리: 나중에 다른 구현체로 교체 가능
 * 2. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 3. 의존성 역전: Controller는 인터페이스에만 의존
 *
 * [기능 분류]
 * - 배너 목록 조회: 메인 페이지 슬라이더용
 * - 이벤트 상세 조회: 이벤트 상세 페이지용
 * - 이벤트 생성/수정/삭제: 관리자용 (Phase 2)
 *
 * [사용 위치]
 * - EventController에서 주입받아 사용
 * - EventServiceImpl에서 구현
 */
public interface EventService {

    /**
     * 활성 배너 목록 조회
     *
     * 메인 페이지 배너 슬라이더에 표시할 이벤트 목록을 조회합니다.
     * 현재 시점에 노출 가능한(isActive=true, 기간 내) 이벤트만 반환합니다.
     *
     * @return 활성 배너 목록
     *
     * Controller: GET /api/events/banners
     */
    List<EventBannerDTO> getActiveBanners();

    /**
     * 이벤트 상세 조회
     *
     * 특정 이벤트의 상세 정보를 조회합니다.
     * 이벤트 상세 페이지에서 사용합니다.
     *
     * @param eventId 이벤트 ID
     * @return 이벤트 상세 정보
     *
     * Controller: GET /api/events/{eventId}
     */
    EventDetailDTO getEventDetail(Long eventId);

    /**
     * 전체 이벤트 목록 조회 (관리자용)
     *
     * 관리자 페이지에서 모든 이벤트를 관리할 때 사용합니다.
     * 활성화 여부, 기간과 관계없이 모든 이벤트를 반환합니다.
     *
     * @return 전체 이벤트 목록
     *
     * Controller: GET /api/admin/events
     */
    List<EventDetailDTO> getAllEvents();

    /**
     * 이벤트 생성 (관리자용)
     *
     * 새로운 이벤트를 생성합니다.
     *
     * @param request 이벤트 생성 요청 DTO
     * @return 생성된 이벤트 ID
     *
     * Controller: POST /api/admin/events
     */
    Long createEvent(EventCreateRequest request);

    /**
     * 이벤트 삭제 (관리자용)
     *
     * 특정 이벤트를 삭제합니다.
     *
     * @param eventId 이벤트 ID
     *
     * Controller: DELETE /api/admin/events/{eventId}
     */
    void deleteEvent(Long eventId);

    /**
     * 이벤트 활성화/비활성화 토글 (관리자용)
     *
     * 이벤트의 활성화 상태를 토글합니다.
     *
     * @param eventId 이벤트 ID
     *
     * Controller: PATCH /api/admin/events/{eventId}/toggle
     */
    void toggleEventActive(Long eventId);
}
