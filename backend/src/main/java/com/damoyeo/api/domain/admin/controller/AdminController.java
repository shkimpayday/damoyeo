package com.damoyeo.api.domain.admin.controller;

import com.damoyeo.api.domain.admin.dto.AdminGroupDTO;
import com.damoyeo.api.domain.admin.dto.AdminMemberDTO;
import com.damoyeo.api.domain.admin.dto.DashboardStatsDTO;
import com.damoyeo.api.domain.admin.service.AdminService;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ============================================================================
 * 관리자 API Controller
 * ============================================================================
 *
 * [역할]
 * 관리자 전용 기능을 제공합니다.
 * - 대시보드 통계
 * - 회원 관리 (목록, 역할 변경)
 * - 모임 관리 (목록, 상태 변경)
 * - 이벤트 관리 (목록, CRUD)
 *
 * [권한]
 * 모든 엔드포인트는 ADMIN 권한이 필요합니다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "관리자 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ========================================================================
    // 대시보드
    // ========================================================================

    /**
     * 대시보드 통계 조회
     *
     * [응답 데이터]
     * - 전체 회원 수
     * - 전체 모임 수
     * - 전체 정모 수
     * - 오늘 신규 가입자 수
     * - 활성 모임 수
     * - 예정된 정모 수
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "대시보드 통계 조회")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ========================================================================
    // 회원 관리
    // ========================================================================

    /**
     * 회원 목록 조회 (페이지네이션 + 검색)
     *
     * @param keyword 검색어 (이메일 또는 닉네임)
     */
    @GetMapping("/members")
    @Operation(summary = "회원 목록 조회")
    public ResponseEntity<PageResponseDTO<AdminMemberDTO>> getMembers(
            @RequestParam(required = false) String keyword,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(adminService.getMembers(keyword, pageRequestDTO));
    }

    /**
     * 회원 역할 변경
     *
     * [가능한 역할]
     * - USER: 일반 회원
     * - ADMIN: 관리자
     * - PREMIUM: 프리미엄 회원
     *
     * @param memberId 대상 회원 ID
     * @param request 역할 정보 { "role": "ADMIN" }
     */
    @PatchMapping("/members/{memberId}/role")
    @Operation(summary = "회원 역할 변경")
    public ResponseEntity<Map<String, String>> updateMemberRole(
            @PathVariable Long memberId,
            @RequestBody Map<String, String> request) {
        String role = request.get("role");
        adminService.updateMemberRole(memberId, role);
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    // ========================================================================
    // 모임 관리
    // ========================================================================

    /**
     * 모임 목록 조회 (페이지네이션 + 검색 + 상태 필터)
     *
     * @param keyword 검색어 (모임 이름)
     * @param status 상태 필터 (ACTIVE, INACTIVE, DELETED)
     */
    @GetMapping("/groups")
    @Operation(summary = "모임 목록 조회 (관리자)")
    public ResponseEntity<PageResponseDTO<AdminGroupDTO>> getGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(adminService.getGroups(keyword, status, pageRequestDTO));
    }

    /**
     * 모임 상태 변경
     *
     * [가능한 상태]
     * - ACTIVE: 활성
     * - INACTIVE: 비활성
     * - DELETED: 삭제됨
     *
     * @param groupId 대상 모임 ID
     * @param request 상태 정보 { "status": "INACTIVE" }
     */
    @PatchMapping("/groups/{groupId}/status")
    @Operation(summary = "모임 상태 변경")
    public ResponseEntity<Map<String, String>> updateGroupStatus(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        adminService.updateGroupStatus(groupId, status);
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    // ========================================================================
    // 프리미엄 관리
    // ========================================================================

    /**
     * 프리미엄 부여
     *
     * @param memberId 대상 회원 ID
     * @param request 부여할 일수 { "days": 30 }
     */
    @PostMapping("/members/{memberId}/premium")
    @Operation(summary = "프리미엄 부여", description = "회원에게 프리미엄을 부여합니다. 기존 프리미엄이 있으면 연장됩니다.")
    public ResponseEntity<Map<String, String>> grantPremium(
            @PathVariable Long memberId,
            @RequestBody Map<String, Integer> request) {
        int days = request.getOrDefault("days", 30);
        adminService.grantPremium(memberId, days);
        log.info("프리미엄 부여: memberId={}, days={}", memberId, days);
        return ResponseEntity.ok(Map.of(
                "result", "SUCCESS",
                "message", days + "일 프리미엄이 부여되었습니다."
        ));
    }

    /**
     * 프리미엄 기간 조정
     *
     * @param memberId 대상 회원 ID
     * @param request 조정할 일수 { "days": 7 } (음수면 감소)
     */
    @PatchMapping("/members/{memberId}/premium/adjust")
    @Operation(summary = "프리미엄 기간 조정", description = "프리미엄 기간을 연장하거나 감소합니다. 음수면 감소됩니다.")
    public ResponseEntity<Map<String, String>> adjustPremiumDays(
            @PathVariable Long memberId,
            @RequestBody Map<String, Integer> request) {
        int days = request.getOrDefault("days", 0);
        adminService.adjustPremiumDays(memberId, days);
        log.info("프리미엄 기간 조정: memberId={}, days={}", memberId, days);
        return ResponseEntity.ok(Map.of(
                "result", "SUCCESS",
                "message", (days > 0 ? "+" : "") + days + "일 조정되었습니다."
        ));
    }

    /**
     * 프리미엄 해제
     *
     * @param memberId 대상 회원 ID
     */
    @DeleteMapping("/members/{memberId}/premium")
    @Operation(summary = "프리미엄 해제", description = "프리미엄을 즉시 해제합니다.")
    public ResponseEntity<Map<String, String>> revokePremium(@PathVariable Long memberId) {
        adminService.revokePremium(memberId);
        log.info("프리미엄 해제: memberId={}", memberId);
        return ResponseEntity.ok(Map.of(
                "result", "SUCCESS",
                "message", "프리미엄이 해제되었습니다."
        ));
    }
}
