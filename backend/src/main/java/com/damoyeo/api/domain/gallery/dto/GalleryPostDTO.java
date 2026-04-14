package com.damoyeo.api.domain.gallery.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 갤러리 게시물 DTO
 *
 * 갤러리 게시물 정보를 프론트엔드에 전달하는 DTO입니다.
 * 여러 이미지를 하나의 게시물로 묶어서 표시합니다.
 *
 * [구조]
 * - 게시물(Post) 정보: id, caption, uploader, createdAt
 * - 이미지 목록: images[]
 * - 상호작용: likeCount, commentCount, liked
 *
 * [프론트엔드 UI]
 * - 갤러리 그리드: 썸네일(첫 번째 이미지)로 표시, 여러 장이면 표시
 * - 라이트박스: 캐러셀로 여러 이미지 슬라이드
 *
 * - GalleryController: 게시물 목록 조회 응답
 * - 프론트엔드 갤러리 그리드, 라이트박스
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalleryPostDTO {

    /** 게시물 ID */
    private Long id;

    /** 모임 ID */
    private Long groupId;

    /** 캡션/설명 */
    private String caption;

    /** 업로더 정보 (중첩 객체) */
    private MemberSummaryDTO uploader;

    /** 게시물에 포함된 이미지 목록 */
    private List<GalleryImageSimpleDTO> images;

    /** 이미지 개수 */
    private int imageCount;

    /** 대표 이미지(썸네일) URL */
    private String thumbnailUrl;

    /** 업로드 일시 */
    private LocalDateTime createdAt;

    /** 현재 사용자가 삭제 가능한지 여부 */
    private boolean canDelete;

    /** 좋아요 개수 */
    private long likeCount;

    /** 댓글 개수 */
    private long commentCount;

    /** 현재 사용자가 좋아요를 눌렀는지 여부 */
    private boolean liked;

    /**
     * ========================================================================
     * 이미지 간단 정보 DTO (게시물 내부용)
     * ========================================================================
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GalleryImageSimpleDTO {

        /** 이미지 ID */
        private Long id;

        /** 이미지 URL */
        private String imageUrl;

        /** 원본 파일명 */
        private String originalFileName;

        /** 파일 크기 (bytes) */
        private Long fileSize;
    }
}
