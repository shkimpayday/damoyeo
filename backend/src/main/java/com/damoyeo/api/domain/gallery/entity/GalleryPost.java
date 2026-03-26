package com.damoyeo.api.domain.gallery.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * 갤러리 게시물(GalleryPost) 엔티티
 * ============================================================================
 *
 * [역할]
 * 여러 이미지를 하나의 게시물로 그룹화합니다.
 * 한 번에 여러 장의 사진을 업로드하면 하나의 Post로 묶입니다.
 *
 * [구조]
 * GalleryPost 1 : N GalleryImage
 * - 하나의 게시물에 여러 이미지가 포함됨
 * - 좋아요/댓글은 게시물(Post) 단위로 관리
 *
 * [프론트엔드 UI]
 * ┌─────────────────────────────────────┐
 * │ ┌─────────────────────────────────┐ │
 * │ │  ← 이미지 1/3 →  (캐러셀)       │ │
 * │ │       [●○○]                     │ │
 * │ └─────────────────────────────────┘ │
 * │ ❤️ 12  💬 5                        │
 * │ "오늘 정모 사진입니다!"             │
 * │ 2024.01.15 • 닉네임                │
 * └─────────────────────────────────────┘
 */
@Entity
@Table(name = "gallery_post", indexes = {
        @Index(name = "idx_gallery_post_group", columnList = "group_id"),
        @Index(name = "idx_gallery_post_uploader", columnList = "uploader_id"),
        @Index(name = "idx_gallery_post_created", columnList = "createdAt")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "uploader", "images"})
public class GalleryPost extends BaseEntity {

    /**
     * 게시물 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게시물이 속한 모임
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * 게시물 작성자 (업로더)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private Member uploader;

    /**
     * 게시물 캡션/설명
     *
     * 이미지들에 대한 설명 (선택사항)
     */
    @Column(length = 500)
    private String caption;

    /**
     * 게시물에 포함된 이미지들
     *
     * 일대다 관계: 하나의 게시물에 여러 이미지
     * cascade = ALL: 게시물 삭제 시 이미지도 함께 삭제
     * orphanRemoval = true: 컬렉션에서 제거된 이미지도 DB에서 삭제
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<GalleryImage> images = new ArrayList<>();

    // ========================================================================
    // 편의 메서드
    // ========================================================================

    /**
     * 이미지 추가
     *
     * @param image 추가할 이미지
     */
    public void addImage(GalleryImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    /**
     * 캡션 수정
     *
     * @param caption 새 캡션
     */
    public void updateCaption(String caption) {
        this.caption = caption;
    }

    /**
     * 대표 이미지(썸네일) 반환
     *
     * @return 첫 번째 이미지의 URL, 없으면 null
     */
    public String getThumbnailUrl() {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getImageUrl();
    }

    /**
     * 이미지 개수 반환
     *
     * @return 이미지 수
     */
    public int getImageCount() {
        return images == null ? 0 : images.size();
    }
}
