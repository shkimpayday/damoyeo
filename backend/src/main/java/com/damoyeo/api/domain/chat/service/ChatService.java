package com.damoyeo.api.domain.chat.service;

import com.damoyeo.api.domain.chat.dto.ChatMessageDTO;
import com.damoyeo.api.domain.chat.dto.ChatRoomDTO;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

import java.util.List;

/**
 * ============================================================================
 * 채팅 서비스 인터페이스
 * ============================================================================
 *
 * [역할]
 * 채팅 관련 비즈니스 로직의 계약(contract)을 정의합니다.
 *
 * [왜 인터페이스를 분리하는가?]
 * 1. 구현과 계약 분리: 나중에 다른 구현체로 교체 가능
 * 2. 테스트 용이성: Mock 객체로 쉽게 대체 가능
 * 3. 의존성 역전: Controller는 인터페이스에만 의존
 *
 * [기능 분류]
 * - 메시지 관리: 전송, 조회
 * - 읽음 상태: unread count 조회, 읽음 처리
 * - 채팅방 목록: 내가 속한 채팅방 조회
 *
 * [사용 위치]
 * - ChatController에서 주입받아 사용
 * - ChatServiceImpl에서 구현
 *
 * @author damoyeo
 * @since 2025-02-25
 */
public interface ChatService {

    // ========================================================================
    // 메시지 관리
    // ========================================================================

    /**
     * 채팅 메시지 전송
     *
     * [처리 흐름]
     * 1. 발신자 검증 (존재하는 회원인지)
     * 2. 모임 검증 (존재하고 활성 상태인지)
     * 3. 멤버 권한 검증 (모임 멤버인지, 강퇴 상태가 아닌지)
     * 4. 메시지 저장 (ChatMessage 엔티티)
     * 5. DTO 변환 후 반환 (WebSocket으로 브로드캐스트됨)
     *
     * @param groupId 모임 ID
     * @param email 발신자 이메일
     * @param message 메시지 내용
     * @return 저장된 메시지 정보
     * @throws CustomException 모임이 없거나, 멤버가 아니거나, 강퇴된 경우
     *
     * Controller: @MessageMapping("/chat/{groupId}")
     */
    ChatMessageDTO sendMessage(Long groupId, String email, String message);

    /**
     * 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 채팅방 진입 시 최근 메시지를 로드합니다.
     * 무한 스크롤로 과거 메시지를 추가 로드할 수 있습니다.
     *
     * [정렬]
     * createdAt DESC: 최신 메시지부터 반환
     *
     * [권한 검증]
     * 요청한 사용자가 해당 모임의 멤버인지 확인합니다.
     *
     * @param groupId 모임 ID
     * @param email 요청자 이메일
     * @param pageRequest 페이지 정보 (page, size)
     * @return 페이지네이션된 메시지 목록
     * @throws CustomException 모임 멤버가 아닌 경우
     *
     * Controller: GET /api/chat/{groupId}/messages?page=0&size=50
     */
    PageResponseDTO<ChatMessageDTO> getMessages(Long groupId, String email, PageRequestDTO pageRequest);

    // ========================================================================
    // 읽음 상태 관리
    // ========================================================================

    /**
     * 읽지 않은 메시지 개수 조회
     *
     * [계산 방식]
     * ChatRead.lastReadMessageId보다 큰 ID를 가진 메시지의 개수
     *
     * [예시]
     * - lastReadMessageId = 100
     * - 최신 메시지 ID = 105
     * - 결과: 5개 (101, 102, 103, 104, 105)
     *
     * @param groupId 모임 ID
     * @param email 요청자 이메일
     * @return 읽지 않은 메시지 개수
     *
     * Controller: GET /api/chat/{groupId}/unread-count
     */
    int getUnreadCount(Long groupId, String email);

