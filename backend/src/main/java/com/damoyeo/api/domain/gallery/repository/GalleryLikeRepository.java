package com.damoyeo.api.domain.gallery.repository;

import com.damoyeo.api.domain.gallery.entity.GalleryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 갤러리 좋아요 레포지토리
 *
 * [구조 변경]
 * - 기존: imageId 기준
 * - 변경: postId 기준 (여러 이미지를 묶은 게시물)
 */
@Repository
public interface GalleryLikeRepository extends JpaRepository<GalleryLike, Long> {

    /**
     * 특정 게시물에 대한 사용자의 좋아요 조회
     */
    Optional<GalleryLike> findByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 특정 게시물에 대한 사용자의 좋아요 여부 확인
     */
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 특정 게시물의 좋아요 개수
     */
    long countByPostId(Long postId);

    /**
     * 특정 게시물에 대한 사용자의 좋아요 삭제
     */
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 게시물 삭제 시 관련 좋아요 모두 삭제
     */
    void deleteAllByPostId(Long postId);
}
