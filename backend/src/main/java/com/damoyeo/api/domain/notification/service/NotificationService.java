package com.damoyeo.api.domain.notification.service;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.notification.dto.NotificationDTO;
import com.damoyeo.api.domain.notification.entity.NotificationType;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

/**
 * ============================================================================
 * 알림 서비스 인터페이스
 * ============================================================================
 *
 * [역할]
 * 알림 관련 비즈니스 로직의 계약(contract)을 정의합니다.
 *
 * [왜 인터페이스를 분리하는가?]
 * 1. 구현과 계약 분리: 나중에 다른 구현체로 교체 가능
 *    (예: 이메일 알림, 푸시 알림 등 확장)
 * 2. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 3. 의존성 역전: Controller는 인터페이스에만 의존
 *
 * [기능 분류]
 * - 알림 발송: 다른 서비스에서 호출하여 알림 생성
 * - 알림 조회: 사용자별 알림 목록 조회
 * - 읽음 처리: 개별/전체 알림 읽음 처리
 *
 * [사용 위치]
 * - NotificationController에서 주입받아 사용
 * - NotificationServiceImpl에서 구현
 * - GroupServiceImpl, MeetingServiceImpl 등에서 알림 발송 시 사용
 */
public interface NotificationService {

    /**
     * 알림 발송
     *
     * 새로운 알림을 생성하고 저장합니다.
     * 다른 서비스(GroupService, MeetingService 등)에서 호출합니다.
     *
     * [사용 예시]
     * // 새 멤버 가입 시
     * notificationService.send(
     *     owner,
     *     NotificationType.NEW_MEMBER,
     *     "새 멤버 가입",
     *     "홍길동님이 강남 러닝 크루에 가입했습니다.",
     *     groupId
     * );
     *
     * @param member 알림을 받을 회원
     * @param type 알림 유형 (NotificationType enum)
     * @param title 알림 제목
     * @param message 알림 내용
     * @param relatedId 관련 리소스 ID (group, meeting 등)
     *
     * 호출 위치: GroupServiceImpl, MeetingServiceImpl 등
     */
    void send(Member member, NotificationType type, String title, String message, Long relatedId);

    /**
     * 알림 목록 조회 (페이지네이션)
     *
     * 현재 로그인한 사용자의 알림 목록을 조회합니다.
     * 최신순으로 정렬됩니다.
     *
     * @param email 사용자 이메일
     * @param pageRequestDTO 페이지 정보 (page, size)
     * @return 페이지네이션된 알림 목록
     *
     * Controller: GET /api/notifications?page=1&size=10
     */
    PageResponseDTO<NotificationDTO> getNotifications(String email, PageRequestDTO pageRequestDTO);

    /**
     * 읽지 않은 알림 개수 조회
     *
     * 현재 로그인한 사용자의 읽지 않은 알림 개수를 반환합니다.
     *
     * @param email 사용자 이메일
     * @return 읽지 않은 알림 개수
     *
     * [프론트엔드 UI]
     * 헤더의 알림 벨 아이콘에 배지로 표시
     *
     * Controller: GET /api/notifications/unread/count
     */
    int getUnreadCount(String email);

    /**
     * 개별 알림 읽음 처리
     *
     * 특정 알림을 읽음으로 표시합니다.
     *
     * [권한 확인]
     * 본인의 알림만 읽음 처리 가능
     *
     * @param notificationId 알림 ID
     * @param email 요청자 이메일 (권한 확인용)
     *
     * Controller: PATCH /api/notifications/{id}/read
     */
    void markAsRead(Long notificationId, String email);

    /**
     * 모든 알림 읽음 처리
     *
     * 현재 사용자의 모든 알림을 읽음으로 표시합니다.
     *
     * @param email 사용자 이메일
     *
     * Controller: PATCH /api/notifications/read-all
     */
    void markAllAsRead(String email);


    /**
     * 개별 알림 삭제 처리
     *
     * [권한 확인]
     * 본인의 알림만 삭제 처리 가능
     *
     * @param notificationId 알림 ID
     * @param email 요청자 이메일 (권한 확인용)
     *
     * Controller: PATCH /api/notifications/{id}/delete
     */
    void delete(Long notificationId, String email);
}
