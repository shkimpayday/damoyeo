package com.damoyeo.api.domain.chat.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.meeting.entity.Meeting;
import com.damoyeo.api.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 채팅 읽음 상태 추적 엔티티
 * ============================================================================
 *
 * [역할]
 * 각 회원이 각 모임의 채팅을 어디까지 읽었는지 추적합니다.
 * 이를 통해 "읽지 않은 메시지 개수(unread count)"를 효율적으로 계산할 수 있습니다.
 *
 * [설계 이유]
 * 모든 메시지에 읽음 여부를 저장하는 것보다 훨씬 효율적입니다.
 * - 메시지 테이블 크기 증가 방지
 * - 읽음 상태 업데이트 성능 향상 (한 행만 UPDATE)
 * - unread count 계산 간편화 (마지막으로 읽은 메시지 ID보다 큰 메시지만 카운트)
 *
 * [DB 테이블]
 * "chat_read" 테이블에 매핑됩니다.
 *
 * [Unique 제약]
 * - (group_id, member_id): 한 회원은 한 모임에 하나의 읽음 상태만 가짐
 *   → 중복 생성 방지
 *
 * [관계]
 * - group (N:1) → Group: 모임 (정모 채팅 시 null)
 * - meeting (N:1) → Meeting: 정모 (모임 채팅 시 null)
 * - member (N:1) → Member: 회원
 *
 * [사용 예시]
 * 1. 회원 A가 모임 1의 채팅방 진입
 *    → ChatRead 조회 (group_id=1, member_id=A)
 *    → lastReadMessageId = 100
 *
 * 2. unread count 계산
 *    → COUNT(*) WHERE group_id=1 AND id > 100
 *    → 5개의 읽지 않은 메시지 존재
 *
 * 3. 회원 A가 메시지 읽음
 *    → lastReadMessageId = 105로 UPDATE
 *    → unread count = 0
 *
 * @author damoyeo
 * @since 2025-02-25
 */
@Entity
@Table(name = "chat_read", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_group_member",
        columnNames = {"group_id", "member_id"}
    ),
    @UniqueConstraint(
        name = "uk_meeting_member",
        columnNames = {"meeting_id", "member_id"}
    )
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "meeting", "member"})
public class ChatRead {

    /**
     * 읽음 상태 고유 ID (PK)
     *
     * AUTO_INCREMENT로 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 모임
     *
     * 어느 모임의 채팅을 읽었는지 추적합니다.
     * 정모 채팅의 경우 null입니다.
     *
     * LAZY: 읽음 상태 조회 시 모임 정보가 항상 필요하진 않음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    /**
     * 정모
     *
     * 어느 정모의 채팅을 읽었는지 추적합니다.
     * 모임 채팅의 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    /**
     * 회원
     *
     * 누가 읽었는지 추적합니다.
     *
     * LAZY: 읽음 상태 조회 시 회원 정보가 항상 필요하진 않음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 마지막으로 읽은 메시지 ID
     *
     * 이 ID보다 큰 메시지는 모두 "읽지 않은 메시지"입니다.
     *
     * 예시:
     * - lastReadMessageId = 100
     * - 현재 최신 메시지 ID = 105
     * - 읽지 않은 메시지: 101, 102, 103, 104, 105 (총 5개)
     *
     * 초기값: 0 (아직 아무 메시지도 읽지 않음)
     */
    @Column(nullable = false)
    @Builder.Default
    private Long lastReadMessageId = 0L;

    /**
     * 마지막 읽음 시각
     *
     * 회원이 마지막으로 채팅방을 확인한 시간입니다.
     * "5분 전 확인" 같은 정보를 UI에 표시할 때 사용할 수 있습니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime lastReadAt = LocalDateTime.now();

    // ========================================================================
    // 변경 메서드
    // ========================================================================

    /**
     * 읽음 상태 업데이트
     *
     * 회원이 채팅방에서 메시지를 읽었을 때 호출됩니다.
     *
     * [사용 예시]
     * - 회원이 채팅방 진입 시: 최신 메시지 ID로 업데이트
     * - 회원이 채팅방에서 메시지 수신 시: 자동으로 읽음 처리
     *
     * @param messageId 읽은 메시지의 ID
     */
    public void updateLastRead(Long messageId) {
        // 이전보다 더 최신 메시지를 읽은 경우에만 업데이트
        // (네트워크 지연으로 인한 역순 업데이트 방지)
        if (messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
            this.lastReadAt = LocalDateTime.now();
        }
    }

    /**
     * 현재 시각으로 읽음 시각만 업데이트
     *
     * 회원이 채팅방에 있지만 새 메시지가 없을 때 사용합니다.
     * (예: 채팅방 진입 시 마지막 활동 시각 기록)
     */
    public void updateLastReadAt() {
        this.lastReadAt = LocalDateTime.now();
    }
}
