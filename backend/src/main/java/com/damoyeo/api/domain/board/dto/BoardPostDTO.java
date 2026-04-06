package com.damoyeo.api.domain.board.dto;

import com.damoyeo.api.domain.board.entity.BoardCategory;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모임 게시판 게시글 DTO
 *
 * <p>게시글 목록 및 상세 조회 시 프론트엔드에 전달하는 DTO입니다.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardPostDTO {

    /** 게시글 ID */
    private Long id;

    /** 모임 ID */
    private Long groupId;

    /** 카테고리 */
    private BoardCategory category;

    /** 제목 */
    private String title;

    /** 본문 */
    private String content;

    /** 첨부 이미지 목록 */
    private List<BoardImageSimpleDTO> images;

    /** 이미지 개수 */
    private int imageCount;

    /** 대표 이미지(썸네일) URL */
    private String thumbnailUrl;

    /** 작성자 정보 */
    private MemberSummaryDTO author;

    /** 좋아요 수 */
    private long likeCount;

    /** 댓글 수 */
    private long commentCount;

    /** 현재 사용자 좋아요 여부 */
    private boolean liked;

    /** 상단 고정 여부 */
    private boolean isPinned;

    /** 삭제 가능 여부 (본인 or 운영진) */
    private boolean canDelete;

    /** 작성 일시 */
    private LocalDateTime createdAt;

    /** 수정 일시 */
    private LocalDateTime updatedAt;

    /**
     * 게시글 내 이미지 간단 정보 DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BoardImageSimpleDTO {
        private Long id;
        private String imageUrl;
        private String originalFileName;
        private Long fileSize;
    }
}
