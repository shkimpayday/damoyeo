package com.damoyeo.api.domain.gallery.repository;

import com.damoyeo.api.domain.gallery.entity.GalleryPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 갤러리 게시물 레포지토리
 *
 * GalleryPost 엔티티의 데이터 접근을 담당합니다.
 *
 * [주요 쿼리]
 * - 모임별 게시물 목록 조회 (페이지네이션)
 * - 게시물 상세 조회 (이미지, 업로더 정보 포함)
 * - 모임별 게시물 개수 조회
 */
@Repository
public interface GalleryPostRepository extends JpaRepository<GalleryPost, Long> {

    /**
     * 모임별 갤러리 게시물 목록 조회 (페이지네이션)
     *
     * <p>이미지와 업로더 정보를 함께 조회합니다.</p>
     *
     * @param groupId 모임 ID
     * @param pageable 페이지 정보
     * @return 페이지네이션된 게시물 목록
     */
    @Query("SELECT DISTINCT p FROM GalleryPost p " +
            "LEFT JOIN FETCH p.uploader " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.group.id = :groupId " +
            "ORDER BY p.createdAt DESC")
    List<GalleryPost> findByGroupIdWithDetails(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 모임별 게시물 목록 조회 (페이지 객체 반환)
     */
    Page<GalleryPost> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * 모임별 게시물 개수 조회
     *
     * @param groupId 모임 ID
     * @return 게시물 개수
     */
    long countByGroupId(Long groupId);

    /**
     * 게시물 상세 조회 (이미지, 업로더, 모임 정보 포함)
     *
     * @param postId 게시물 ID
     * @return 게시물 정보 (Optional)
     */
    @Query("SELECT p FROM GalleryPost p " +
            "LEFT JOIN FETCH p.uploader " +
            "LEFT JOIN FETCH p.group " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :postId")
    Optional<GalleryPost> findByIdWithDetails(@Param("postId") Long postId);

    /**
     * 특정 회원이 업로드한 게시물 목록 조회
     *
     * @param uploaderId 업로더 회원 ID
     * @return 게시물 목록
     */
    @Query("SELECT p FROM GalleryPost p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.uploader.id = :uploaderId " +
            "ORDER BY p.createdAt DESC")
    List<GalleryPost> findByUploaderIdWithImages(@Param("uploaderId") Long uploaderId);

    /**
     * 모임의 최신 게시물 N개 조회 (미리보기용)
     *
     * @param groupId 모임 ID
     * @param pageable 페이지 정보 (size로 N개 제한)
     * @return 최신 게시물 목록
     */
    @Query("SELECT p FROM GalleryPost p " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.group.id = :groupId " +
            "ORDER BY p.createdAt DESC")
    List<GalleryPost> findRecentByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 모임 삭제 시 갤러리 게시물 전체 삭제용
     */
    void deleteAllByGroupId(Long groupId);
}
