package com.damoyeo.api.domain.gallery.controller;

import com.damoyeo.api.domain.gallery.dto.GalleryCommentDTO;
import com.damoyeo.api.domain.gallery.dto.GalleryPostDTO;
import com.damoyeo.api.domain.gallery.service.GalleryService;
import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 갤러리 컨트롤러
 * ============================================================================
 *
 * [역할]
 * 모임 갤러리 게시물 관련 API 엔드포인트를 제공합니다.
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 *
 * [엔드포인트]
 * POST   /api/groups/{groupId}/gallery        - 게시물 업로드 (이미지 최대 10개)
 * GET    /api/groups/{groupId}/gallery        - 게시물 목록 조회 (페이지네이션)
 * GET    /api/groups/{groupId}/gallery/count  - 게시물 개수 조회
 * GET    /api/groups/{groupId}/gallery/recent - 최신 게시물 미리보기
 * DELETE /api/gallery/posts/{postId}          - 게시물 삭제
 * POST   /api/gallery/posts/{postId}/like     - 게시물 좋아요 토글
 * GET    /api/gallery/posts/{postId}/comments - 댓글 목록 조회
 * POST   /api/gallery/posts/{postId}/comments - 댓글 작성
 * DELETE /api/gallery/comments/{commentId}    - 댓글 삭제
 *
 * [권한]
 * - 업로드/조회: 모임 멤버만 가능
 * - 삭제: 업로더 본인 또는 관리자(OWNER/MANAGER)만 가능
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gallery", description = "모임 갤러리 API")
public class GalleryController {

    private final GalleryService galleryService;

    // ========================================================================
    // 게시물 API
    // ========================================================================

    /**
     * 갤러리 게시물 업로드
     *
     * <p>최대 10개의 이미지를 하나의 게시물로 업로드합니다.</p>
     * <p>지원 형식: jpg, jpeg, png, gif, webp (최대 10MB)</p>
     *
     * @param groupId 모임 ID
     * @param files 업로드할 이미지 파일들
     * @param caption 게시물 캡션 (선택)
     * @param member 인증된 사용자 정보
     * @return 업로드된 게시물 정보
     */
    @PostMapping(value = "/groups/{groupId}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "갤러리 게시물 업로드", description = "모임 갤러리에 게시물을 업로드합니다. (이미지 최대 10개)")
    public ResponseEntity<GalleryPostDTO> uploadPost(
            @PathVariable Long groupId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "caption", required = false) String caption,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Gallery upload request - groupId: {}, fileCount: {}, user: {}",
                groupId, files.size(), member.getEmail());

        GalleryPostDTO uploadedPost = galleryService.uploadPost(
                groupId, files, caption, member.getEmail()
        );

