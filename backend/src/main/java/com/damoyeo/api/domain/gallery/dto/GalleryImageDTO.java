package com.damoyeo.api.domain.gallery.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 갤러리 이미지 DTO
 * ============================================================================
 *
 * [역할]
 * 갤러리 이미지 정보를 프론트엔드에 전달하는 DTO입니다.
 * 업로더 정보를 중첩 객체로 포함합니다.
 *
 * [사용 위치]
 * - GalleryController: 이미지 목록 조회 응답
 * - 프론트엔드 갤러리 그리드, 라이트박스
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalleryImageDTO {

    /** 이미지 ID */
    private Long id;

    /** 모임 ID */
    private Long groupId;

    /** 이미지 URL */
    private String imageUrl;

    /** 캡션/설명 */
    private String caption;

    /** 원본 파일명 */
    private String originalFileName;

    /** 파일 크기 (bytes) */
    private Long fileSize;

    /** 업로더 정보 (중첩 객체) */
    private MemberSummaryDTO uploader;

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
}
