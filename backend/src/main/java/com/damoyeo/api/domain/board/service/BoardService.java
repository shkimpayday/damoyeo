package com.damoyeo.api.domain.board.service;

import com.damoyeo.api.domain.board.dto.BoardCommentDTO;
import com.damoyeo.api.domain.board.dto.BoardPostDTO;
import com.damoyeo.api.domain.board.entity.BoardCategory;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 모임 게시판 서비스 인터페이스
 */
public interface BoardService {

    /**
     * 게시글 작성
     *
     * @param groupId 모임 ID
     * @param category 카테고리
     * @param title 제목
     * @param content 본문
     * @param files 첨부 이미지 (선택)
     * @param email 작성자 이메일
     * @return 생성된 게시글 DTO
     */
    BoardPostDTO createPost(Long groupId, BoardCategory category, String title,
                            String content, List<MultipartFile> files, String email);

    /**
     * 게시글 목록 조회 (무한 스크롤 / 페이지네이션)
     *
     * @param groupId 모임 ID
     * @param page 페이지 번호 (0부터)
     * @param size 페이지 크기
     * @param category 카테고리 필터 (null이면 전체)
     * @param email 조회자 이메일
     * @return 페이지네이션된 게시글 목록
     */
    PageResponseDTO<BoardPostDTO> getPosts(Long groupId, int page, int size,
                                           BoardCategory category, String email);

    /**
     * 게시글 상세 조회
     *
     * @param groupId 모임 ID
     * @param postId 게시글 ID
     * @param email 조회자 이메일
     * @return 게시글 DTO
     */
    BoardPostDTO getPost(Long groupId, Long postId, String email);

    /**
     * 게시글 삭제
     *
     * @param postId 삭제할 게시글 ID
     * @param email 요청자 이메일
     */
    void deletePost(Long postId, String email);

    /**
     * 좋아요 토글
     *
     * @param postId 게시글 ID
     * @param email 요청자 이메일
     * @return true: 좋아요 추가, false: 좋아요 취소
     */
    boolean toggleLike(Long postId, String email);

    /**
     * 좋아요 개수 조회
     *
     * @param postId 게시글 ID
     * @return 좋아요 개수
     */
    long getLikeCount(Long postId);

    /**
     * 댓글 목록 조회
     *
     * @param postId 게시글 ID
     * @param email 조회자 이메일
     * @return 댓글 목록
     */
    List<BoardCommentDTO> getComments(Long postId, String email);

    /**
     * 댓글 작성
     *
     * @param postId 게시글 ID
     * @param content 댓글 내용
     * @param email 작성자 이메일
     * @return 생성된 댓글 DTO
     */
    BoardCommentDTO addComment(Long postId, String content, String email);

    /**
     * 댓글 삭제
     *
     * @param commentId 삭제할 댓글 ID
     * @param email 요청자 이메일
     */
    void deleteComment(Long commentId, String email);
}
