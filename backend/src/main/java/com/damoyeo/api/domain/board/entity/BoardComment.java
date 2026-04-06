package com.damoyeo.api.domain.board.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 모임 게시판 댓글 엔티티
 *
 * <p>게시글에 작성된 댓글 정보를 저장합니다.</p>
 *
 * <p>권한:</p>
 * <ul>
 *   <li>작성: 모임 멤버</li>
 *   <li>삭제: 댓글 작성자 또는 운영진(OWNER/MANAGER)</li>
 * </ul>
 */
@Entity
@Table(name = "board_comment", indexes = {
        @Index(name = "idx_board_comment_post", columnList = "post_id"),
        @Index(name = "idx_board_comment_author", columnList = "author_id")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "author"})
public class BoardComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 댓글이 달린 게시글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    /** 댓글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    /** 댓글 내용 (최대 500자) */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 댓글 내용 수정
     *
     * @param content 새 댓글 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }
}
