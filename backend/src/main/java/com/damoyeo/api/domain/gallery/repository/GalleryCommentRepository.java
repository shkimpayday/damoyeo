package com.damoyeo.api.domain.gallery.repository;

import com.damoyeo.api.domain.gallery.entity.GalleryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 갤러리 댓글 레포지토리
 *
 * [구조 변경]
 * - 기존: imageId 기준
 * - 변경: postId 기준 (여러 이미지를 묶은 게시물)
 */
@Repository
public interface GalleryCommentRepository extends JpaRepository<GalleryComment, Long> {

    /**
     * 특정 게시물의 댓글 목록 조회 (최신순)
     */
    @Query("SELECT c FROM GalleryComment c " +
            "LEFT JOIN FETCH c.writer " +
            "WHERE c.post.id = :postId " +
            "ORDER BY c.createdAt DESC")
    List<GalleryComment> findByPostIdWithWriter(@Param("postId") Long postId);

    /**
     * 특정 게시물의 댓글 목록 조회 (페이지네이션)
     */
    Page<GalleryComment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);

    /**
     * 특정 게시물의 댓글 개수
     */
    long countByPostId(Long postId);

    /**
     * 댓글 상세 조회 (작성자, 게시물 정보 포함)
     */
    @Query("SELECT c FROM GalleryComment c " +
            "LEFT JOIN FETCH c.writer " +
            "LEFT JOIN FETCH c.post p " +
            "LEFT JOIN FETCH p.group " +
            "WHERE c.id = :commentId")
    Optional<GalleryComment> findByIdWithDetails(@Param("commentId") Long commentId);

    /**
     * 게시물 삭제 시 관련 댓글 모두 삭제
     */
    void deleteAllByPostId(Long postId);
}
