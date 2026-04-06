package com.damoyeo.api.domain.board.dto;

import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 모임 게시판 댓글 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardCommentDTO {

    /** 댓글 ID */
    private Long id;

    /** 게시글 ID */
    private Long postId;

    /** 댓글 내용 */
    private String content;

    /** 작성자 정보 */
    private MemberSummaryDTO author;

    /** 작성 일시 */
    private LocalDateTime createdAt;

    /** 삭제 가능 여부 (본인 or 운영진) */
    private boolean canDelete;
}
