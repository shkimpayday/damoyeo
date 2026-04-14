package com.damoyeo.api.domain.chat.repository;

import com.damoyeo.api.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 채팅 메시지 Repository
 *
 * 채팅 메시지 엔티티의 데이터베이스 접근을 담당합니다.
 *
 * [상속]
 * JpaRepository<ChatMessage, Long>을 상속받아 기본 CRUD 메서드를 자동으로 제공받습니다:
 * - save(), findById(), findAll(), delete() 등
 *
 * [커스텀 쿼리]
 * @Query 어노테이션으로 채팅 메시지 조회 로직을 구현합니다.
 * - JPQL: 엔티티 기반 쿼리
 * - fetch join: N+1 문제 방지 (sender 정보 함께 조회)
 *
 * - ChatServiceImpl: 메시지 저장, 조회, unread count 계산
 * - ChatController: REST API 엔드포인트
 *
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 모임별 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 특정 모임의 채팅 메시지를 최신순으로 조회합니다.
     * 프론트엔드에서 채팅방 진입 시 최근 50개 메시지를 로드할 때 사용됩니다.
     *
     * [정렬]
     * createdAt DESC: 최신 메시지부터 반환
     *
     * [N+1 문제 방지]
     * left join fetch m.sender: 메시지와 발신자 정보를 한 번의 쿼리로 조회
     * - fetch join 없이 조회하면 메시지 50개마다 발신자 정보를 조회하는 쿼리가 50번 실행됩니다.
     * - fetch join 사용 시 1번의 쿼리로 모두 조회됩니다.
     *
     * [SYSTEM 메시지 처리]
     * left join fetch: sender가 null인 SYSTEM 메시지도 조회됩니다.
     *
     * @param groupId 모임 ID
     * @param pageable 페이지 정보 (page, size, sort)
     * @return 페이지네이션된 메시지 목록 (발신자 정보 포함)
     *
     * 호출 위치: ChatServiceImpl.getMessages()
     */
    @Query("select m from ChatMessage m " +
            "left join fetch m.sender " +
            "where m.group.id = :groupId " +
            "order by m.createdAt desc")
    Page<ChatMessage> findByGroupIdOrderByCreatedAtDesc(
            @Param("groupId") Long groupId,
            Pageable pageable
    );

    /**
     * 특정 메시지 ID 이후의 메시지 개수 조회 (unread count)
     *
     * [용도]
     * 읽지 않은 메시지 개수를 계산합니다.
     *
     * [계산 방식]
     * ChatRead 엔티티의 lastReadMessageId 값보다 큰 ID를 가진 메시지의 개수를 반환합니다.
     *
     * [예시]
     * - 회원 A의 lastReadMessageId = 100
     * - 모임 1의 최신 메시지 ID = 105
     * - 쿼리: SELECT COUNT(*) FROM chat_message WHERE group_id = 1 AND id > 100
     * - 결과: 5개 (101, 102, 103, 104, 105)
     *
     * @param groupId 모임 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 읽지 않은 메시지 개수
     *
     * 호출 위치: ChatServiceImpl.getUnreadCount()
     */
    @Query("select count(m) from ChatMessage m " +
            "where m.group.id = :groupId and m.id > :lastReadMessageId")
    int countByGroupIdAndIdGreaterThan(
            @Param("groupId") Long groupId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    /**
     * 모임의 최신 메시지 조회
     *
     * [용도]
     * 채팅방 목록에서 각 모임의 최신 메시지를 표시할 때 사용합니다.
     * (예: "홍길동: 안녕하세요" - 5분 전)
     *
     * [정렬]
     * createdAt DESC + LIMIT 1: 가장 최신 메시지 1개만 반환
     *
     * @param groupId 모임 ID
     * @return 해당 모임의 최신 메시지 (없으면 null)
     *
     * 호출 위치: ChatServiceImpl.getMyChatRooms()
     */
    @Query("select m from ChatMessage m " +
            "left join fetch m.sender " +
            "where m.group.id = :groupId " +
            "order by m.createdAt desc " +
            "limit 1")
    ChatMessage findLatestMessageByGroupId(@Param("groupId") Long groupId);

    /**
     * 모임의 전체 메시지 개수 조회
     *
     * [용도]
     * 채팅방 통계 정보 표시 시 사용합니다.
     * (예: "총 123개의 메시지")
     *
     * @param groupId 모임 ID
     * @return 해당 모임의 전체 메시지 개수
     *
     * 호출 위치: ChatServiceImpl.getMessageCount()
     */
    long countByGroupId(Long groupId);

    // 정모 채팅 관련 쿼리

    /**
     * 정모별 메시지 히스토리 조회 (페이지네이션)
     *
     * [용도]
     * 정모 참석자 전용 채팅의 메시지를 최신순으로 조회합니다.
     *
     * @param meetingId 정모 ID
     * @param pageable 페이지 정보 (page, size, sort)
     * @return 페이지네이션된 메시지 목록 (발신자 정보 포함)
     *
     * 호출 위치: ChatServiceImpl.getMeetingMessages()
     */
    @Query("select m from ChatMessage m " +
            "left join fetch m.sender " +
            "where m.meeting.id = :meetingId " +
            "order by m.createdAt desc")
    Page<ChatMessage> findByMeetingIdOrderByCreatedAtDesc(
            @Param("meetingId") Long meetingId,
            Pageable pageable
    );

    /**
     * 정모별 읽지 않은 메시지 개수
     *
     * @param meetingId 정모 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 읽지 않은 메시지 개수
     *
     * 호출 위치: ChatServiceImpl.getMeetingUnreadCount()
     */
    @Query("select count(m) from ChatMessage m " +
            "where m.meeting.id = :meetingId and m.id > :lastReadMessageId")
    int countByMeetingIdAndIdGreaterThan(
            @Param("meetingId") Long meetingId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    /**
     * 정모의 최신 메시지 조회
     *
     * @param meetingId 정모 ID
     * @return 해당 정모의 최신 메시지 (없으면 null)
     *
     * 호출 위치: ChatServiceImpl.getMeetingLatestMessage()
     */
    @Query("select m from ChatMessage m " +
            "left join fetch m.sender " +
            "where m.meeting.id = :meetingId " +
            "order by m.createdAt desc " +
            "limit 1")
    ChatMessage findLatestMessageByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 정모의 전체 메시지 개수 조회
     *
     * @param meetingId 정모 ID
     * @return 해당 정모의 전체 메시지 개수
     */
    long countByMeetingId(Long meetingId);
}
