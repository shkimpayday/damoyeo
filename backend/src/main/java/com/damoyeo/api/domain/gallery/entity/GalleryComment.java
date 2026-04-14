package com.damoyeo.api.domain.gallery.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 갤러리 댓글(GalleryComment) 엔티티
 *
 * 갤러리 게시물(Post)에 대한 댓글 정보를 저장합니다.
 *
 * [구조 변경]
 * - 기존: GalleryImage 기준 댓글
 * - 변경: GalleryPost 기준 댓글 (여러 이미지를 하나로 묶은 게시물)
 *
 * [권한]
 * - 작성: 모임 멤버
 * - 삭제: 댓글 작성자 또는 모임 관리자 (OWNER/MANAGER)
 */
@Entity
@Table(name = "gallery_comment", indexes = {
        @Index(name = "idx_gallery_comment_post", columnList = "post_id"),
        @Index(name = "idx_gallery_comment_writer", columnList = "writer_id")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "writer"})
public class GalleryComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 댓글이 달린 게시물
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GalleryPost post;

    /**
     * 댓글 작성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;

    /**
     * 댓글 내용 (최대 500자)
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 댓글 내용 수정
     */
    public void updateContent(String content) {
        this.content = content;
    }
}
