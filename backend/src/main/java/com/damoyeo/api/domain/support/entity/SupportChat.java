package com.damoyeo.api.domain.support.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 상담 채팅 엔티티
 * ============================================================================
 *
 * [역할]
 * 사용자와 관리자 간의 1:1 상담 채팅방을 나타냅니다.
 *
 * [DB 테이블]
 * "support_chat" 테이블에 매핑됩니다.
 *
 * [관계]
 * - user (N:1) → Member: 상담을 요청한 사용자
 * - admin (N:1) → Member: 상담을 담당하는 관리자 (null 가능)
 * - messages (1:N) → SupportMessage: 상담 메시지 목록
 *
 * [상태 흐름]
 * WAITING → IN_PROGRESS → COMPLETED
 *
 * @author damoyeo
 * @since 2025-03-16
 */
@Entity
@Table(name = "support_chat", indexes = {
    @Index(name = "idx_support_user", columnList = "user_id"),
    @Index(name = "idx_support_admin", columnList = "admin_id"),
    @Index(name = "idx_support_status", columnList = "status")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user", "admin", "messages"})
public class SupportChat extends BaseEntity {

    /**
     * 상담 채팅 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 상담 요청 사용자
     *
     * 상담을 요청한 일반 사용자입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    /**
     * 담당 관리자
     *
     * 상담을 담당하는 관리자입니다.
     * WAITING 상태에서는 null일 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Member admin;

    /**
     * 상담 제목
     *
     * 사용자가 상담 요청 시 입력한 제목입니다.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 상담 상태
     *
     * WAITING: 대기 중
     * IN_PROGRESS: 진행 중
     * COMPLETED: 완료
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SupportChatStatus status = SupportChatStatus.WAITING;

    /**
     * 상담 종료 시각
     *
     * 상담이 완료되었을 때의 시각입니다.
     */
    private LocalDateTime completedAt;

    /**
     * 만족도 평가 (1~5)
     *
     * 사용자가 상담 종료 후 평가한 만족도입니다.
     * null이면 아직 평가하지 않음
     */
    private Integer rating;

    /**
     * 상담 메시지 목록
     */
    @OneToMany(mappedBy = "supportChat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupportMessage> messages = new ArrayList<>();

    // ========================================================================
    // 비즈니스 메서드
    // ========================================================================

    /**
     * 관리자 배정 (상담 시작)
     *
     * @param admin 담당 관리자
     */
    public void assignAdmin(Member admin) {
        this.admin = admin;
        this.status = SupportChatStatus.IN_PROGRESS;
    }

    /**
     * 상담 완료
     */
    public void complete() {
        this.status = SupportChatStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 만족도 평가
     *
     * @param rating 평가 점수 (1~5)
     */
    public void rate(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평가는 1~5 사이여야 합니다.");
        }
        this.rating = rating;
    }
}
