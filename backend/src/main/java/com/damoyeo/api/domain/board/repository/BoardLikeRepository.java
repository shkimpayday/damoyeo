package com.damoyeo.api.domain.board.repository;

import com.damoyeo.api.domain.board.entity.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 모임 게시판 좋아요 레포지토리
 */
@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {

    Optional<BoardLike> findByPostIdAndMemberId(Long postId, Long memberId);

    long countByPostId(Long postId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    void deleteAllByPostId(Long postId);
}
