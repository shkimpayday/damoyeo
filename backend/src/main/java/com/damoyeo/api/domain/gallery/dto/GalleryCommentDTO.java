package com.damoyeo.api.domain.gallery.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 갤러리 댓글 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GalleryCommentDTO {

    /** 댓글 ID */
    private Long id;

    /** 게시물 ID */
    private Long postId;

    /** 작성자 정보 (중첩 객체) */
    private MemberSummaryDTO writer;

    /** 댓글 내용 */
    private String content;

    /** 작성 일시 */
    private LocalDateTime createdAt;

    /** 현재 사용자가 삭제 가능한지 여부 */
    private boolean canDelete;
}