        return ResponseEntity.ok(uploadedPost);
    }

    /**
     * 갤러리 게시물 목록 조회 (페이지네이션)
     *
     * @param groupId 모임 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본 20)
     * @param member 인증된 사용자 정보
     * @return 페이지네이션된 게시물 목록
     */
    @GetMapping("/groups/{groupId}/gallery")
    @Operation(summary = "갤러리 게시물 목록 조회", description = "모임 갤러리의 게시물 목록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<PageResponseDTO<GalleryPostDTO>> getGalleryPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal MemberDTO member) {

        PageResponseDTO<GalleryPostDTO> posts = galleryService.getGalleryPosts(
                groupId, page, size, member.getEmail()
        );

        return ResponseEntity.ok(posts);
    }

    /**
     * 갤러리 게시물 개수 조회
     *
     * @param groupId 모임 ID
     * @return 게시물 개수
     */
    @GetMapping("/groups/{groupId}/gallery/count")
    @Operation(summary = "갤러리 게시물 개수 조회", description = "모임 갤러리의 총 게시물 개수를 조회합니다.")
    public ResponseEntity<Map<String, Long>> getPostCount(@PathVariable Long groupId) {
        long count = galleryService.getPostCount(groupId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 최신 게시물 미리보기 조회
     *
     * <p>모임 상세 페이지에서 갤러리 탭 클릭 전 미리보기 용도입니다.</p>
     *
     * @param groupId 모임 ID
     * @param limit 조회할 개수 (기본 4, 최대 10)
     * @return 최신 게시물 목록
     */
    @GetMapping("/groups/{groupId}/gallery/recent")
    @Operation(summary = "최신 게시물 미리보기", description = "모임 갤러리의 최신 게시물을 미리보기로 조회합니다.")
    public ResponseEntity<List<GalleryPostDTO>> getRecentPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "4") @Parameter(description = "조회할 개수 (최대 10)") int limit) {

        int safeLimit = Math.min(limit, 10);
        List<GalleryPostDTO> posts = galleryService.getRecentPosts(groupId, safeLimit);

        return ResponseEntity.ok(posts);
    }

    /**
     * 갤러리 게시물 삭제
     *
     * <p>게시물과 포함된 모든 이미지를 삭제합니다.</p>
     *
     * @param postId 삭제할 게시물 ID
     * @param member 인증된 사용자 정보
     * @return 성공 메시지
     */
    @DeleteMapping("/gallery/posts/{postId}")
    @Operation(summary = "갤러리 게시물 삭제", description = "갤러리 게시물을 삭제합니다. (업로더 또는 관리자만 가능)")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Gallery delete request - postId: {}, user: {}",
                postId, member.getEmail());

        galleryService.deletePost(postId, member.getEmail());

        return ResponseEntity.ok(Map.of("message", "게시물이 삭제되었습니다."));
    }

    // ========================================================================
    // 좋아요 API
    // ========================================================================

    /**
     * 갤러리 게시물 좋아요 토글
     *
     * <p>이미 좋아요한 경우 취소, 아니면 좋아요 추가합니다.</p>
     *
     * @param postId 게시물 ID
     * @param member 인증된 사용자 정보
     * @return 좋아요 상태 및 개수
     */
    @PostMapping("/gallery/posts/{postId}/like")
    @Operation(summary = "게시물 좋아요 토글", description = "게시물에 좋아요를 추가하거나 취소합니다.")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Gallery like toggle - postId: {}, user: {}", postId, member.getEmail());

        boolean liked = galleryService.toggleLike(postId, member.getEmail());
        long likeCount = galleryService.getLikeCount(postId);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }

    /**
     * 갤러리 게시물 좋아요 개수 조회
     *
     * @param postId 게시물 ID
     * @return 좋아요 개수
     */
    @GetMapping("/gallery/posts/{postId}/like/count")
    @Operation(summary = "게시물 좋아요 개수 조회", description = "게시물의 좋아요 개수를 조회합니다.")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable Long postId) {
        long count = galleryService.getLikeCount(postId);
        return ResponseEntity.ok(Map.of("likeCount", count));
    }

    // ========================================================================
    // 댓글 API
    // ========================================================================

    /**
     * 갤러리 게시물에 댓글 작성
     *
     * @param postId 게시물 ID
     * @param request 댓글 내용 (content)
     * @param member 인증된 사용자 정보
     * @return 작성된 댓글
     */
    @PostMapping("/gallery/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시물에 댓글을 작성합니다.")
    public ResponseEntity<GalleryCommentDTO> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal MemberDTO member) {

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        log.info("Gallery comment add - postId: {}, user: {}", postId, member.getEmail());

        GalleryCommentDTO comment = galleryService.addComment(postId, content.trim(), member.getEmail());

        return ResponseEntity.ok(comment);
    }

    /**
     * 갤러리 게시물의 댓글 목록 조회
     *
     * @param postId 게시물 ID
     * @return 댓글 목록 (최신순)
     */
    @GetMapping("/gallery/posts/{postId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "게시물의 댓글 목록을 조회합니다.")
    public ResponseEntity<List<GalleryCommentDTO>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        List<GalleryCommentDTO> comments = galleryService.getComments(postId, member.getEmail());

        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 삭제
     *
     * @param commentId 삭제할 댓글 ID
     * @param member 인증된 사용자 정보
     * @return 성공 메시지
     */
    @DeleteMapping("/gallery/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (작성자만 가능)")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Gallery comment delete - commentId: {}, user: {}", commentId, member.getEmail());

        galleryService.deleteComment(commentId, member.getEmail());

        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }
}