    /**
     * 메시지 읽음 처리
     *
     * [처리 흐름]
     * 1. ChatRead 엔티티 조회 (없으면 새로 생성)
     * 2. lastReadMessageId 업데이트
     * 3. lastReadAt 현재 시각으로 업데이트
     *
     * [호출 시점]
     * - 채팅방 진입 시: 최신 메시지 ID로 업데이트
     * - 채팅방에서 메시지 수신 시: 실시간으로 읽음 처리
     *
     * @param groupId 모임 ID
     * @param email 요청자 이메일
     * @param messageId 마지막으로 읽은 메시지 ID
     *
     * Controller: POST /api/chat/{groupId}/read
     */
    void markAsRead(Long groupId, String email, Long messageId);

    // ========================================================================
    // 채팅방 목록
    // ========================================================================

    /**
     * 내가 속한 채팅방 목록 조회
     *
     * [반환 정보]
     * - 모임 기본 정보 (ID, 이름, 이미지)
     * - 최신 메시지 (미리보기)
     * - 읽지 않은 메시지 개수
     *
     * [정렬]
     * 최신 메시지가 있는 순서대로 정렬
     *
     * [사용 위치]
     * 채팅 탭의 채팅방 목록 화면
     *
     * @param email 요청자 이메일
     * @return 내 채팅방 목록 (모임별)
     *
     * Controller: GET /api/chat/my-chats
     */
    List<ChatRoomDTO> getMyChatRooms(String email);

    // ========================================================================
    // 정모 채팅 (참석자 전용)
    // ========================================================================

    /**
     * 정모 채팅 메시지 전송
     *
     * [처리 흐름]
     * 1. 발신자 검증 (존재하는 회원인지)
     * 2. 정모 검증 (존재하고 예정/진행 중 상태인지)
     * 3. 참석자 권한 검증 (ATTENDING 상태인지)
     * 4. 메시지 저장 (ChatMessage 엔티티, meeting_id 설정)
     * 5. DTO 변환 후 반환 (WebSocket으로 브로드캐스트됨)
     *
     * @param meetingId 정모 ID
     * @param email 발신자 이메일
     * @param message 메시지 내용
     * @return 저장된 메시지 정보
     * @throws CustomException 정모가 없거나, 참석자가 아닌 경우
     *
     * Controller: @MessageMapping("/meeting-chat/{meetingId}")
     */
    ChatMessageDTO sendMeetingMessage(Long meetingId, String email, String message);

    /**
     * 정모 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 정모 채팅방 진입 시 최근 메시지를 로드합니다.
     *
     * [권한 검증]
     * 요청한 사용자가 해당 정모의 참석자(ATTENDING)인지 확인합니다.
     *
     * @param meetingId 정모 ID
     * @param email 요청자 이메일
     * @param pageRequest 페이지 정보 (page, size)
     * @return 페이지네이션된 메시지 목록
     * @throws CustomException 정모 참석자가 아닌 경우
     *
     * Controller: GET /api/chat/meeting/{meetingId}/messages?page=0&size=50
     */
    PageResponseDTO<ChatMessageDTO> getMeetingMessages(Long meetingId, String email, PageRequestDTO pageRequest);

    /**
     * 정모 읽지 않은 메시지 개수 조회
     *
     * @param meetingId 정모 ID
     * @param email 요청자 이메일
     * @return 읽지 않은 메시지 개수
     *
     * Controller: GET /api/chat/meeting/{meetingId}/unread-count
     */
    int getMeetingUnreadCount(Long meetingId, String email);

    /**
     * 정모 메시지 읽음 처리
     *
     * @param meetingId 정모 ID
     * @param email 요청자 이메일
     * @param messageId 마지막으로 읽은 메시지 ID
     *
     * Controller: POST /api/chat/meeting/{meetingId}/read
     */
    void markMeetingAsRead(Long meetingId, String email, Long messageId);

    /**
     * 정모 채팅 접근 권한 검증
     *
     * [검증 내용]
     * 1. 정모 존재 여부
     * 2. 회원의 참석 상태가 ATTENDING인지
     *
     * @param meetingId 정모 ID
     * @param email 요청자 이메일
     * @throws CustomException 접근 권한이 없는 경우
     */
    void validateMeetingChatAccess(Long meetingId, String email);
}
