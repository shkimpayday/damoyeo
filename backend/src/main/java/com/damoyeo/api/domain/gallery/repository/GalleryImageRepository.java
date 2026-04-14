package com.damoyeo.api.domain.gallery.repository;

import com.damoyeo.api.domain.gallery.entity.GalleryImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 갤러리 이미지 레포지토리
 *
 * GalleryImage 엔티티의 데이터 접근을 담당합니다.
 *
 * [주요 쿼리]
 * - 모임별 이미지 목록 조회 (페이지네이션)
 * - 모임별 이미지 개수 조회
 * - 이미지 상세 조회 (업로더 정보 포함)
 */
@Repository
public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    /**
     * 모임별 갤러리 이미지 목록 조회 (페이지네이션)
     *
     * [정렬]
     * 최신순으로 정렬 (createdAt DESC)
     *
     * @param groupId 모임 ID
     * @param pageable 페이지 정보
     * @return 페이지네이션된 이미지 목록
     */
    @Query("SELECT g FROM GalleryImage g " +
            "LEFT JOIN FETCH g.uploader " +
            "WHERE g.group.id = :groupId " +
            "ORDER BY g.createdAt DESC")
    List<GalleryImage> findByGroupIdWithUploader(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 모임별 갤러리 이미지 목록 조회 (페이지 객체 반환)
     */
    Page<GalleryImage> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * 모임별 이미지 개수 조회
     *
     * @param groupId 모임 ID
     * @return 이미지 개수
     */
    long countByGroupId(Long groupId);

    /**
     * 이미지 상세 조회 (업로더, 모임 정보 포함)
     *
     * @param imageId 이미지 ID
     * @return 이미지 정보 (Optional)
     */
    @Query("SELECT g FROM GalleryImage g " +
            "LEFT JOIN FETCH g.uploader " +
            "LEFT JOIN FETCH g.group " +
            "WHERE g.id = :imageId")
    Optional<GalleryImage> findByIdWithDetails(@Param("imageId") Long imageId);

    /**
     * 특정 회원이 업로드한 이미지 목록 조회
     *
     * @param uploaderId 업로더 회원 ID
     * @return 이미지 목록
     */
    List<GalleryImage> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId);

    /**
     * 모임의 최신 이미지 N개 조회 (미리보기용)
     *
     * @param groupId 모임 ID
     * @param pageable 페이지 정보 (size로 N개 제한)
     * @return 최신 이미지 목록
     */
    @Query("SELECT g FROM GalleryImage g " +
            "WHERE g.group.id = :groupId " +
            "ORDER BY g.createdAt DESC")
    List<GalleryImage> findRecentByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 모임 삭제 시 갤러리 이미지 전체 삭제용
     */
    void deleteAllByGroupId(Long groupId);
}
