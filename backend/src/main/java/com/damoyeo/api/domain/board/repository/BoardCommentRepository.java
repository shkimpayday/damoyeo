package com.damoyeo.api.domain.board.repository;

import com.damoyeo.api.domain.board.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 모임 게시판 댓글 레포지토리
 */
@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    /**
     * 게시글의 댓글 목록 조회 (작성자 정보 포함, 오래된 순)
     *
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    @Query("SELECT c FROM BoardComment c " +
           "LEFT JOIN FETCH c.author " +
           "LEFT JOIN FETCH c.post " +
           "WHERE c.post.id = :postId " +
           "ORDER BY c.createdAt ASC")
    List<BoardComment> findByPostIdWithAuthor(@Param("postId") Long postId);

    /**
     * 댓글 상세 조회 (게시글, 작성자 정보 포함)
     */
    @Query("SELECT c FROM BoardComment c " +
           "LEFT JOIN FETCH c.author " +
           "LEFT JOIN FETCH c.post p " +
           "LEFT JOIN FETCH p.group " +
           "WHERE c.id = :commentId")
    Optional<BoardComment> findByIdWithDetails(@Param("commentId") Long commentId);

    long countByPostId(Long postId);

    void deleteAllByPostId(Long postId);
}
