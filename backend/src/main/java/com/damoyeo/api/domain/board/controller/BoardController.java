package com.damoyeo.api.domain.board.controller;

import com.damoyeo.api.domain.board.dto.BoardCommentDTO;
import com.damoyeo.api.domain.board.dto.BoardPostDTO;
import com.damoyeo.api.domain.board.entity.BoardCategory;
import com.damoyeo.api.domain.board.service.BoardService;
import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
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
 * 모임 게시판 컨트롤러
 *
 * <p>모임 게시판 관련 REST API 엔드포인트를 제공합니다.</p>
 *
 * <p>엔드포인트:</p>
 * <ul>
 *   <li>POST   /api/groups/{groupId}/board          - 게시글 작성</li>
 *   <li>GET    /api/groups/{groupId}/board          - 게시글 목록 (페이지네이션)</li>
 *   <li>GET    /api/groups/{groupId}/board/{postId} - 게시글 상세</li>
 *   <li>DELETE /api/board/posts/{postId}            - 게시글 삭제</li>
 *   <li>POST   /api/board/posts/{postId}/like       - 좋아요 토글</li>
 *   <li>GET    /api/board/posts/{postId}/comments   - 댓글 목록</li>
 *   <li>POST   /api/board/posts/{postId}/comments   - 댓글 작성</li>
 *   <li>DELETE /api/board/comments/{commentId}      - 댓글 삭제</li>
 * </ul>
 *
 * <p>권한: 모임 멤버만 접근 가능 (공지사항 작성은 OWNER/MANAGER)</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Board", description = "모임 게시판 API")
public class BoardController {

    private final BoardService boardService;

    // 게시글 API

    /**
     * 게시글 작성
     *
     * <p>텍스트와 이미지(최대 5개)를 포함한 게시글을 작성합니다.</p>
     * <p>공지사항(NOTICE)은 OWNER/MANAGER만 작성 가능합니다.</p>
     *
     * @param groupId  모임 ID
     * @param category 카테고리 (GREETING, REVIEW, FREE, NOTICE)
     * @param title    제목
     * @param content  본문
     * @param files    첨부 이미지 (선택, 최대 5개)
     * @param member   인증된 사용자
     * @return 생성된 게시글 정보
     */
    @PostMapping(value = "/groups/{groupId}/board", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 작성", description = "모임 게시판에 게시글을 작성합니다.")
    public ResponseEntity<BoardPostDTO> createPost(
            @PathVariable Long groupId,
            @RequestParam("category") BoardCategory category,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Board post create - groupId: {}, category: {}, user: {}", groupId, category, member.getEmail());

        BoardPostDTO post = boardService.createPost(groupId, category, title, content, files, member.getEmail());

        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 목록 조회 (페이지네이션)
     *
     * <p>카테고리 필터를 적용하여 게시글 목록을 조회합니다.</p>
     * <p>공지(isPinned=true) 먼저, 최신순으로 정렬됩니다.</p>
     *
     * @param groupId  모임 ID
     * @param page     페이지 번호 (0부터 시작)
     * @param size     페이지 크기 (기본 10)
     * @param category 카테고리 필터 (없으면 전체)
     * @param member   인증된 사용자
     * @return 페이지네이션된 게시글 목록
     */
    @GetMapping("/groups/{groupId}/board")
    @Operation(summary = "게시글 목록 조회", description = "모임 게시판 게시글 목록을 조회합니다.")
    public ResponseEntity<PageResponseDTO<BoardPostDTO>> getPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) BoardCategory category,
            @AuthenticationPrincipal MemberDTO member) {

        PageResponseDTO<BoardPostDTO> posts = boardService.getPosts(groupId, page, size, category, member.getEmail());

        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회
     *
     * @param groupId 모임 ID
     * @param postId  게시글 ID
     * @param member  인증된 사용자
     * @return 게시글 상세 정보
     */
    @GetMapping("/groups/{groupId}/board/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
    public ResponseEntity<BoardPostDTO> getPost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        BoardPostDTO post = boardService.getPost(groupId, postId, member.getEmail());

        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 삭제
     *
     * <p>작성자 본인 또는 운영진(OWNER/MANAGER)만 삭제 가능합니다.</p>
     *
     * @param postId 삭제할 게시글 ID
     * @param member 인증된 사용자
     * @return 성공 메시지
     */
    @DeleteMapping("/board/posts/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (작성자 또는 운영진만 가능)")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Board post delete - postId: {}, user: {}", postId, member.getEmail());

        boardService.deletePost(postId, member.getEmail());

        return ResponseEntity.ok(Map.of("message", "게시글이 삭제되었습니다."));
    }

    // 좋아요 API

    /**
     * 게시글 좋아요 토글
     *
     * <p>이미 좋아요한 경우 취소, 아닌 경우 추가합니다.</p>
     *
     * @param postId 게시글 ID
     * @param member 인증된 사용자
     * @return 좋아요 상태 및 개수
     */
    @PostMapping("/board/posts/{postId}/like")
    @Operation(summary = "좋아요 토글", description = "게시글에 좋아요를 추가하거나 취소합니다.")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Board like toggle - postId: {}, user: {}", postId, member.getEmail());

        boolean liked = boardService.toggleLike(postId, member.getEmail());
        long likeCount = boardService.getLikeCount(postId);

        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", likeCount));
    }

    // 댓글 API

    /**
     * 댓글 목록 조회
     *
     * @param postId 게시글 ID
     * @param member 인증된 사용자
     * @return 댓글 목록 (오래된 순)
     */
    @GetMapping("/board/posts/{postId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다.")
    public ResponseEntity<List<BoardCommentDTO>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberDTO member) {

        List<BoardCommentDTO> comments = boardService.getComments(postId, member.getEmail());

        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     *
     * @param postId  게시글 ID
     * @param request 댓글 내용 (content 필드)
     * @param member  인증된 사용자
     * @return 작성된 댓글
     */
    @PostMapping("/board/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public ResponseEntity<BoardCommentDTO> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal MemberDTO member) {

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        log.info("Board comment add - postId: {}, user: {}", postId, member.getEmail());

        BoardCommentDTO comment = boardService.addComment(postId, content.trim(), member.getEmail());

        return ResponseEntity.ok(comment);
    }

    /**
     * 댓글 삭제
     *
     * <p>작성자 본인 또는 운영진(OWNER/MANAGER)만 삭제 가능합니다.</p>
     *
     * @param commentId 삭제할 댓글 ID
     * @param member    인증된 사용자
     * @return 성공 메시지
     */
    @DeleteMapping("/board/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (작성자 또는 운영진만 가능)")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDTO member) {

        log.info("Board comment delete - commentId: {}, user: {}", commentId, member.getEmail());

        boardService.deleteComment(commentId, member.getEmail());

        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }
}
