package com.damoyeo.api.domain.board.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 모임 게시판 좋아요 엔티티
 *
 * <p>게시글에 대한 좋아요 정보를 저장합니다.</p>
 * <p>한 사용자는 하나의 게시글에 한 번만 좋아요를 누를 수 있습니다.</p>
 */
@Entity
@Table(name = "board_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_board_like_post_member",
                        columnNames = {"post_id", "member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_board_like_post", columnList = "post_id"),
                @Index(name = "idx_board_like_member", columnList = "member_id")
        })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "member"})
public class BoardLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 좋아요가 눌린 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    /** 좋아요를 누른 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
