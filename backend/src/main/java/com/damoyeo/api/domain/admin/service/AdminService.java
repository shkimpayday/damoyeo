package com.damoyeo.api.domain.admin.service;

import com.damoyeo.api.domain.admin.dto.AdminGroupDTO;
import com.damoyeo.api.domain.admin.dto.AdminMemberDTO;
import com.damoyeo.api.domain.admin.dto.DashboardStatsDTO;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;

/**
 * 관리자 서비스 인터페이스
 *
 * 관리자 전용 기능에 대한 비즈니스 로직을 정의합니다.
 *
 * [기능]
 * - 대시보드 통계 조회
 * - 회원 관리 (목록, 역할 변경)
 * - 모임 관리 (목록, 상태 변경)
 */
public interface AdminService {

    // 대시보드

    /**
     * 대시보드 통계 조회
     *
     * @return 대시보드 통계 정보
     */
    DashboardStatsDTO getDashboardStats();

    // 회원 관리

    /**
     * 회원 목록 조회 (페이지네이션 + 검색)
     *
     * @param keyword 검색어 (이메일 또는 닉네임)
     * @param pageRequestDTO 페이지 요청 정보
     * @return 회원 목록 (페이지네이션)
     */
    PageResponseDTO<AdminMemberDTO> getMembers(String keyword, PageRequestDTO pageRequestDTO);

    /**
     * 회원 역할 변경 (토글 방식)
     *
     * <p>해당 역할이 있으면 제거, 없으면 추가합니다.</p>
     *
     * @param memberId 대상 회원 ID
     * @param role 역할 (ADMIN, PREMIUM)
     */
    void updateMemberRole(Long memberId, String role);

    // 모임 관리

    /**
     * 모임 목록 조회 (페이지네이션 + 검색 + 상태 필터)
     *
     * @param keyword 검색어 (모임 이름)
     * @param status 상태 필터 (ACTIVE, INACTIVE, DELETED)
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모임 목록 (페이지네이션)
     */
    PageResponseDTO<AdminGroupDTO> getGroups(String keyword, String status, PageRequestDTO pageRequestDTO);

    /**
     * 모임 상태 변경
     *
     * @param groupId 대상 모임 ID
     * @param status 상태 (ACTIVE, INACTIVE, DELETED)
     */
    void updateGroupStatus(Long groupId, String status);

    // 프리미엄 관리

    /**
     * 회원에게 프리미엄 부여 (일수 지정)
     *
     * @param memberId 대상 회원 ID
     * @param days 부여할 일수
     */
    void grantPremium(Long memberId, int days);

    /**
     * 프리미엄 기간 연장/감소
     *
     * @param memberId 대상 회원 ID
     * @param days 추가/감소할 일수 (음수면 감소)
     */
    void adjustPremiumDays(Long memberId, int days);

    /**
     * 프리미엄 즉시 해제
     *
     * @param memberId 대상 회원 ID
     */
    void revokePremium(Long memberId);
}
