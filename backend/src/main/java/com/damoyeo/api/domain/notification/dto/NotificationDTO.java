package com.damoyeo.api.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림(Notification) 응답 DTO
 *
 * 알림 정보를 클라이언트에게 전달하는 데이터 전송 객체입니다.
 * Notification 엔티티를 프론트엔드 응답용으로 변환합니다.
 *
 * [왜 Entity를 직접 반환하지 않는가?]
 * 1. 순환 참조 방지 (Member → Notification → Member ...)
 * 2. 필요한 필드만 선택적으로 노출
 * 3. 내부 구조(Entity)와 API 응답 구조 분리
 * 4. Jackson 직렬화 시 LAZY 로딩 문제 방지
 *
 * - NotificationController의 응답 타입
 * - NotificationServiceImpl.entityToDTO()에서 변환
 *
 * [Lombok 어노테이션]
 * - @Data: Getter, Setter, toString, equals, hashCode 자동 생성
 * - @Builder: 빌더 패턴 지원 (NotificationDTO.builder().id(1).build())
 * - @AllArgsConstructor: 모든 필드 생성자
 * - @NoArgsConstructor: 기본 생성자 (Jackson 직렬화에 필요)
 *
 * [프론트엔드 응답 예시]
 * {
 *   "id": 1,
 *   "type": "NEW_MEMBER",
 *   "title": "새 멤버 가입",
 *   "message": "홍길동님이 강남 러닝 크루에 가입했습니다.",
 *   "relatedId": 5,
 *   "isRead": false,
 *   "createdAt": "2024-05-01T10:30:00"
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    /**
     * 알림 고유 ID
     *
     * [프론트엔드 활용]
     * - 개별 알림 읽음 처리 시 사용
     * - 알림 목록에서 key로 사용
     *
     * [API]
     * PATCH /api/notifications/{id}/read
     */
    private Long id;

    /**
     * 알림 유형
     *
     * [값 예시]
     * "NEW_MEETING", "MEETING_REMINDER", "MEETING_CANCELLED",
     * "NEW_MEMBER", "MEMBER_LEFT", "GROUP_UPDATE", "ROLE_CHANGED", "WELCOME"
     *
     * [프론트엔드 활용]
     * 알림 유형에 따라 아이콘, 색상, 클릭 동작을 다르게 처리
     *
     * [예시 코드]
     * const getNotificationIcon = (type: string) => {
     *   switch (type) {
     *     case 'NEW_MEMBER': return '👋';
     *     case 'NEW_MEETING': return '📅';
     *     case 'MEETING_CANCELLED': return '🚫';
     *     case 'WELCOME': return '🎉';
     *     default: return '🔔';
     *   }
     * };
     *
     * [참고]
     * Entity에서는 NotificationType enum이지만,
     * DTO에서는 String으로 변환하여 전달
     * → enum.name()으로 변환
     */
    private String type;

    /**
     * 알림 제목
     *
     * [예시]
     * - "새 멤버 가입"
     * - "새 정모가 등록되었습니다"
     * - "운영진으로 임명되었습니다"
     *
     * [프론트엔드 UI]
     * ┌─────────────────────────────────────┐
     * │ 👋 새 멤버 가입                     │ ← title
     * │    홍길동님이 강남 러닝 크루에 가입  │ ← message
     * │    2시간 전                          │ ← createdAt
     * └─────────────────────────────────────┘
     */
    private String title;

    /**
     * 알림 내용 (상세 메시지)
     *
     * [예시]
     * - "강남 러닝 크루에 가입되었습니다. 다음 정모를 확인해보세요!"
     * - "5월 첫째 주 러닝 정모가 등록되었습니다."
     * - "[강남 러닝 크루]에서 운영진으로 임명되었습니다."
     *
     * [특징]
     * - title보다 상세한 정보 제공
     * - 최대 500자 (Entity에서 @Column(length = 500))
     */
    private String content;

    /**
     * 관련 리소스 ID
     *
     * [유형별 값]
     * - JOIN_*: groupId (모임 ID)
     * - NEW_MEETING, MEETING_*: meetingId (정모 ID)
     * - NEW_MEMBER: groupId (모임 ID)
     * - GROUP_UPDATE: groupId (모임 ID)
     * - ROLE_CHANGED: groupId (모임 ID)
     *
     * [프론트엔드 활용]
     * 알림 클릭 시 해당 페이지로 이동
     */
    private Long referenceId;

    /**
     * 관련 리소스 타입
     *
     * [값 예시]
     * - "GROUP": 모임 관련 알림
     * - "MEETING": 정모 관련 알림
     * - "CHAT": 채팅 관련 알림
     *
     * [프론트엔드 활용]
     * referenceId와 함께 사용하여 이동할 페이지 결정
     */
    private String referenceType;

    /**
     * 읽음 여부
     *
     * [값]
     * - false: 읽지 않음 (기본값)
     * - true: 읽음
     *
     * [프론트엔드 활용]
     * 읽지 않은 알림은 강조 표시 (굵은 글씨, 배경색 등)
     *
     * [UI 예시]
     * ┌─────────────────────────────────────┐
     * │ ● 새 멤버 가입 (NEW!)              │ ← isRead = false
     * │    홍길동님이 강남 러닝 크루에 가입  │
     * ├─────────────────────────────────────┤
     * │ ○ 새 정모가 등록되었습니다          │ ← isRead = true
     * │    5월 첫째 주 러닝                 │
     * └─────────────────────────────────────┘
     *
     * [JSON 필드명 주의]
     * Java의 isRead → JSON에서 "isRead" (getter가 isIsRead가 아닌 isRead)
     * boolean 필드는 "is" prefix가 있어도 그대로 직렬화됨
     */
    @JsonProperty("isRead")
    private boolean isRead;

    /**
     * 알림 생성 시간
     *
     * [형식]
     * ISO 8601 형식: "2024-05-01T10:30:00"
     *
     * [프론트엔드 활용]
     * 상대 시간으로 변환하여 표시
     *
     * [예시 변환 코드]
     * const getRelativeTime = (dateString: string) => {
     *   const diff = Date.now() - new Date(dateString).getTime();
     *   const minutes = Math.floor(diff / 60000);
     *   const hours = Math.floor(minutes / 60);
     *   const days = Math.floor(hours / 24);
     *
     *   if (minutes < 1) return '방금 전';
     *   if (minutes < 60) return `${minutes}분 전`;
     *   if (hours < 24) return `${hours}시간 전`;
     *   if (days < 7) return `${days}일 전`;
     *   return dateString.split('T')[0];  // 날짜만 표시
     * };
     *
     * [UI 표시]
     * - "방금 전"
     * - "5분 전"
     * - "2시간 전"
     * - "어제"
     * - "2024-05-01"
     */
    private LocalDateTime createdAt;
}
