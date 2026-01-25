package com.damoyeo.api.domain.member.entity;

/**
 * ============================================================================
 * 회원 권한 Enum
 * ============================================================================
 *
 * [권한 종류]
 * - USER: 일반 회원 (기본 권한)
 * - ADMIN: 관리자 (모든 기능 접근)
 *
 * [DB 저장] member_role 테이블에 문자열로 저장 ("USER", "ADMIN")
 *
 * [Spring Security] ROLE_USER, ROLE_ADMIN으로 변환되어 사용
 *
 * [Controller에서 사용]
 * @PreAuthorize("hasRole('ADMIN')")
 * public void deleteUser() { ... }
 */
public enum MemberRole {
    /** 일반 회원 - 회원가입 시 기본 부여 */
    USER,

    /** 관리자 - 시스템 관리 권한 */
    ADMIN
}
