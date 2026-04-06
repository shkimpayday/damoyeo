package com.damoyeo.api.domain.board.entity;

import com.damoyeo.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 모임 게시판 게시글 첨부 이미지 엔티티
 *
 * <p>게시글(BoardPost)에 첨부된 이미지 파일 정보를 저장합니다.</p>
 * <p>BoardPost 1 : N BoardImage</p>
 */
@Entity
@Table(name = "board_image", indexes = {
        @Index(name = "idx_board_image_post", columnList = "post_id")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"post"})
public class BoardImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이미지가 속한 게시글
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BoardPost post;

    /**
     * 이미지 URL (/uploads/board/{groupId}/{uuid}.{ext})
     */
    @Column(nullable = false, length = 500)
    private String imageUrl;

    /** 원본 파일명 */
    @Column(length = 255)
    private String originalFileName;

    /** 파일 크기 (bytes) */
    private Long fileSize;
}
