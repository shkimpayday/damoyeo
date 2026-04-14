package com.damoyeo.api.domain.event.service;

import com.damoyeo.api.domain.event.dto.EventBannerDTO;
import com.damoyeo.api.domain.event.dto.EventCreateRequest;
import com.damoyeo.api.domain.event.dto.EventDetailDTO;
import com.damoyeo.api.domain.event.dto.EventUpdateRequest;
import com.damoyeo.api.domain.event.entity.Event;
import com.damoyeo.api.domain.event.entity.EventType;
import com.damoyeo.api.domain.event.repository.EventRepository;
import com.damoyeo.api.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 이벤트 서비스 구현체
 *
 * EventService 인터페이스의 실제 구현을 담당합니다.
 * 이벤트 관련 비즈니스 로직을 처리합니다.
 *
 * - 활성 배너 목록 조회 (메인 페이지용)
 * - 이벤트 상세 조회 (상세 페이지용)
 * - 이벤트 CRUD (관리자용)
 *
 * - 클래스 레벨 @Transactional: 모든 메서드에 트랜잭션 적용
 * - 조회 메서드: @Transactional(readOnly = true)로 성능 최적화
 *
 * - EventController에서 주입받아 사용
 *
 * [Spring 어노테이션 설명]
 * - @Service: 서비스 계층 빈으로 등록
 * - @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * - @Transactional: 트랜잭션 관리
 * - @Slf4j: 로깅
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {

    // 의존성 주입 (DI)

    /** 이벤트 레포지토리 */
    private final EventRepository eventRepository;

    // 배너 조회 (공개 API)

    /**
     * 활성 배너 목록 조회
     *
     * 메인 페이지 배너 슬라이더에 표시할 이벤트 목록을 조회합니다.
     * 현재 시점에 노출 가능한(isActive=true, 기간 내) 이벤트만 반환합니다.
     *
     * @return 활성 배너 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventBannerDTO> getActiveBanners() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findActiveBanners(now);

        return events.stream()
                .map(this::toEventBannerDTO)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 상세 조회
     *
     * 특정 이벤트의 상세 정보를 조회합니다.
     * 이벤트 상세 페이지에서 사용합니다.
     *
     * @param eventId 이벤트 ID
     * @return 이벤트 상세 정보
     * @throws CustomException 이벤트가 존재하지 않으면 404 에러
     */
    @Override
    @Transactional(readOnly = true)
    public EventDetailDTO getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> CustomException.notFound("이벤트를 찾을 수 없습니다."));

        return toEventDetailDTO(event);
    }

    // 관리자용 API

    /**
     * 전체 이벤트 목록 조회 (관리자용)
     *
     * @return 전체 이벤트 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventDetailDTO> getAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByDisplayOrderAsc();

        return events.stream()
                .map(this::toEventDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 생성 (관리자용)
     *
     * @param request 이벤트 생성 요청 DTO
     * @return 생성된 이벤트 ID
     */
    @Override
    public Long createEvent(EventCreateRequest request) {
        // 이벤트 타입 변환 (기본값: PROMOTION)
        EventType eventType = EventType.PROMOTION;
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                eventType = EventType.valueOf(request.getType());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid event type: {}, using default PROMOTION", request.getType());
            }
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .type(eventType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .tags(request.getTags())
                .isActive(true)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event created: id={}, title={}", saved.getId(), saved.getTitle());

        return saved.getId();
    }

    /**
     * 이벤트 수정 (관리자용)
     *
     * <p>수정 프로세스:</p>
     * <ol>
     *   <li>이벤트 존재 여부 확인</li>
     *   <li>이벤트 타입 유효성 검사 (기존 타입 유지 가능)</li>
     *   <li>엔티티의 update() 메서드로 필드 업데이트</li>
     *   <li>수정된 이벤트를 DTO로 변환하여 반환</li>
     * </ol>
     *
     * @param eventId 이벤트 ID
     * @param request 이벤트 수정 요청 DTO
     * @return 수정된 이벤트 상세 정보
     * @throws CustomException 이벤트가 존재하지 않으면 404 에러
     */
    @Override
    public EventDetailDTO updateEvent(Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> CustomException.notFound("이벤트를 찾을 수 없습니다."));

        // 이벤트 타입 변환 (요청에 없으면 기존 타입 유지)
        EventType eventType = event.getType();
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                eventType = EventType.valueOf(request.getType());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid event type: {}, keeping existing type: {}", request.getType(), eventType);
            }
        }

        event.update(
                request.getTitle(),
                request.getDescription(),
                request.getContent(),
                request.getImageUrl(),
                request.getLinkUrl(),
                eventType,
                request.getStartDate(),
                request.getEndDate(),
                request.getDisplayOrder() != null ? request.getDisplayOrder() : event.getDisplayOrder(),
                request.getTags()
        );

        log.info("Event updated: id={}, title={}", eventId, event.getTitle());
        return toEventDetailDTO(event);
    }

    /**
     * 이벤트 삭제 (관리자용)
     *
     * @param eventId 이벤트 ID
     * @throws CustomException 이벤트가 존재하지 않으면 404 에러
     */
    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> CustomException.notFound("이벤트를 찾을 수 없습니다."));

        eventRepository.delete(event);
        log.info("Event deleted: id={}, title={}", eventId, event.getTitle());
    }

    /**
     * 이벤트 활성화/비활성화 토글 (관리자용)
     *
     * @param eventId 이벤트 ID
     * @throws CustomException 이벤트가 존재하지 않으면 404 에러
     */
    @Override
    public void toggleEventActive(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> CustomException.notFound("이벤트를 찾을 수 없습니다."));

        event.toggleActive();
        log.info("Event toggled: id={}, isActive={}", eventId, event.isActive());
    }

    // DTO 변환 메서드 (private)

    /**
     * Event 엔티티를 EventBannerDTO로 변환 (목록용)
     */
    private EventBannerDTO toEventBannerDTO(Event event) {
        return EventBannerDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .linkUrl(event.getLinkUrl())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isActive(event.isActive())
                .build();
    }

    /**
     * Event 엔티티를 EventDetailDTO로 변환 (상세용)
     */
    private EventDetailDTO toEventDetailDTO(Event event) {
        // 태그 문자열을 리스트로 변환
        List<String> tagList = Collections.emptyList();
        if (event.getTags() != null && !event.getTags().isEmpty()) {
            tagList = Arrays.asList(event.getTags().split(","));
        }

        return EventDetailDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .content(event.getContent())
                .imageUrl(event.getImageUrl())
                .linkUrl(event.getLinkUrl())
                .type(event.getType().name())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .isActive(event.isActive())
                .tags(tagList)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
