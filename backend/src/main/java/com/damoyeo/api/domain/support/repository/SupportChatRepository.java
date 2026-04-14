package com.damoyeo.api.domain.support.repository;

import com.damoyeo.api.domain.support.entity.SupportChat;
import com.damoyeo.api.domain.support.entity.SupportChatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 상담 채팅 Repository
 *
 */
public interface SupportChatRepository extends JpaRepository<SupportChat, Long> {

    /**
     * 사용자의 상담 목록 조회
     *
     * @param userId 사용자 ID
     * @return 사용자의 상담 목록 (최신순)
     */
    @Query("select sc from SupportChat sc " +
            "left join fetch sc.user " +
            "left join fetch sc.admin " +
            "where sc.user.id = :userId " +
            "order by sc.createdAt desc")
    List<SupportChat> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 활성 상담 조회 (WAITING 또는 IN_PROGRESS 상태)
     *
     * <p>JPQL에서 enum 비교는 string 리터럴이 아닌 파라미터로 전달해야 합니다.</p>
     *
     * @param userId   사용자 ID
     * @param statuses 조회할 상태 목록 (WAITING, IN_PROGRESS)
     * @return 진행 중인 상담 (없으면 Optional.empty)
     */
    @Query("select sc from SupportChat sc " +
            "where sc.user.id = :userId " +
            "and sc.status in :statuses " +
            "order by sc.createdAt desc " +
            "limit 1")
    Optional<SupportChat> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("statuses") Collection<SupportChatStatus> statuses
    );

    /**
     * 상태별 상담 목록 조회 (관리자용)
     *
     * @param status 상담 상태
     * @param pageable 페이지 정보
     * @return 해당 상태의 상담 목록
     */
    @Query("select sc from SupportChat sc " +
            "left join fetch sc.user " +
            "left join fetch sc.admin " +
            "where sc.status = :status " +
            "order by sc.createdAt asc")
    Page<SupportChat> findByStatus(
            @Param("status") SupportChatStatus status,
            Pageable pageable
    );

    /**
     * 대기 중인 상담 개수 조회
     *
     * @return 대기 중인 상담 개수
     */
    long countByStatus(SupportChatStatus status);

    /**
     * 관리자가 담당한 상담 목록 조회
     *
     * @param adminId 관리자 ID
     * @param pageable 페이지 정보
     * @return 관리자가 담당한 상담 목록
     */
    @Query("select sc from SupportChat sc " +
            "left join fetch sc.user " +
            "where sc.admin.id = :adminId " +
            "order by sc.createdAt desc")
    Page<SupportChat> findByAdminId(
            @Param("adminId") Long adminId,
            Pageable pageable
    );

    /**
     * 모든 상담 목록 조회 (관리자용)
     */
    @Query("select sc from SupportChat sc " +
            "left join fetch sc.user " +
            "left join fetch sc.admin " +
            "order by " +
            "case sc.status when 'WAITING' then 0 when 'IN_PROGRESS' then 1 else 2 end, " +
            "sc.createdAt asc")
    Page<SupportChat> findAllWithDetails(Pageable pageable);
}
