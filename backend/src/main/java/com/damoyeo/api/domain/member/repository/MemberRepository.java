package com.damoyeo.api.domain.member.repository;

import com.damoyeo.api.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
