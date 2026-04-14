package com.damoyeo.api.domain.gallery.service;

import com.damoyeo.api.domain.gallery.dto.GalleryCommentDTO;
import com.damoyeo.api.domain.gallery.dto.GalleryPostDTO;
import com.damoyeo.api.domain.gallery.dto.GalleryPostDTO.GalleryImageSimpleDTO;
import com.damoyeo.api.domain.gallery.entity.GalleryComment;
import com.damoyeo.api.domain.gallery.entity.GalleryImage;
import com.damoyeo.api.domain.gallery.entity.GalleryLike;
import com.damoyeo.api.domain.gallery.entity.GalleryPost;
import com.damoyeo.api.domain.gallery.repository.GalleryCommentRepository;
import com.damoyeo.api.domain.gallery.repository.GalleryImageRepository;
import com.damoyeo.api.domain.gallery.repository.GalleryLikeRepository;
import com.damoyeo.api.domain.gallery.repository.GalleryPostRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 갤러리 서비스 구현체
 *
 * 갤러리 게시물 관련 비즈니스 로직을 구현합니다.
 *
 * [구조 변경]
 * - 기존: 개별 이미지(GalleryImage) 단위
 * - 변경: 게시물(GalleryPost) 단위 (여러 이미지 묶음)
 *
 * [권한 검증]
 * - 업로드: 모임 멤버만 가능
 * - 조회: 모임 멤버만 가능
 * - 삭제: 업로더 본인 또는 관리자(OWNER/MANAGER)만 가능
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {

    private final GalleryPostRepository galleryPostRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final GalleryLikeRepository galleryLikeRepository;
    private final GalleryCommentRepository galleryCommentRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final FileUploadUtil fileUploadUtil;

    /** 한 번에 업로드 가능한 최대 이미지 수 */
    private static final int MAX_UPLOAD_COUNT = 10;

    // 게시물 관련 메서드

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GalleryPostDTO uploadPost(Long groupId, List<MultipartFile> files,
                                      String caption, String email) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member uploader = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, uploader.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 게시물을 올릴 수 있습니다.", HttpStatus.FORBIDDEN));

        if (files == null || files.isEmpty()) {
            throw new CustomException("업로드할 파일이 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (files.size() > MAX_UPLOAD_COUNT) {
            throw new CustomException(
                    "한 번에 최대 " + MAX_UPLOAD_COUNT + "개의 이미지만 업로드할 수 있습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        GalleryPost post = GalleryPost.builder()
                .group(group)
                .uploader(uploader)
                .caption(caption)
                .build();

        for (MultipartFile file : files) {
            // 파일 업로드
            String imageUrl = fileUploadUtil.uploadGalleryImage(file, groupId);

            // 이미지 엔티티 생성
            GalleryImage galleryImage = GalleryImage.builder()
                    .group(group)
                    .uploader(uploader)
                    .imageUrl(imageUrl)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .build();

            // 게시물에 이미지 추가
            post.addImage(galleryImage);
        }

        GalleryPost saved = galleryPostRepository.save(post);

        log.info("Gallery post uploaded - groupId: {}, postId: {}, imageCount: {}, uploader: {}",
                groupId, saved.getId(), saved.getImageCount(), email);

        return toPostDTO(saved, true, uploader.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponseDTO<GalleryPostDTO> getGalleryPosts(Long groupId, int page, int size, String email) {
        if (!groupRepository.existsById(groupId)) {
            throw new CustomException("모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }

        Member viewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(groupId, viewer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 갤러리를 볼 수 있습니다.", HttpStatus.FORBIDDEN));

        // 프론트엔드에서 0-based로 요청하므로 1-based로 변환
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(page + 1)
                .size(size)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<GalleryPost> postPage = galleryPostRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);

        boolean isManager = membership.getRole() == GroupRole.OWNER
                || membership.getRole() == GroupRole.MANAGER;

        List<GalleryPostDTO> dtoList = postPage.getContent().stream()
                .map(post -> {
                    boolean canDelete = isManager || post.getUploader().getId().equals(viewer.getId());
                    return toPostDTO(post, canDelete, viewer.getId());
                })
                .collect(Collectors.toList());

        return PageResponseDTO.<GalleryPostDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount((int) postPage.getTotalElements())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPostCount(Long groupId) {
        return galleryPostRepository.countByGroupId(groupId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletePost(Long postId, String email) {
        GalleryPost post = galleryPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        boolean isUploader = post.getUploader().getId().equals(requester.getId());
        boolean isManager = false;

        if (!isUploader) {
            GroupMember membership = groupMemberRepository
                    .findByGroupIdAndMemberId(post.getGroup().getId(), requester.getId())
                    .orElse(null);

            if (membership != null) {
                isManager = membership.getRole() == GroupRole.OWNER
                        || membership.getRole() == GroupRole.MANAGER;
            }
        }

        if (!isUploader && !isManager) {
            throw new CustomException("게시물을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        galleryLikeRepository.deleteAllByPostId(postId);

        galleryCommentRepository.deleteAllByPostId(postId);

        for (GalleryImage image : post.getImages()) {
            fileUploadUtil.deleteFile(image.getImageUrl());
        }

        galleryPostRepository.delete(post);

        log.info("Gallery post deleted - postId: {}, imageCount: {}, deletedBy: {}",
                postId, post.getImageCount(), email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GalleryPostDTO> getRecentPosts(Long groupId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<GalleryPost> posts = galleryPostRepository.findRecentByGroupId(groupId, pageable);

        return posts.stream()
                .map(post -> toPostDTO(post, false, null))
                .collect(Collectors.toList());
    }

    // 좋아요 관련 메서드

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean toggleLike(Long postId, String email) {
        GalleryPost post = galleryPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), member.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 좋아요를 누를 수 있습니다.", HttpStatus.FORBIDDEN));

        Optional<GalleryLike> existingLike = galleryLikeRepository.findByPostIdAndMemberId(postId, member.getId());

        if (existingLike.isPresent()) {
            // 좋아요 취소
            galleryLikeRepository.delete(existingLike.get());
            log.info("Gallery like removed - postId: {}, member: {}", postId, email);
            return false;
        } else {
            // 좋아요 추가
            GalleryLike like = GalleryLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            galleryLikeRepository.save(like);
            log.info("Gallery like added - postId: {}, member: {}", postId, email);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLikeCount(Long postId) {
        return galleryLikeRepository.countByPostId(postId);
    }

    // 댓글 관련 메서드

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public GalleryCommentDTO addComment(Long postId, String content, String email) {
        GalleryPost post = galleryPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), writer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 댓글을 작성할 수 있습니다.", HttpStatus.FORBIDDEN));

        if (content == null || content.trim().isEmpty()) {
            throw new CustomException("댓글 내용을 입력해주세요.", HttpStatus.BAD_REQUEST);
        }
        if (content.length() > 500) {
            throw new CustomException("댓글은 500자 이하로 작성해주세요.", HttpStatus.BAD_REQUEST);
        }

        GalleryComment comment = GalleryComment.builder()
                .post(post)
                .writer(writer)
                .content(content.trim())
                .build();

        GalleryComment saved = galleryCommentRepository.save(comment);
        log.info("Gallery comment added - postId: {}, commentId: {}, writer: {}", postId, saved.getId(), email);

        return toCommentDTO(saved, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GalleryCommentDTO> getComments(Long postId, String email) {
        GalleryPost post = galleryPostRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new CustomException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member viewer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(post.getGroup().getId(), viewer.getId())
                .orElseThrow(() -> new CustomException("모임 멤버만 댓글을 볼 수 있습니다.", HttpStatus.FORBIDDEN));

        boolean isManager = membership.getRole() == GroupRole.OWNER || membership.getRole() == GroupRole.MANAGER;

        List<GalleryComment> comments = galleryCommentRepository.findByPostIdWithWriter(postId);

        return comments.stream()
                .map(comment -> {
                    boolean canDelete = isManager || comment.getWriter().getId().equals(viewer.getId());
                    return toCommentDTO(comment, canDelete);
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteComment(Long commentId, String email) {
        GalleryComment comment = galleryCommentRepository.findByIdWithDetails(commentId)
                .orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Member requester = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        boolean isWriter = comment.getWriter().getId().equals(requester.getId());
        boolean isManager = false;

        if (!isWriter) {
            GroupMember membership = groupMemberRepository
                    .findByGroupIdAndMemberId(comment.getPost().getGroup().getId(), requester.getId())
                    .orElse(null);

            if (membership != null) {
                isManager = membership.getRole() == GroupRole.OWNER
                        || membership.getRole() == GroupRole.MANAGER;
            }
        }

        if (!isWriter && !isManager) {
            throw new CustomException("댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        galleryCommentRepository.delete(comment);
        log.info("Gallery comment deleted - commentId: {}, deletedBy: {}", commentId, email);
    }

    // DTO 변환 메서드

    /**
     * 게시물 엔티티를 DTO로 변환
     *
     * @param post 갤러리 게시물 엔티티
     * @param canDelete 삭제 가능 여부
     * @param viewerId 조회자 ID (좋아요 여부 확인용, null이면 미확인)
     * @return DTO
     */
    private GalleryPostDTO toPostDTO(GalleryPost post, boolean canDelete, Long viewerId) {
        // 업로더 정보
        MemberSummaryDTO uploaderDTO = null;
        if (post.getUploader() != null) {
            uploaderDTO = MemberSummaryDTO.builder()
                    .id(post.getUploader().getId())
                    .nickname(post.getUploader().getNickname())
                    .profileImage(post.getUploader().getProfileImage())
                    .build();
        }

        // 이미지 목록
        List<GalleryImageSimpleDTO> imageDTOs = post.getImages().stream()
                .map(image -> GalleryImageSimpleDTO.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .originalFileName(image.getOriginalFileName())
                        .fileSize(image.getFileSize())
                        .build())
                .collect(Collectors.toList());

        // 좋아요/댓글 개수 조회
        long likeCount = galleryLikeRepository.countByPostId(post.getId());
        long commentCount = galleryCommentRepository.countByPostId(post.getId());

        // 현재 사용자가 좋아요를 눌렀는지 확인
        boolean liked = false;
        if (viewerId != null) {
            liked = galleryLikeRepository.existsByPostIdAndMemberId(post.getId(), viewerId);
        }

        return GalleryPostDTO.builder()
                .id(post.getId())
                .groupId(post.getGroup().getId())
                .caption(post.getCaption())
                .uploader(uploaderDTO)
                .images(imageDTOs)
                .imageCount(post.getImageCount())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdAt(post.getCreatedAt())
                .canDelete(canDelete)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .liked(liked)
                .build();
    }

    /**
     * 댓글 엔티티를 DTO로 변환
     */
    private GalleryCommentDTO toCommentDTO(GalleryComment comment, boolean canDelete) {
        MemberSummaryDTO writerDTO = null;
        if (comment.getWriter() != null) {
            writerDTO = MemberSummaryDTO.builder()
                    .id(comment.getWriter().getId())
                    .nickname(comment.getWriter().getNickname())
                    .profileImage(comment.getWriter().getProfileImage())
                    .build();
        }

        return GalleryCommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .writer(writerDTO)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .canDelete(canDelete)
                .build();
    }
}
