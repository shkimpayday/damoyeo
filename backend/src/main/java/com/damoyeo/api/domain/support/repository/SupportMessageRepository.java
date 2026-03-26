package com.damoyeo.api.domain.support.repository;

import com.damoyeo.api.domain.support.entity.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * ============================================================================
 * 상담 메시지 Repository
 * ============================================================================
 *
 * @author damoyeo
 * @since 2025-03-16
 */
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    /**
     * 상담방의 메시지 목록 조회 (페이지네이션)
     *
     * @param supportChatId 상담방 ID
     * @param pageable 페이지 정보
     * @return 메시지 목록 (최신순)
     */
    @Query("select sm from SupportMessage sm " +
            "left join fetch sm.sender " +
            "where sm.supportChat.id = :supportChatId " +
            "order by sm.createdAt desc")
    Page<SupportMessage> findBySupportChatId(
            @Param("supportChatId") Long supportChatId,
            Pageable pageable
    );

    /**
     * 상담방의 최신 메시지 조회
     *
     * @param supportChatId 상담방 ID
     * @return 가장 최근 메시지
     */
    @Query("select sm from SupportMessage sm " +
            "left join fetch sm.sender " +
            "where sm.supportChat.id = :supportChatId " +
            "order by sm.createdAt desc " +
            "limit 1")
    Optional<SupportMessage> findLatestBySupportChatId(@Param("supportChatId") Long supportChatId);

    /**
     * 상담방의 메시지 개수 조회
     *
     * @param supportChatId 상담방 ID
     * @return 메시지 개수
     */
    long countBySupportChatId(Long supportChatId);

    /**
     * 상담방의 모든 메시지 삭제
     *
     * @param supportChatId 상담방 ID
     */
    void deleteBySupportChatId(Long supportChatId);

    /**
     * 사용자가 읽지 않은 메시지 개수 (관리자가 보낸 메시지 중)
     *
     * @param supportChatId 상담방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 읽지 않은 메시지 개수
     */
    @Query("select count(sm) from SupportMessage sm " +
            "where sm.supportChat.id = :supportChatId " +
            "and sm.isAdmin = true " +
            "and sm.id > :lastReadMessageId")
    int countUnreadByUser(
            @Param("supportChatId") Long supportChatId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );

    /**
     * 관리자가 읽지 않은 메시지 개수 (사용자가 보낸 메시지 중)
     *
     * @param supportChatId 상담방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 읽지 않은 메시지 개수
     */
    @Query("select count(sm) from SupportMessage sm " +
            "where sm.supportChat.id = :supportChatId " +
            "and sm.isAdmin = false " +
            "and sm.id > :lastReadMessageId")
    int countUnreadByAdmin(
            @Param("supportChatId") Long supportChatId,
            @Param("lastReadMessageId") Long lastReadMessageId
    );
}
