package com.damoyeo.api.domain.notification.repository;

import com.damoyeo.api.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ============================================================================
 * 알림(Notification) 레포지토리
 * ============================================================================
 *
 * [역할]
 * 알림 엔티티에 대한 데이터베이스 접근을 담당합니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본 CRUD 메서드를 사용합니다.
 *
 * [상속 관계]
 * JpaRepository<Notification, Long>
 *   └─ PagingAndSortingRepository
 *       └─ CrudRepository
 *
 * [기본 제공 메서드] (JpaRepository에서 상속)
 * - save(entity): 저장/수정
 * - findById(id): ID로 조회 → Optional<Notification>
 * - findAll(): 전체 조회
 * - findAll(Pageable): 페이지네이션 조회
 * - delete(entity): 삭제
 * - count(): 총 개수
 *
 * [사용 위치]
 * - NotificationServiceImpl에서 주입받아 사용
 *
 * [쿼리 메서드 명명 규칙]
 * Spring Data JPA가 메서드명을 파싱하여 자동으로 쿼리 생성
 * 예: findByMemberIdOrderByCreatedAtDesc
 *     → WHERE member_id = ? ORDER BY created_at DESC
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 회원의 알림 목록 조회 (페이지네이션)
     *
     * [메서드명 분석]
     * findBy + MemberId + OrderBy + CreatedAt + Desc
     *   ↓        ↓          ↓         ↓         ↓
     * SELECT  WHERE      ORDER BY   정렬기준   내림차순
     *
     * [생성되는 쿼리]
     * SELECT *
     * FROM notification
     * WHERE member_id = ?
     * ORDER BY created_at DESC
     * LIMIT ?, ?
     *
     * [파라미터]
     * @param memberId 조회할 회원 ID
     * @param pageable 페이지 정보 (page, size, sort)
     *
     * [반환값]
     * Page<Notification>: 페이지네이션된 결과
     *   - content: 알림 목록
     *   - totalElements: 전체 개수
     *   - totalPages: 전체 페이지 수
     *
     * [사용 위치]
     * NotificationServiceImpl.getNotifications()
     *
     * [프론트엔드 API]
     * GET /api/notifications?page=1&size=10
     */
    Page<Notification> findByMemberIdAndIsDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    /**
     * 읽지 않은 알림 개수 조회
     *
     * [@Query 사용 이유]
     * count + 조건이 있는 복잡한 쿼리는 메서드명으로 표현하기 어려움
     * countByMemberIdAndIsReadFalse() 보다 JPQL이 명확함
     *
     * [JPQL 쿼리 분석]
     * SELECT COUNT(n)           -- 개수 조회
     * FROM Notification n       -- 엔티티명 (테이블명 아님!)
     * WHERE n.member.id = :memberId  -- 회원 ID 조건
     *   AND n.isRead = false    -- 읽지 않은 것만
     *
     * [@Param]
     * :memberId 파라미터와 Java 파라미터를 바인딩
     *
     * [반환값]
     * int: 읽지 않은 알림 개수
     *
     * [사용 위치]
     * NotificationServiceImpl.getUnreadCount()
     *
     * [프론트엔드 API]
     * GET /api/notifications/unread/count
     * 응답: { "count": 5 }
     *
     * [프론트엔드 UI]
     * 헤더의 알림 벨 아이콘 옆에 배지로 표시
     * ┌──────────┐
     * │  🔔⑤    │  ← 읽지 않은 알림 5개
     * └──────────┘
     */
    @Query("select count(n) from Notification n where n.member.id = :memberId and n.isRead = false and n.isDeleted = true")
    int countUnread(@Param("memberId") Long memberId);

    /**
     * 모든 알림 읽음 처리 (벌크 업데이트)
     *
     * [@Modifying 필수!]
     * SELECT가 아닌 UPDATE/DELETE 쿼리는 반드시 @Modifying 필요
     * 없으면 예외 발생: Not supported for DML operations
     *
     * [주의사항]
     * 벌크 업데이트는 영속성 컨텍스트를 거치지 않고 DB에 직접 실행
     * → 영속성 컨텍스트와 DB 데이터 불일치 가능
     * → 트랜잭션 종료 후 조회하면 정상 반영됨
     *
     * [JPQL 쿼리 분석]
     * UPDATE Notification n     -- 엔티티명
     * SET n.isRead = true       -- 읽음으로 변경
     * WHERE n.member.id = :memberId  -- 해당 회원의 알림만
     *
     * [생성되는 SQL]
     * UPDATE notification
     * SET is_read = true
     * WHERE member_id = ?
     *
     * [사용 위치]
     * NotificationServiceImpl.markAllAsRead()
     *
     * [프론트엔드 API]
     * PATCH /api/notifications/read-all
     *
     * [프론트엔드 UI]
     * "모두 읽음" 버튼 클릭 시 호출
     * ┌─────────────────────────────────────┐
     * │  🔔 알림                 [모두 읽음] │ ← 이 버튼
     * └─────────────────────────────────────┘
     *
     * [성능 이점]
     * 개별 업데이트: N번의 UPDATE 쿼리 실행
     * 벌크 업데이트: 1번의 UPDATE 쿼리로 N개 처리
     */
    @Modifying
    @Query("update Notification n set n.isRead = true where n.member.id = :memberId")
    void markAllAsRead(@Param("memberId") Long memberId);

    /**
     * 특정 회원에게 특정 정모에 대한 특정 타입의 알림이 존재하는지 확인
     *
     * [용도]
     * 리마인더 알림 중복 생성 방지.
     * 이미 해당 정모에 대한 리마인더 알림이 있으면 다시 생성하지 않음.
     *
     * @param memberId 회원 ID
     * @param type 알림 타입 (MEETING_REMINDER, MEETING_IMMINENT 등)
     * @param relatedId 관련 정모 ID
     * @return 존재 여부
     */
    boolean existsByMemberIdAndTypeAndRelatedId(Long memberId,
                                                 com.damoyeo.api.domain.notification.entity.NotificationType type,
                                                 Long relatedId);
}
