package com.damoyeo.api.domain.board.repository;

import com.damoyeo.api.domain.board.entity.BoardCategory;
import com.damoyeo.api.domain.board.entity.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 모임 게시판 게시글 레포지토리
 */
@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    /**
     * 모임별 게시글 목록 조회 (카테고리 필터)
     *
     * <p>공지(isPinned=true) 먼저, 그 다음 최신순으로 정렬합니다.</p>
     *
     * @param groupId 모임 ID
     * @param category 카테고리 필터 (null이면 전체)
     * @param pageable 페이지 정보
     * @return 페이지네이션된 게시글 목록
     */
    @Query("SELECT p FROM BoardPost p " +
           "WHERE p.group.id = :groupId " +
           "AND (:category IS NULL OR p.category = :category) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<BoardPost> findByGroupIdAndCategory(
            @Param("groupId") Long groupId,
            @Param("category") BoardCategory category,
            Pageable pageable);

    /**
     * 게시글 상세 조회 (이미지, 작성자, 모임 정보 포함)
     *
     * @param postId 게시글 ID
     * @return 게시글 정보
     */
    @Query("SELECT p FROM BoardPost p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.group " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.id = :postId")
    Optional<BoardPost> findByIdWithDetails(@Param("postId") Long postId);

    /**
     * 모임 삭제 시 게시글 전체 삭제
     */
    void deleteAllByGroupId(Long groupId);
}
