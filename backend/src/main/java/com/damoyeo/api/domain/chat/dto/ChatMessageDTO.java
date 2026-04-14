package com.damoyeo.api.domain.chat.dto;

import com.damoyeo.api.domain.chat.entity.MessageType;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 *
 * 채팅 메시지 정보를 프론트엔드에 전달하기 위한 데이터 전송 객체입니다.
 *
 * [Entity → DTO 변환 이유]
 * 1. 순환 참조 방지 (Entity의 양방향 관계 → JSON 직렬화 시 무한 루프)
 * 2. 필요한 정보만 선택적으로 노출 (group 전체 정보 대신 groupId만)
 * 3. 발신자 정보를 중첩 객체(MemberSummaryDTO)로 반환 (RESTful)
 *
 * - ChatController: REST API 응답 (메시지 히스토리)
 * - ChatController.sendMessage: WebSocket 브로드캐스트
 * - ChatServiceImpl: Entity를 DTO로 변환
 *
 * [프론트엔드 응답 예시]
 * {
 *   "id": 123,
 *   "groupId": 1,
 *   "sender": { "id": 5, "nickname": "홍길동", "profileImage": "..." },
 *   "message": "안녕하세요!",
 *   "messageType": "TEXT",
 *   "imageUrl": null,
 *   "createdAt": "2025-02-25T10:30:00"
 * }
 *
 * [SYSTEM 메시지 예시]
 * {
 *   "id": 124,
 *   "groupId": 1,
 *   "sender": null,  // SYSTEM 메시지는 발신자가 없음
 *   "message": "홍길동님이 입장했습니다",
 *   "messageType": "SYSTEM",
 *   "createdAt": "2025-02-25T10:31:00"
 * }
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    // 기본 정보

    /**
     * 메시지 ID (PK)
     *
     * WebSocket으로 수신한 메시지를 식별하는 데 사용됩니다.
     * 읽음 상태 업데이트 시 lastReadMessageId로 활용됩니다.
     */
    private Long id;

    /**
     * 소속 모임 ID
     *
     * Group 엔티티 전체를 반환하지 않고 ID만 반환합니다.
     * 프론트엔드는 이미 모임 정보를 알고 있으므로 ID만으로 충분합니다.
     * 정모 채팅인 경우 null입니다.
     */
    private Long groupId;

    /**
     * 소속 정모 ID
     *
     * 정모 채팅인 경우에만 값이 설정됩니다.
     * 모임 채팅인 경우 null입니다.
     */
    private Long meetingId;

    // 발신자 정보 (중첩 객체)

    /**
     * 발신자 정보
     *
     * MemberSummaryDTO: { id, nickname, profileImage }
     * SYSTEM 메시지인 경우 null입니다.
     *
     * [중첩 객체 패턴]
     * senderId만 반환하는 대신 발신자 정보를 포함하여
     * 프론트엔드에서 추가 API 호출 없이 바로 표시할 수 있습니다.
     */
    private MemberSummaryDTO sender;

    // 메시지 내용

    /**
     * 메시지 텍스트
     *
     * 최대 2000자
     * 예: "안녕하세요!", "다음 주 토요일 10시에 만나요"
     */
    private String message;

    /**
     * 메시지 타입
     *
     * - TEXT: 일반 텍스트 메시지
     * - IMAGE: 이미지 메시지 (Phase 3)
     * - SYSTEM: 시스템 메시지 (입장/퇴장 알림)
     */
    private MessageType messageType;

    /**
     * 이미지 URL (Phase 3 확장 기능)
     *
     * messageType이 IMAGE일 때 사용됩니다.
     * 예: "/uploads/chat/abc123.jpg"
     */
    private String imageUrl;

    // 시간 정보

    /**
     * 메시지 전송 시각
     *
     * 프론트엔드에서 상대 시간("5분 전")으로 표시됩니다.
     * ISO 8601 형식으로 직렬화됩니다: "2025-02-25T10:30:00"
     */
    private LocalDateTime createdAt;
}
