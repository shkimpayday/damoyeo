package com.damoyeo.api.domain.group.repository;

import com.damoyeo.api.domain.group.entity.Group;
import com.damoyeo.api.domain.group.entity.GroupStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 모임(Group) Repository
 *
 * 모임 엔티티의 데이터베이스 접근을 담당합니다.
 *
 * [상속]
 * JpaRepository<Group, Long>을 상속받아 기본 CRUD 메서드를 자동으로 제공받습니다:
 * - save(), findById(), findAll(), delete() 등
 *
 * [커스텀 쿼리]
 * @Query 어노테이션으로 복잡한 조회 로직을 구현합니다.
 * - JPQL: 엔티티 기반 쿼리 (대부분의 메서드)
 * - Native Query: SQL 직접 사용 (위치 기반 검색)
 *
 * [N+1 문제 방지]
 * 대부분의 쿼리에서 left join fetch를 사용하여
 * 연관 엔티티를 한 번의 쿼리로 함께 조회합니다.
 *
 * - GroupServiceImpl: 모임 비즈니스 로직
 */
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * 모임 상세 조회 (연관 엔티티 포함)
     *
     * [용도]
     * 모임 상세 페이지에서 카테고리, 모임장 정보를 함께 표시할 때 사용합니다.
     *
     * [특징]
     * - left join fetch: category, owner를 한 번의 쿼리로 조회
     * - 삭제된 모임(DELETED)은 제외
     *
     * [N+1 문제 방지]
     * fetch join 없이 조회하면:
     * 1. select * from club where id = ?  (1번)
     * 2. select * from category where id = ?  (1번 - 지연 로딩 시)
     * 3. select * from member where id = ?  (1번 - 지연 로딩 시)
     * → 총 3번의 쿼리
     *
     * fetch join 사용 시:
     * → 1번의 쿼리로 모두 조회
     *
     * @param id 모임 ID
     * @return 모임 정보 (카테고리, 모임장 포함)
     *
     * 호출 위치: GroupServiceImpl.getById()
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "left join fetch g.owner " +
            "where g.id = :id and g.status <> 'DELETED'")
    Optional<Group> findByIdWithDetails(@Param("id") Long id);

    /**
     * 상태별 모임 목록 조회 (페이지네이션)
     *
     * [용도]
     * 관리자 페이지에서 상태별로 모임을 필터링할 때 사용합니다.
     *
     * @param status 조회할 상태 (ACTIVE, INACTIVE, DELETED)
     * @param pageable 페이지 정보 (page, size, sort)
     * @return 페이지네이션된 모임 목록
     *
     * 호출 위치: GroupServiceImpl.getList()
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "where g.status = :status")
    Page<Group> findAllByStatus(@Param("status") GroupStatus status, Pageable pageable);

    /**
     * 카테고리별 모임 목록 조회
     *
     * [용도]
     * 특정 카테고리의 활성 모임만 조회합니다.
     * 예: "운동/스포츠" 카테고리의 모임 목록
     *
     * [필터]
     * - ACTIVE 상태인 모임만 조회
     * - 삭제/비활성 모임은 제외
     *
     * @param categoryId 카테고리 ID
     * @param pageable 페이지 정보
     * @return 해당 카테고리의 활성 모임 목록
     *
     * 호출 위치: GroupServiceImpl.getByCategoryId()
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "where g.category.id = :categoryId and g.status = 'ACTIVE'")
    Page<Group> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 키워드 검색
     *
     * [용도]
     * 모임 이름에 특정 키워드가 포함된 모임을 검색합니다.
     *
     * [검색 방식]
     * LIKE '%keyword%' - 키워드가 어디에 있든 매칭
     * 예: "러닝" 검색 → "강남 러닝 크루", "러닝 초보 모임" 모두 매칭
     *
     * [필터]
     * - ACTIVE 상태인 모임만 검색
     *
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 키워드가 포함된 모임 목록
     *
     * 호출 위치: GroupServiceImpl.search()
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "where g.name like %:keyword% and g.status = 'ACTIVE'")
    Page<Group> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 내가 만든 모임 목록 조회
     *
     * [용도]
     * 특정 회원이 모임장으로 있는 모임 목록을 조회합니다.
     * 마이페이지의 "내가 만든 모임" 탭에서 사용됩니다.
     *
     * [필터]
     * - 삭제된 모임(DELETED)은 제외
     * - 비활성(INACTIVE) 모임은 포함 (모임장이 관리해야 하므로)
     *
     * @param memberId 회원 ID
     * @return 해당 회원이 만든 모임 목록
     *
     * 호출 위치: GroupServiceImpl.getMyOwnedGroups()
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "where g.owner.id = :memberId and g.status <> 'DELETED'")
    List<Group> findByOwnerId(@Param("memberId") Long memberId);

    /**
     * 근처 모임 검색 (위치 기반)
     *
     * [용도]
     * 사용자의 현재 위치에서 일정 거리 내의 모임을 검색합니다.
     * 모바일 앱의 "내 주변 모임" 기능에서 사용됩니다.
     *
     * [알고리즘]
     * Haversine 공식을 사용하여 두 좌표 간의 거리를 계산합니다.
     * - 지구 반지름: 6371km
     * - 입력: 사용자 위치(lat, lng), 검색 반경(radiusKm)
     * - 출력: 반경 내 모임 목록 (거리순 정렬)
     *
     * [Haversine 공식]
     * distance = 6371 * acos(
     *   cos(radians(lat1)) * cos(radians(lat2)) *
     *   cos(radians(lng2) - radians(lng1)) +
     *   sin(radians(lat1)) * sin(radians(lat2))
     * )
     *
     * [Native Query 사용 이유]
     * JPQL은 수학 함수(acos, cos, sin, radians)를 지원하지 않아
     * 데이터베이스 네이티브 쿼리를 사용합니다.
     *
     * [필터]
     * - ACTIVE 상태인 모임만 검색
     * - 위치 정보(latitude, longitude)가 있는 모임만 검색
     *
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radiusKm 검색 반경 (킬로미터)
     * @return 반경 내 모임 목록 (거리순 정렬)
     *
     * 호출 위치: GroupServiceImpl.getNearbyGroups()
     */
    @Query(value = "select g.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(g.latitude)) * " +
            "cos(radians(g.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(g.latitude)))) as distance " +
            "from club g " +
            "where g.status = 'ACTIVE' " +
            "and g.latitude is not null and g.longitude is not null " +
            "having distance <= :radius " +
            "order by distance",
            nativeQuery = true)
    List<Group> findNearbyGroups(@Param("lat") double lat,
                                 @Param("lng") double lng,
                                 @Param("radius") double radiusKm);

    /**
     * 추천 모임 조회 (최신순 상위 10개)
     *
     * [용도]
     * 메인 페이지에서 추천 모임을 표시할 때 사용합니다.
     *
     * [추천 기준]
     * 활성 상태(ACTIVE)인 모임 중 최신순으로 10개를 반환합니다.
     *
     * @param status 조회할 상태 (ACTIVE)
     * @return 최신 모임 10개
     *
     * 호출 위치: GroupServiceImpl.getRecommendedGroups()
     */
    List<Group> findTop10ByStatusOrderByCreatedAtDesc(GroupStatus status);

    /**
     * 통합 검색 - 키워드 + 카테고리 필터링
     *
     * [용도]
     * 검색 페이지에서 키워드와 카테고리를 동시에 필터링할 때 사용합니다.
     *
     * @param keyword 검색 키워드 (모임 이름)
     * @param categoryId 카테고리 ID
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "where g.name like %:keyword% " +
            "and g.category.id = :categoryId " +
            "and g.status = 'ACTIVE'")
    Page<Group> searchByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * 인기순 정렬 (멤버 수 기준) - 전체 조회
     *
     * [용도]
     * 인기순 정렬 시 멤버 수가 많은 모임을 상위에 표시합니다.
     *
     * [Native Query 사용 이유]
     * 서브쿼리로 멤버 수를 계산하여 정렬해야 하기 때문
     */
    @Query(value = "select g.* from club g " +
            "left join (select group_id, count(*) as member_count from group_member " +
            "           where status = 'APPROVED' group by group_id) gm " +
            "on g.id = gm.group_id " +
            "where g.status = 'ACTIVE' " +
            "order by coalesce(gm.member_count, 0) desc",
            countQuery = "select count(*) from club g where g.status = 'ACTIVE'",
            nativeQuery = true)
    Page<Group> findAllOrderByMemberCount(Pageable pageable);

    /**
     * 인기순 정렬 (멤버 수 기준) - 카테고리 필터
     */
    @Query(value = "select g.* from club g " +
            "left join (select group_id, count(*) as member_count from group_member " +
            "           where status = 'APPROVED' group by group_id) gm " +
            "on g.id = gm.group_id " +
            "where g.status = 'ACTIVE' and g.category_id = :categoryId " +
            "order by coalesce(gm.member_count, 0) desc",
            countQuery = "select count(*) from club g where g.status = 'ACTIVE' and g.category_id = :categoryId",
            nativeQuery = true)
    Page<Group> findByCategoryIdOrderByMemberCount(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 인기순 정렬 (멤버 수 기준) - 키워드 검색
     */
    @Query(value = "select g.* from club g " +
            "left join (select group_id, count(*) as member_count from group_member " +
            "           where status = 'APPROVED' group by group_id) gm " +
            "on g.id = gm.group_id " +
            "where g.status = 'ACTIVE' and g.name like concat('%', :keyword, '%') " +
            "order by coalesce(gm.member_count, 0) desc",
            countQuery = "select count(*) from club g where g.status = 'ACTIVE' and g.name like concat('%', :keyword, '%')",
            nativeQuery = true)
    Page<Group> searchByKeywordOrderByMemberCount(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 인기순 정렬 (멤버 수 기준) - 키워드 + 카테고리
     */
    @Query(value = "select g.* from club g " +
            "left join (select group_id, count(*) as member_count from group_member " +
            "           where status = 'APPROVED' group by group_id) gm " +
            "on g.id = gm.group_id " +
            "where g.status = 'ACTIVE' " +
            "and g.name like concat('%', :keyword, '%') " +
            "and g.category_id = :categoryId " +
            "order by coalesce(gm.member_count, 0) desc",
            countQuery = "select count(*) from club g where g.status = 'ACTIVE' " +
                    "and g.name like concat('%', :keyword, '%') and g.category_id = :categoryId",
            nativeQuery = true)
    Page<Group> searchByKeywordAndCategoryOrderByMemberCount(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    // 관리자용 쿼리

    /**
     * 상태별 모임 수 집계
     *
     * [용도]
     * 관리자 대시보드에서 활성 모임 수 조회
     *
     * @param status 조회할 상태
     * @return 해당 상태의 모임 수
     */
    long countByStatus(GroupStatus status);

    /**
     * 키워드 + 상태로 모임 검색 (관리자용)
     *
     * [용도]
     * 관리자 페이지에서 모임 검색 + 상태 필터링
     *
     * @param keyword 검색어 (모임 이름)
     * @param status 상태 필터
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "left join fetch g.owner " +
            "where g.name like %:keyword% and g.status = :status")
    Page<Group> searchByKeywordAndStatus(@Param("keyword") String keyword,
                                         @Param("status") GroupStatus status,
                                         Pageable pageable);

    /**
     * 키워드로 모임 검색 (관리자용 - 모든 상태 포함)
     *
     * [용도]
     * 관리자 페이지에서 모임 검색 (삭제된 모임 포함)
     *
     * @param keyword 검색어 (모임 이름)
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "left join fetch g.owner " +
            "where g.name like %:keyword%")
    Page<Group> searchByKeywordAdmin(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 전체 모임 조회 (관리자용 - 모든 상태 포함)
     *
     * [용도]
     * 관리자 페이지에서 모임 목록 조회 (삭제된 모임 포함)
     *
     * @param pageable 페이지 정보
     * @return 모임 목록
     */
    @Query("select g from Group g " +
            "left join fetch g.category " +
            "left join fetch g.owner")
    Page<Group> findAllAdmin(Pageable pageable);

    // 프리미엄 기능용 쿼리

    /**
     * 회원이 소유한 모임 수 집계
     *
     * [용도]
     * 모임 생성 시 일반 회원의 모임 생성 제한 확인
     * 일반 회원: 2개, 프리미엄 회원: 무제한
     *
     * @param memberId 회원 ID
     * @return 소유한 모임 수 (활성/비활성 포함, 삭제 제외)
     */
    @Query("select count(g) from Group g where g.owner.id = :memberId and g.status <> 'DELETED'")
    int countOwnedGroups(@Param("memberId") Long memberId);
}
