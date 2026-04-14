package com.damoyeo.api.domain.chat.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.meeting.entity.Meeting;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 채팅 메시지 엔티티
 *
 * 모임 내에서 주고받는 채팅 메시지를 저장합니다.
 * 실시간으로 WebSocket을 통해 전송되지만, 이후 조회를 위해 DB에도 저장됩니다.
 *
 * [DB 테이블]
 * "chat_message" 테이블에 매핑됩니다.
 *
 * [관계]
 * - group (N:1) → Group: 메시지가 속한 모임 (정모 채팅 시 null 가능)
 * - meeting (N:1) → Meeting: 메시지가 속한 정모 (모임 채팅 시 null)
 * - sender (N:1) → Member: 메시지 발신자 (SYSTEM 메시지는 null 가능)
 *
 * [인덱스 최적화]
 * - (group_id, created_at): 모임 채팅 히스토리 조회 시 성능 향상
 * - (meeting_id, created_at): 정모 채팅 히스토리 조회 시 성능 향상
 *
 * - ChatController: 메시지 히스토리 REST API
 * - ChatController.sendMessage: WebSocket 메시지 전송 (@MessageMapping)
 * - ChatService: 메시지 저장, 조회, 검증 비즈니스 로직
 *
 */
@Entity
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_group_created", columnList = "group_id, createdAt"),
    @Index(name = "idx_meeting_created", columnList = "meeting_id, createdAt")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "meeting", "sender"})
public class ChatMessage extends BaseEntity {

    /**
     * 메시지 고유 ID (PK)
     *
     * AUTO_INCREMENT로 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 모임
     *
     * 모임 채팅의 경우 필수, 정모 채팅의 경우 null 가능.
     * 모임이 삭제되어도 채팅 히스토리는 보존됩니다 (orphanRemoval 없음).
     *
     * LAZY: 메시지 조회 시 모임 정보가 항상 필요하진 않음
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    /**
     * 소속 정모
     *
     * 정모 참석자 전용 채팅에서 사용됩니다.
     * 모임 채팅의 경우 null입니다.
     *
     * [권한 검증]
     * 정모 채팅은 ATTENDING 상태의 참석자만 접근 가능합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    /**
     * 발신자
     *
     * 메시지를 보낸 회원입니다.
     * SYSTEM 메시지인 경우 null일 수 있습니다.
     * (예: "홍길동님이 입장했습니다" 같은 시스템 알림)
     *
     * LAZY: 메시지 조회 시 발신자 정보는 fetch join으로 필요 시에만 가져옴
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    /**
     * 메시지 내용
     *
     * 최대 2000자까지 저장 가능합니다.
     * null 불가 (빈 메시지는 프론트엔드에서 차단)
     *
     * 예: "안녕하세요!", "다음 주 토요일 10시에 만나요"
     */
    @Column(nullable = false, length = 2000)
    private String message;

    /**
     * 메시지 타입
     *
     * - TEXT: 일반 텍스트 메시지 (기본값)
     * - IMAGE: 이미지 메시지 (Phase 3)
     * - SYSTEM: 시스템 메시지 (입장/퇴장 알림 등)
     *
     * EnumType.STRING: DB에 "TEXT", "IMAGE", "SYSTEM" 문자열로 저장
     * (EnumType.ORDINAL은 enum 순서가 바뀌면 데이터 오류 발생 위험)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /**
     * 이미지 URL (Phase 3 확장 기능)
     *
     * messageType이 IMAGE일 때 사용됩니다.
     * 이미지 파일은 uploads/ 디렉토리에 저장되고, URL만 DB에 저장됩니다.
     *
     * 예: "/uploads/chat/abc123.jpg"
     */
    private String imageUrl;

    // BaseEntity 상속 필드
    // - createdAt: LocalDateTime (메시지 전송 시각)
    // - modifiedAt: LocalDateTime (메시지 수정 시각, 현재는 미사용)
    //
    // @CreatedDate, @LastModifiedDate로 자동 관리됩니다.
    // DamoyeoApplication.java에 @EnableJpaAuditing 설정 필요

    // 비즈니스 메서드

    /**
     * 메시지 내용 수정 (Phase 3 확장 기능)
     *
     * 메시지 전송 후 일정 시간 내에만 수정 가능하도록 제한할 수 있습니다.
     * (예: 5분 이내만 수정 가능)
     */
    public void updateMessage(String newMessage) {
        this.message = newMessage;
    }
}
