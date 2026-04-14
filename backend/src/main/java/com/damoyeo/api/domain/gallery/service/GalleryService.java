package com.damoyeo.api.domain.gallery.service;

import com.damoyeo.api.domain.gallery.dto.GalleryCommentDTO;
import com.damoyeo.api.domain.gallery.dto.GalleryPostDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 갤러리 서비스 인터페이스
 *
 * 갤러리 게시물 관련 비즈니스 로직을 정의합니다.
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 *
 * - 게시물 업로드 (다중 이미지)
 * - 게시물 목록 조회 (페이지네이션)
 * - 게시물 삭제
 * - 좋아요/댓글 (게시물 단위)
 * - 권한 검증
 */
public interface GalleryService {

    /**
     * 갤러리 게시물 업로드
     *
     * <p>여러 이미지를 하나의 게시물로 묶어서 업로드합니다.</p>
     *
     * [권한]
     * 모임 멤버만 업로드 가능 (OWNER, MANAGER, MEMBER)
     *
     * @param groupId 모임 ID
     * @param files 업로드할 파일들 (최대 10개)
     * @param caption 게시물 캡션 (선택)
     * @param email 업로더 이메일
     * @return 업로드된 게시물 정보
     */
    GalleryPostDTO uploadPost(Long groupId, List<MultipartFile> files, String caption, String email);

    /**
     * 갤러리 게시물 목록 조회 (페이지네이션)
     *
     * [권한]
     * 모임 멤버만 조회 가능
     *
     * @param groupId 모임 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param email 조회하는 사용자 이메일 (삭제 권한 확인용)
     * @return 페이지네이션된 게시물 목록
     */
    PageResponseDTO<GalleryPostDTO> getGalleryPosts(Long groupId, int page, int size, String email);

    /**
     * 갤러리 게시물 개수 조회
     *
     * @param groupId 모임 ID
     * @return 게시물 개수
     */
    long getPostCount(Long groupId);

    /**
     * 갤러리 게시물 삭제
     *
     * <p>게시물과 포함된 모든 이미지를 삭제합니다.</p>
     *
     * [권한]
     * - 업로더 본인
     * - 모임 관리자 (OWNER, MANAGER)
     *
     * @param postId 게시물 ID
     * @param email 삭제 요청자 이메일
     */
    void deletePost(Long postId, String email);

    /**
     * 최신 게시물 미리보기 조회
     *
     * 모임 상세 페이지에서 갤러리 탭 클릭 전 미리보기용
     *
     * @param groupId 모임 ID
     * @param limit 조회할 개수
     * @return 최신 게시물 목록
     */
    List<GalleryPostDTO> getRecentPosts(Long groupId, int limit);

    // 좋아요 관련 메서드 (게시물 단위)

    /**
     * 게시물 좋아요 토글
     *
     * @param postId 게시물 ID
     * @param email 사용자 이메일
     * @return 좋아요 상태 (true: 좋아요 추가, false: 좋아요 취소)
     */
    boolean toggleLike(Long postId, String email);

    /**
     * 게시물 좋아요 개수 조회
     *
     * @param postId 게시물 ID
     * @return 좋아요 개수
     */
    long getLikeCount(Long postId);

    // 댓글 관련 메서드 (게시물 단위)

    /**
     * 댓글 작성
     *
     * @param postId 게시물 ID
     * @param content 댓글 내용
     * @param email 작성자 이메일
     * @return 작성된 댓글 정보
     */
    GalleryCommentDTO addComment(Long postId, String content, String email);

    /**
     * 댓글 목록 조회
     *
     * @param postId 게시물 ID
     * @param email 조회하는 사용자 이메일 (삭제 권한 확인용)
     * @return 댓글 목록
     */
    List<GalleryCommentDTO> getComments(Long postId, String email);

    /**
     * 댓글 삭제
     *
     * @param commentId 댓글 ID
     * @param email 삭제 요청자 이메일
     */
    void deleteComment(Long commentId, String email);
}
