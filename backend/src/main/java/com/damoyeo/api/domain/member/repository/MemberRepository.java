package com.damoyeo.api.domain.member.repository;

import com.damoyeo.api.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ============================================================================
 * 회원 Repository
 * ============================================================================
 *
 * [역할] Member 엔티티에 대한 DB 접근 담당
 *
 * [기본 메서드 (JpaRepository)]
 * - save(entity): 저장/수정
 * - findById(id): ID로 조회
 * - findAll(): 전체 조회
 * - delete(entity): 삭제
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원 조회 (권한 포함)
     *
     * @EntityGraph: memberRoleList 즉시 로딩 (N+1 방지)
     *
     * [사용] CustomUserDetailsService: 로그인 시 권한 확인
     */
    @EntityGraph(attributePaths = {"memberRoleList"})
    @Query("select m from Member m where m.email = :email")
    Optional<Member> getWithRoles(@Param("email") String email);

    /**
     * 이메일로 회원 조회 (권한 미포함)
     */
    Optional<Member> findByEmail(String email);

    /**
     * 이메일 중복 확인
     *
     * [사용] MemberService.signup(): 회원가입 전 중복 체크
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 확인
     *
     * [사용] signup(), modify(): 닉네임 중복 체크
     */
    boolean existsByNickname(String nickname);

    // ========================================================================
    // 관리자용 쿼리
    // ========================================================================

    /**
     * 특정 기간 내 가입한 회원 수
     *
     * [용도]
     * 관리자 대시보드에서 오늘 신규 가입자 수 조회
     *
     * @param start 시작 시간
     * @param end 종료 시간
     * @return 해당 기간 내 가입한 회원 수
     */
    @Query("select count(m) from Member m where m.createdAt >= :start and m.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 회원 검색 (이메일 또는 닉네임)
     *
     * [용도]
     * 관리자 페이지에서 회원 검색
     *
     * @param keyword 검색어
     * @param pageable 페이지 정보
     * @return 검색된 회원 목록
     */
    @EntityGraph(attributePaths = {"memberRoleList"})
    @Query("select m from Member m where m.email like %:keyword% or m.nickname like %:keyword%")
    Page<Member> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 전체 회원 조회 (역할 포함)
     *
     * [용도]
     * 관리자 페이지에서 회원 목록 조회
     *
     * @param pageable 페이지 정보
     * @return 회원 목록
     */
    @EntityGraph(attributePaths = {"memberRoleList"})
    @Query("select m from Member m")
    Page<Member> findAllWithRoles(Pageable pageable);
}
