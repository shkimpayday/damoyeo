package com.damoyeo.api.domain.board.service;

import com.damoyeo.api.domain.board.dto.BoardCommentDTO;
import com.damoyeo.api.domain.board.dto.BoardPostDTO;
import com.damoyeo.api.domain.board.dto.BoardPostDTO.BoardImageSimpleDTO;
import com.damoyeo.api.domain.board.entity.*;
import com.damoyeo.api.domain.board.repository.*;
import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.group.entity.GroupMember;
import com.damoyeo.api.domain.group.entity.GroupRole;
import com.damoyeo.api.domain.group.repository.GroupMemberRepository;
import com.damoyeo.api.domain.group.repository.GroupRepository;
import com.damoyeo.api.domain.member.dto.MemberSummaryDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import com.damoyeo.api.global.exception.CustomException;
import com.damoyeo.api.global.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * 모임 게시판 서비스 구현체
 * ============================================================================
 *
 * <p>모임 게시판 관련 비즈니스 로직을 구현합니다.</p>
 *
 * <p>권한 정책:</p>
 * <ul>
 *   <li>게시글 작성: 모임 멤버 (NOTICE는 OWNER/MANAGER만)</li>
 *   <li>게시글 조회: 모임 멤버</li>
 *   <li>게시글 삭제: 작성자 본인 또는 OWNER/MANAGER</li>
 *   <li>댓글 작성/삭제: 동일 정책</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardPostRepository boardPostRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final FileUploadUtil fileUploadUtil;

    /** 한 게시글에 첨부 가능한 최대 이미지 수 */
    private static final int MAX_IMAGE_COUNT = 5;

    // ========================================================================
    // 게시글 관련 메서드
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BoardPostDTO createPost(Long groupId, BoardCategory category, String title,
                                   String content, List<MultipartFile> files, String email) {
        // 1. 모임 조회
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 작성자 조회
        Member author = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, author.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 게시글을 작성할 수 있습니다.", HttpStatus.FORBIDDEN));

        // 4. 공지사항은 운영진만 작성 가능
        if (category == BoardCategory.NOTICE) {
            boolean isStaff = membership.getRole() == GroupRole.OWNER
                    || membership.getRole() == GroupRole.MANAGER;
            if (!isStaff) {
                throw new CustomException("공지사항은 운영진만 작성할 수 있습니다.", HttpStatus.FORBIDDEN);
            }
        }

        // 5. 이미지 개수 확인
        if (files != null && files.size() > MAX_IMAGE_COUNT) {
            throw new CustomException(
                    "이미지는 최대 " + MAX_IMAGE_COUNT + "개까지 첨부할 수 있습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 6. 게시글 생성
        BoardPost post = BoardPost.builder()
                .group(group)
                .author(author)
                .category(category)
                .title(title.trim())
                .content(content.trim())
                .isPinned(category == BoardCategory.NOTICE) // 공지사항은 자동 고정
                .build();

        // 7. 이미지 업로드 및 연결
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                String imageUrl = fileUploadUtil.uploadBoardImage(file, groupId);

                BoardImage image = BoardImage.builder()
                        .imageUrl(imageUrl)
                        .originalFileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .build();

                post.addImage(image);
            }
        }

        // 8. 저장
        BoardPost saved = boardPostRepository.save(post);

        log.info("Board post created - groupId: {}, postId: {}, category: {}, author: {}",
                groupId, saved.getId(), category, email);

        return toPostDTO(saved, true, author.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponseDTO<BoardPostDTO> getPosts(Long groupId, int page, int size,
                                                   BoardCategory category, String email) {
        // 1. 모임 존재 확인
        if (!groupRepository.existsById(groupId)) {
            throw new CustomException("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        // 2. 조회자 정보
        Member viewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, viewer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 게시판을 볼 수 있습니다.", HttpStatus.FORBIDDEN));

        // 4. PageRequestDTO 생성 (프론트는 0-based → 1-based 변환)
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(page + 1)
                .size(size)
                .build();

        // 5. 페이지네이션 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<BoardPost> postPage = boardPostRepository.findByGroupIdAndCategory(groupId, category, pageable);

        // 6. DTO 변환
        boolean isManager = membership.getRole() == GroupRole.OWNER
                || membership.getRole() == GroupRole.MANAGER;

        List<BoardPostDTO> dtoList = postPage.getContent().stream()
                .map(post -> {
                    boolean canDelete = isManager || post.getAuthor().getId().equals(viewer.getId());
                    return toPostDTO(post, canDelete, viewer.getId());
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<BoardPostDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) postPage.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoardPostDTO getPost(Long groupId, Long postId, String email) {
        // 1. 게시글 조회
        BoardPost post = boardPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 조회자 정보
        Member viewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, viewer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 게시글을 볼 수 있습니다.", HttpStatus.FORBIDDEN));

        boolean isManager = membership.getRole() == GroupRole.OWNER
                || membership.getRole() == GroupRole.MANAGER;
        boolean canDelete = isManager || post.getAuthor().getId().equals(viewer.getId());

        return toPostDTO(post, canDelete, viewer.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletePost(Long postId, String email) {
        // 1. 게시글 조회
        BoardPost post = boardPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 요청자 조회
        Member requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 권한 확인 (작성자 본인 또는 관리자)
        boolean isAuthor = post.getAuthor().getId().equals(requester.getId());
        boolean isManager = false;

        if (!isAuthor) {
            GroupMember membership = groupMemberRepository
                    .findByGroupIdAndMemberId(post.getGroup().getId(), requester.getId())
                    .orElse(null);
            if (membership != null) {
                isManager = membership.getRole() == GroupRole.OWNER
                        || membership.getRole() == GroupRole.MANAGER;
            }
        }

        if (!isAuthor && !isManager) {
            throw new CustomException("게시글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 4. 관련 좋아요 / 댓글 삭제
        boardLikeRepository.deleteAllByPostId(postId);
        boardCommentRepository.deleteAllByPostId(postId);

        // 5. 이미지 파일 삭제
        for (BoardImage image : post.getImages()) {
            fileUploadUtil.deleteFile(image.getImageUrl());
        }

        // 6. DB 삭제 (cascade로 BoardImage도 삭제됨)
        boardPostRepository.delete(post);

        log.info("Board post deleted - postId: {}, deletedBy: {}", postId, email);
    }

    // ========================================================================
    // 좋아요 관련 메서드
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean toggleLike(Long postId, String email) {
        // 1. 게시글 조회
        BoardPost post = boardPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), member.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 좋아요를 누를 수 있습니다.", HttpStatus.FORBIDDEN));

        // 4. 좋아요 토글
        Optional<BoardLike> existingLike = boardLikeRepository.findByPostIdAndMemberId(postId, member.getId());

        if (existingLike.isPresent()) {
            boardLikeRepository.delete(existingLike.get());
            log.info("Board like removed - postId: {}, member: {}", postId, email);
            return false;
        } else {
            BoardLike like = BoardLike.builder().post(post).member(member).build();
            boardLikeRepository.save(like);
            log.info("Board like added - postId: {}, member: {}", postId, email);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLikeCount(Long postId) {
        return boardLikeRepository.countByPostId(postId);
    }

    // ========================================================================
    // 댓글 관련 메서드
    // ========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BoardCommentDTO> getComments(Long postId, String email) {
        // 1. 게시글 조회
        BoardPost post = boardPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 조회자 정보
        Member viewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), viewer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 댓글을 볼 수 있습니다.", HttpStatus.FORBIDDEN));

        boolean isManager = membership.getRole() == GroupRole.OWNER
                || membership.getRole() == GroupRole.MANAGER;

        // 4. 댓글 목록 조회
        List<BoardComment> comments = boardCommentRepository.findByPostIdWithAuthor(postId);

        return comments.stream()
                .map(comment -> {
                    boolean canDelete = isManager || comment.getAuthor().getId().equals(viewer.getId());
                    return toCommentDTO(comment, canDelete);
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BoardCommentDTO addComment(Long postId, String content, String email) {
        // 1. 게시글 조회
        BoardPost post = boardPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 작성자 조회
        Member author = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 모임 멤버 권한 확인
        groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), author.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 댓글을 작성할 수 있습니다.", HttpStatus.FORBIDDEN));

        // 4. 내용 검증
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException("댓글 내용을 입력해주세요.", HttpStatus.BAD_REQUEST);
        }
        if (content.length() > 500) {
            throw new CustomException("댓글은 500자 이하로 작성해주세요.", HttpStatus.BAD_REQUEST);
        }

        // 5. 댓글 생성
        BoardComment comment = BoardComment.builder()
                .post(post)
                .author(author)
                .content(content.trim())
                .build();

        BoardComment saved = boardCommentRepository.save(comment);
        log.info("Board comment added - postId: {}, commentId: {}, author: {}", postId, saved.getId(), email);

        return toCommentDTO(saved, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId, String email) {
        // 1. 댓글 조회
        BoardComment comment = boardCommentRepository.findByIdWithDetails(commentId)
                .orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 요청자 조회
        Member requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 3. 권한 확인 (작성자 본인 또는 관리자)
        boolean isAuthor = comment.getAuthor().getId().equals(requester.getId());
        boolean isManager = false;

        if (!isAuthor) {
            GroupMember membership = groupMemberRepository
                    .findByGroupIdAndMemberId(comment.getPost().getGroup().getId(), requester.getId())
                    .orElse(null);
            if (membership != null) {
                isManager = membership.getRole() == GroupRole.OWNER
                        || membership.getRole() == GroupRole.MANAGER;
            }
        }

        if (!isAuthor && !isManager) {
            throw new CustomException("댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        boardCommentRepository.delete(comment);
        log.info("Board comment deleted - commentId: {}, deletedBy: {}", commentId, email);
    }

    // ========================================================================
    // DTO 변환 메서드
    // ========================================================================

    /**
     * 게시글 엔티티 → DTO 변환
     *
     * @param post 게시글 엔티티
     * @param canDelete 삭제 가능 여부
     * @param viewerId 조회자 ID (좋아요 여부 확인용, null이면 미확인)
     * @return BoardPostDTO
     */
    private BoardPostDTO toPostDTO(BoardPost post, boolean canDelete, Long viewerId) {
        MemberSummaryDTO authorDTO = null;
        if (post.getAuthor() != null) {
            authorDTO = MemberSummaryDTO.builder()
                    .id(post.getAuthor().getId())
                    .nickname(post.getAuthor().getNickname())
                    .profileImage(post.getAuthor().getProfileImage())
                    .build();
        }

        List<BoardImageSimpleDTO> imageDTOs = post.getImages() == null
                ? Collections.emptyList()
                : post.getImages().stream()
                    .map(img -> BoardImageSimpleDTO.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .originalFileName(img.getOriginalFileName())
                            .fileSize(img.getFileSize())
                            .build())
                    .collect(Collectors.toList());

        long likeCount = boardLikeRepository.countByPostId(post.getId());
        long commentCount = boardCommentRepository.countByPostId(post.getId());
        boolean liked = viewerId != null && boardLikeRepository.existsByPostIdAndMemberId(post.getId(), viewerId);

        return BoardPostDTO.builder()
                .id(post.getId())
                .groupId(post.getGroup().getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .images(imageDTOs)
                .imageCount(post.getImageCount())
                .thumbnailUrl(post.getThumbnailUrl())
                .author(authorDTO)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .liked(liked)
                .isPinned(post.isPinned())
                .canDelete(canDelete)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getModifiedAt())
                .build();
    }

    /**
     * 댓글 엔티티 → DTO 변환
     */
    private BoardCommentDTO toCommentDTO(BoardComment comment, boolean canDelete) {
        MemberSummaryDTO authorDTO = null;
        if (comment.getAuthor() != null) {
            authorDTO = MemberSummaryDTO.builder()
                    .id(comment.getAuthor().getId())
                    .nickname(comment.getAuthor().getNickname())
                    .profileImage(comment.getAuthor().getProfileImage())
                    .build();
        }

        return BoardCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(authorDTO)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .canDelete(canDelete)
                .build();
    }
}
