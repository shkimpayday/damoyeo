package com.damoyeo.api.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================================
 * 채팅방 정보 DTO
 * ============================================================================
 *
 * [역할]
 * 사용자의 채팅방 목록을 표시하기 위한 데이터 전송 객체입니다.
 * 각 채팅방(모임)의 기본 정보, 최신 메시지, 읽지 않은 메시지 개수를 포함합니다.
 *
 * [사용 위치]
 * - ChatController.getMyChatRooms: GET /api/chat/my-chats
 * - ChatServiceImpl.getMyChatRooms
 *
 * [프론트엔드 표시 예시]
 * ┌───────────────────────────────────┐
 * │ 🏃 강남 러닝 크루              [5] │ ← unread count
 * │ 홍길동: 다음 주 토요일 10시...    │ ← latest message
 * │ 5분 전                            │
 * └───────────────────────────────────┘
 *
 * [API 응답 예시]
 * [
 *   {
 *     "groupId": 1,
 *     "groupName": "강남 러닝 크루",
 *     "groupImage": "/uploads/groups/abc.jpg",
 *     "latestMessage": {
 *       "id": 123,
 *       "sender": { "nickname": "홍길동" },
 *       "message": "다음 주 토요일 10시에 만나요",
 *       "createdAt": "2025-02-25T10:30:00"
 *     },
 *     "unreadCount": 5
 *   },
 *   {
 *     "groupId": 2,
 *     "groupName": "판교 독서 모임",
 *     "latestMessage": null,  // 아직 메시지 없음
 *     "unreadCount": 0
 *   }
 * ]
 *
 * @author damoyeo
 * @since 2025-02-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {

    // ========================================================================
    // 모임 정보
    // ========================================================================

    /**
     * 모임 ID
     *
     * 채팅방 진입 시 사용됩니다.
     * 프론트엔드: /groups/{groupId}/chat
     */
    private Long groupId;

    /**
     * 모임 이름
     *
     * 예: "강남 러닝 크루", "판교 독서 모임"
     */
    private String groupName;

    /**
     * 모임 대표 이미지 URL
     *
     * 채팅방 목록에서 썸네일로 표시됩니다.
     * null이면 프론트엔드에서 기본 이미지 표시
     */
    private String groupImage;

    // ========================================================================
    // 최신 메시지 정보 (중첩 객체)
    // ========================================================================

    /**
     * 최신 메시지
     *
     * 채팅방 목록에서 미리보기로 표시됩니다.
     * 아직 메시지가 없으면 null입니다.
     *
     * [표시 예시]
     * - "홍길동: 안녕하세요"
     * - "시스템: 홍길동님이 입장했습니다"
     * - null → "아직 메시지가 없습니다"
     */
    private ChatMessageDTO latestMessage;

    // ========================================================================
    // 읽음 상태
    // ========================================================================

    /**
     * 읽지 않은 메시지 개수
     *
     * 채팅방 목록에서 배지로 표시됩니다.
     * 0이면 배지를 표시하지 않습니다.
     *
     * [계산 방식]
     * ChatRead.lastReadMessageId보다 큰 메시지의 개수
     */
    private int unreadCount;
}
