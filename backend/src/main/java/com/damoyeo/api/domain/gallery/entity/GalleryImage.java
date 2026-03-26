package com.damoyeo.api.domain.gallery.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================================
 * 갤러리 이미지(GalleryImage) 엔티티
 * ============================================================================
 *
 * [역할]
 * 모임별 갤러리에 업로드된 개별 이미지 정보를 저장합니다.
 * GalleryPost에 속하여 여러 이미지가 하나의 게시물로 묶일 수 있습니다.
 *
 * [구조]
 * GalleryPost 1 : N GalleryImage
 * - 여러 이미지가 하나의 게시물(Post)에 속함
 * - 캡션, 좋아요, 댓글은 Post 단위로 관리
 *
 * [권한]
 * - 업로드: 모임 멤버 (OWNER, MANAGER, MEMBER)
 * - 삭제: 업로더 본인 또는 모임 관리자 (OWNER, MANAGER)
 * - 조회: 모임 멤버
 *
 * [사용 위치]
 * - GalleryController: 이미지 업로드, 조회, 삭제
 * - 프론트엔드 모임 상세 페이지 갤러리 탭
 *
 * [DB 테이블]
 * gallery_image 테이블
 */
@Entity
@Table(name = "gallery_image", indexes = {
        @Index(name = "idx_gallery_image_post", columnList = "post_id"),
        @Index(name = "idx_gallery_image_group", columnList = "group_id"),
        @Index(name = "idx_gallery_image_uploader", columnList = "uploader_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post", "group", "uploader"})
public class GalleryImage extends BaseEntity {

    /**
     * 갤러리 이미지 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이미지가 속한 게시물
     *
     * 다대일 관계: 여러 이미지가 하나의 게시물에 속함
     * nullable = true: 마이그레이션을 위해 임시로 nullable 허용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private GalleryPost post;

    /**
     * 이미지가 속한 모임
     *
     * 다대일 관계: 여러 이미지가 하나의 모임에 속함
     * LAZY 로딩으로 N+1 문제 방지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * 이미지 업로더
     *
     * 다대일 관계: 여러 이미지가 한 명의 회원에 의해 업로드됨
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private Member uploader;

    /**
     * 이미지 URL
     *
     * 서버에 저장된 이미지 파일의 URL 경로
     * 예: "/uploads/gallery/uuid.jpg"
     */
    @Column(nullable = false, length = 500)
    private String imageUrl;

    /**
     * 이미지 캡션/설명
     *
     * 이미지에 대한 짧은 설명 (선택사항)
     */
    @Column(length = 200)
    private String caption;

    /**
     * 원본 파일명
     *
     * 업로드 시 원본 파일 이름 저장
     */
    @Column(length = 255)
    private String originalFileName;

    /**
     * 파일 크기 (bytes)
     */
    private Long fileSize;

    // ========================================================================
    // 변경 메서드
    // ========================================================================

    /**
     * 캡션 수정
     */
    public void updateCaption(String caption) {
        this.caption = caption;
    }
}
