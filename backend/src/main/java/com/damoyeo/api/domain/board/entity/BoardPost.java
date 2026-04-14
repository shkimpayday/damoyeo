package com.damoyeo.api.domain.board.entity;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 모임 게시판 게시글 엔티티
 *
 * <p>모임 내 게시판의 게시글 정보를 저장합니다.</p>
 *
 * <p>카테고리별 게시글:</p>
 * <ul>
 *   <li>GREETING: 가입인사</li>
 *   <li>REVIEW: 모임후기</li>
 *   <li>FREE: 자유게시판</li>
 *   <li>NOTICE: 공지사항 (운영진만 작성)</li>
 * </ul>
 */
@Entity
@Table(name = "board_post", indexes = {
        @Index(name = "idx_board_post_group", columnList = "group_id"),
        @Index(name = "idx_board_post_author", columnList = "author_id"),
        @Index(name = "idx_board_post_category", columnList = "category"),
        @Index(name = "idx_board_post_created", columnList = "createdAt")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"group", "author", "images"})
public class BoardPost extends BaseEntity {

    /** 게시글 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글이 속한 모임 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /** 게시글 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    /** 게시글 카테고리 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BoardCategory category;

    /** 게시글 제목 (최대 100자) */
    @Column(nullable = false, length = 100)
    private String title;

    /** 게시글 본문 (최대 2000자) */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * 상단 고정 여부 (공지사항용)
     *
     * <p>true이면 목록 최상단에 고정됩니다.</p>
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isPinned = false;

    /**
     * 첨부 이미지 목록
     *
     * <p>cascade = ALL: 게시글 삭제 시 이미지도 함께 삭제</p>
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<BoardImage> images = new ArrayList<>();

    // 편의 메서드

    /**
     * 이미지 추가
     *
     * @param image 추가할 이미지
     */
    public void addImage(BoardImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    /**
     * 대표 이미지(썸네일) URL 반환
     *
     * @return 첫 번째 이미지 URL, 없으면 null
     */
    public String getThumbnailUrl() {
        if (images == null || images.isEmpty()) return null;
        return images.get(0).getImageUrl();
    }

    /**
     * 이미지 개수 반환
     */
    public int getImageCount() {
        return images == null ? 0 : images.size();
    }

    /**
     * 고정 상태 변경
     */
    public void pin(boolean pinned) {
        this.isPinned = pinned;
    }
}
