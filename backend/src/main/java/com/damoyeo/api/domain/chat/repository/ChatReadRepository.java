package com.damoyeo.api.domain.chat.repository;

import com.damoyeo.api.domain.chat.entity.ChatRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 채팅 읽음 상태 Repository
 *
 * ChatRead 엔티티의 데이터베이스 접근을 담당합니다.
 * 각 회원이 각 모임의 채팅을 어디까지 읽었는지 추적합니다.
 *
 * [상속]
 * JpaRepository<ChatRead, Long>을 상속받아 기본 CRUD 메서드를 자동으로 제공받습니다.
 *
 * - ChatServiceImpl: 읽음 상태 조회, 업데이트, unread count 계산
 * - ChatController: 읽음 처리 REST API
 *
 */
public interface ChatReadRepository extends JpaRepository<ChatRead, Long> {

    /**
     * 회원별 모임별 읽음 상태 조회
     *
     * [용도]
     * 특정 회원이 특정 모임의 채팅을 어디까지 읽었는지 조회합니다.
     *
     * [Unique 제약]
     * (group_id, member_id)에 대해 Unique 제약이 있으므로
     * 결과는 최대 1개입니다 (Optional 반환).
     *
     * 1. 채팅방 진입 시: lastReadMessageId 조회
     * 2. unread count 계산 시: lastReadMessageId보다 큰 메시지 개수 카운트
     * 3. 읽음 상태 업데이트 시: 기존 레코드 조회 후 UPDATE
     *
     * @param groupId 모임 ID
     * @param memberId 회원 ID
     * @return 읽음 상태 (없으면 Optional.empty())
     *
     * 호출 위치: ChatServiceImpl.getOrCreateChatRead()
     */
    @Query("select cr from ChatRead cr " +
            "where cr.group.id = :groupId and cr.member.id = :memberId")
    Optional<ChatRead> findByGroupIdAndMemberId(
            @Param("groupId") Long groupId,
            @Param("memberId") Long memberId
    );

    /**
     * 회원의 모든 모임 읽음 상태 삭제
     *
     * [용도]
     * 회원 탈퇴 시 해당 회원의 모든 읽음 상태를 삭제합니다.
     *
     * [GDPR 대응]
     * 개인정보 보호를 위해 회원 탈퇴 시 관련 데이터를 삭제합니다.
     *
     * @param memberId 회원 ID
     *
     * 호출 위치: MemberServiceImpl.deleteMember() (Phase 3)
     */
    void deleteByMemberId(Long memberId);

    /**
     * 모임의 모든 읽음 상태 삭제
     *
     * [용도]
     * 모임 해체 시 해당 모임의 모든 읽음 상태를 삭제합니다.
     *
     * [Cascade]
     * 모임 삭제 시 관련 읽음 상태도 함께 삭제하여 데이터 정합성을 유지합니다.
     *
     * @param groupId 모임 ID
     *
     * 호출 위치: GroupServiceImpl.deleteGroup()
     */
    void deleteByGroupId(Long groupId);

    // 정모 채팅 관련 쿼리

    /**
     * 회원별 정모별 읽음 상태 조회
     *
     * [용도]
     * 특정 회원이 특정 정모의 채팅을 어디까지 읽었는지 조회합니다.
     *
     * [Unique 제약]
     * (meeting_id, member_id)에 대해 Unique 제약이 있으므로
     * 결과는 최대 1개입니다 (Optional 반환).
     *
     * @param meetingId 정모 ID
     * @param memberId 회원 ID
     * @return 읽음 상태 (없으면 Optional.empty())
     *
     * 호출 위치: ChatServiceImpl.getOrCreateMeetingChatRead()
     */
    @Query("select cr from ChatRead cr " +
            "where cr.meeting.id = :meetingId and cr.member.id = :memberId")
    Optional<ChatRead> findByMeetingIdAndMemberId(
            @Param("meetingId") Long meetingId,
            @Param("memberId") Long memberId
    );

    /**
     * 정모의 모든 읽음 상태 삭제
     *
     * [용도]
     * 정모 취소/삭제 시 해당 정모의 모든 읽음 상태를 삭제합니다.
     *
     * @param meetingId 정모 ID
     *
     * 호출 위치: MeetingServiceImpl.deleteMeeting()
     */
    void deleteByMeetingId(Long meetingId);
}
