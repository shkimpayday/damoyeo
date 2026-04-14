package com.damoyeo.api.domain.support.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 상담 메시지 엔티티
 *
 * 상담 채팅에서 주고받는 개별 메시지를 저장합니다.
 *
 * [DB 테이블]
 * "support_message" 테이블에 매핑됩니다.
 *
 * [관계]
 * - supportChat (N:1) → SupportChat: 소속 상담 채팅
 * - sender (N:1) → Member: 메시지 발신자
 *
 */
@Entity
@Table(name = "support_message", indexes = {
    @Index(name = "idx_support_msg_chat", columnList = "support_chat_id, createdAt")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"supportChat", "sender"})
public class SupportMessage extends BaseEntity {

    /**
     * 메시지 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 상담 채팅
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_chat_id", nullable = false)
    private SupportChat supportChat;

    /**
     * 발신자
     *
     * 메시지를 보낸 사용자 또는 관리자입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    /**
     * 메시지 내용
     *
     * 최대 2000자까지 저장 가능합니다.
     */
    @Column(nullable = false, length = 2000)
    private String message;

    /**
     * 관리자 메시지 여부
     *
     * true: 관리자가 보낸 메시지
     * false: 사용자가 보낸 메시지
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isAdmin = false;
}
