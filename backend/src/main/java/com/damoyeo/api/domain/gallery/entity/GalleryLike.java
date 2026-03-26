package com.damoyeo.api.domain.gallery.entity;

import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================================
 * 갤러리 좋아요(GalleryLike) 엔티티
 * ============================================================================
 *
 * [역할]
 * 갤러리 게시물(Post)에 대한 좋아요 정보를 저장합니다.
 * 한 사용자는 하나의 게시물에 한 번만 좋아요를 누를 수 있습니다.
 *
 * [구조 변경]
 * - 기존: GalleryImage 기준 좋아요
 * - 변경: GalleryPost 기준 좋아요 (여러 이미지를 하나로 묶은 게시물)
 *
 * [제약조건]
 * - 복합 유니크 키: (post_id, member_id)
 * - 중복 좋아요 방지
 */
@Entity
@Table(name = "gallery_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_gallery_like_post_member",
                        columnNames = {"post_id", "member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_gallery_like_post", columnList = "post_id"),
                @Index(name = "idx_gallery_like_member", columnList = "member_id")
        })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "member"})
public class GalleryLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 좋아요가 눌린 게시물
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GalleryPost post;

    /**
     * 좋아요를 누른 회원
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
