package com.damoyeo.api.domain.group.controller;

import com.damoyeo.api.domain.group.dto.*;
import com.damoyeo.api.domain.group.service.GroupService;
import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.global.common.dto.PageRequestDTO;
import com.damoyeo.api.global.common.dto.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 모임 관리 API Controller
 * ============================================================================
 *
 * [역할]
 * 모임 CRUD, 검색, 멤버 관리 등 모든 모임 관련 API를 제공합니다.
 *
 * [엔드포인트 목록]
 *
 * === 모임 CRUD ===
 * POST   /api/groups           - 모임 생성 (인증 필요)
 * GET    /api/groups/{id}      - 모임 상세 조회
 * PUT    /api/groups/{id}      - 모임 수정 (모임장/운영진)
 * DELETE /api/groups/{id}      - 모임 삭제 (모임장만)
 *
 * === 모임 목록/검색 ===
 * GET    /api/groups              - 모임 목록 (카테고리 필터 가능)
 * GET    /api/groups/search       - 모임 검색
 * GET    /api/groups/my           - 내 모임 목록 (인증 필요)
 * GET    /api/groups/nearby       - 근처 모임 (위치 기반)
 *
 * === 멤버 관리 ===
 * POST   /api/groups/{id}/join                      - 가입 신청
 * POST   /api/groups/{id}/leave                     - 탈퇴
 * GET    /api/groups/{id}/members                   - 멤버 목록
 * GET    /api/groups/{id}/pending                   - 가입 대기 목록
 * POST   /api/groups/{id}/members/{mid}/approve     - 가입 승인
 * POST   /api/groups/{id}/members/{mid}/reject      - 가입 거절
 * DELETE /api/groups/{id}/members/{mid}             - 멤버 강퇴
 * PATCH  /api/groups/{id}/members/{mid}/role        - 역할 변경
 *
 * [어노테이션 설명]
 * @RestController: @Controller + @ResponseBody (JSON 응답)
 * @RequestMapping("/api/groups"): 기본 URL 경로
 * @Tag: Swagger API 문서 그룹핑
 */
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Group", description = "모임 관리 API")
public class GroupController {

    private final GroupService groupService;

    // ========================================================================
    // 모임 CRUD
    // ========================================================================

    /**
     * 모임 생성
     *
     * [프론트엔드 요청]
     * POST /api/groups
     * Authorization: Bearer {accessToken}
     * {
     *   "name": "강남 러닝 크루",
     *   "categoryId": 1,
     *   "maxMembers": 30,
     *   ...
     * }
     *
     * @AuthenticationPrincipal: JWT 토큰에서 추출한 사용자 정보
     * JWTCheckFilter에서 SecurityContext에 저장한 MemberDTO를 주입받습니다.
     *
     * @Valid: request의 유효성 검사 (@NotBlank 등)
     */
    @PostMapping
    @Operation(summary = "모임 생성")
    public ResponseEntity<GroupDTO> create(
            @AuthenticationPrincipal MemberDTO member,
            @Valid @ModelAttribute GroupCreateRequest request) {
        log.info("Create group: {} by {}", request.getName(), member.getEmail());
        return ResponseEntity.ok(groupService.create(member.getEmail(), request));
    }

    /**
     * 모임 상세 조회
     *
     * [프론트엔드 요청]
     * GET /api/groups/1
     * Authorization: Bearer {accessToken}  (선택)
     *
     * [특징]
     * - 비로그인 상태에서도 조회 가능
     * - 로그인 상태면 myRole, myStatus 필드에 현재 사용자와의 관계 포함
     *
     * @PathVariable: URL 경로의 {id} 값을 파라미터로 매핑
     */
    @GetMapping("/{id}")
    @Operation(summary = "모임 상세 조회")
    public ResponseEntity<GroupDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        // 비로그인 상태면 member가 null
        String email = member != null ? member.getEmail() : null;
        return ResponseEntity.ok(groupService.getById(id, email));
    }

    /**
     * 모임 정보 수정
     *
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * PUT /api/groups/1
     * Authorization: Bearer {accessToken}
     * { "name": "새 모임 이름" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "모임 수정")
    public ResponseEntity<GroupDTO> modify(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member,
            @Valid @RequestBody GroupModifyRequest request) {
        return ResponseEntity.ok(groupService.modify(id, member.getEmail(), request));
    }

    /**
     * 모임 삭제 (소프트 삭제)
     *
     * [권한] 모임장(OWNER)만 가능
     *
     * [프론트엔드 요청]
     * DELETE /api/groups/1
     * Authorization: Bearer {accessToken}
     *
     * [응답]
     * { "result": "SUCCESS" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "모임 삭제")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.delete(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    // ========================================================================
    // 모임 목록/검색
    // ========================================================================

    /**
     * 모임 목록 조회 (페이지네이션)
     *
     * [프론트엔드 요청]
     * GET /api/groups?page=1&size=10
     * GET /api/groups?categoryId=1&page=1&size=10
     *
     * @RequestParam(required = false): categoryId가 없으면 전체 조회
     * PageRequestDTO: 쿼리 파라미터에서 page, size를 자동 바인딩
     */
    @GetMapping
    @Operation(summary = "모임 목록 조회")
    public ResponseEntity<PageResponseDTO<GroupDTO>> getList(
            @RequestParam(required = false) Long categoryId,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(groupService.getList(pageRequestDTO, categoryId));
    }

    /**
     * 모임 검색
     *
     * [프론트엔드 요청]
     * GET /api/groups/search?keyword=러닝&page=1&size=10
     *
     * [검색 대상]
     * 모임 이름에 키워드가 포함된 모임 (LIKE '%keyword%')
     */
    @GetMapping("/search")
    @Operation(summary = "모임 검색")
    public ResponseEntity<PageResponseDTO<GroupDTO>> search(
            @RequestParam String keyword,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(groupService.search(keyword, pageRequestDTO));
    }

    /**
     * 내가 가입한 모임 목록
     *
     * [프론트엔드 요청]
     * GET /api/groups/my
     * Authorization: Bearer {accessToken}
     *
     * [조건]
     * 승인된 멤버(APPROVED)로 가입한 모임만 조회
     */
    @GetMapping("/my")
    @Operation(summary = "내 모임 목록")
    public ResponseEntity<List<GroupDTO>> getMyGroups(
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(groupService.getMyGroups(member.getEmail()));
    }

    /**
     * 근처 모임 조회 (위치 기반)
     *
     * [프론트엔드 요청]
     * GET /api/groups/nearby?lat=37.5&lng=127.0&radius=5
     *
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radius 검색 반경 (km), 기본값 10
     *
     * [알고리즘]
     * Haversine 공식으로 거리 계산 후 반경 내 모임 반환
     */
    @GetMapping("/nearby")
    @Operation(summary = "근처 모임 조회")
    public ResponseEntity<List<GroupDTO>> getNearbyGroups(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(groupService.getNearbyGroups(lat, lng, radius));
    }

    /**
     * 추천 모임 조회
     *
     * [프론트엔드 요청]
     * GET /api/groups/recommended
     *
     * [동작]
     * 인기 모임 또는 최신 모임을 추천
     */
    @GetMapping("/recommended")
    @Operation(summary = "추천 모임 조회")
    public ResponseEntity<List<GroupDTO>> getRecommendedGroups() {
        return ResponseEntity.ok(groupService.getRecommendedGroups());
    }

    // ========================================================================
    // 멤버 관리
    // ========================================================================

    /**
     * 모임 가입 신청
     *
     * [프론트엔드 요청]
     * POST /api/groups/1/join
     * Authorization: Bearer {accessToken}
     *
     * [동작]
     * - 공개 모임: 즉시 승인 (APPROVED)
     * - 비공개 모임: 대기 상태 (PENDING)
     */
    @PostMapping("/{id}/join")
    @Operation(summary = "모임 가입 신청")
    public ResponseEntity<Map<String, String>> join(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.join(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 모임 탈퇴
     *
     * [프론트엔드 요청]
     * POST /api/groups/1/leave
     * Authorization: Bearer {accessToken}
     *
     * [제한]
     * 모임장은 탈퇴 불가 (먼저 모임장을 위임해야 함)
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "모임 탈퇴")
    public ResponseEntity<Map<String, String>> leave(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.leave(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 모임 멤버 목록 조회
     *
     * [프론트엔드 요청]
     * GET /api/groups/1/members
     * GET /api/groups/1/members?status=APPROVED
     *
     * @param status 조회할 상태 (APPROVED, PENDING 등), 기본값 APPROVED
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "모임 멤버 목록")
    public ResponseEntity<List<GroupMemberDTO>> getMembers(
            @PathVariable Long id,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(groupService.getMembers(id, status));
    }

    /**
     * 가입 대기 목록 조회 (관리자용)
     *
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * GET /api/groups/1/pending
     * Authorization: Bearer {accessToken}
     *
     * [응답]
     * PENDING 상태인 멤버 목록
     */
    @GetMapping("/{id}/pending")
    @Operation(summary = "가입 대기 목록 (관리자용)")
    public ResponseEntity<List<GroupMemberDTO>> getPendingMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(groupService.getPendingMembers(id, member.getEmail()));
    }

    /**
     * 가입 승인
     *
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * POST /api/groups/1/members/5/approve
     * Authorization: Bearer {accessToken}
     *
     * [동작]
     * PENDING → APPROVED
     */
    @PostMapping("/{id}/members/{memberId}/approve")
    @Operation(summary = "가입 승인")
    public ResponseEntity<Map<String, String>> approveMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.approveMember(id, memberId, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 가입 거절
     *
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * POST /api/groups/1/members/5/reject
     * Authorization: Bearer {accessToken}
     *
     * [동작]
     * PENDING → REJECTED
     */
    @PostMapping("/{id}/members/{memberId}/reject")
    @Operation(summary = "가입 거절")
    public ResponseEntity<Map<String, String>> rejectMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.rejectMember(id, memberId, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 멤버 강퇴
     *
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * DELETE /api/groups/1/members/5
     * Authorization: Bearer {accessToken}
     *
     * [제한]
     * 모임장(OWNER)은 강퇴 불가
     */
    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "멤버 강퇴")
    public ResponseEntity<Map<String, String>> kickMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.kickMember(id, memberId, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 멤버 역할 변경
     *
     * [권한] 모임장(OWNER)만 가능
     *
     * [프론트엔드 요청]
     * PATCH /api/groups/1/members/5/role?role=MANAGER
     * Authorization: Bearer {accessToken}
     *
     * [용도]
     * 일반 멤버 → 운영진 승격 등
     */
    @PatchMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "멤버 역할 변경")
    public ResponseEntity<Map<String, String>> changeRole(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestParam String role,
            @AuthenticationPrincipal MemberDTO member) {
        groupService.changeRole(id, memberId, role, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }


    /**
     * 정모 불러오기
     *
     * [프론트엔드 요청]
     * GET /api/groups/1/meetings
     */
    @GetMapping("/{id}/meetings")
    @Operation(summary = "정모 리스트 불러오기")
    public ResponseEntity<List<GroupMemberDTO>> getMeetingLists(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(groupService.getPendingMembers(id, member.getEmail()));
    }

    /**
     * 정모 생성
     * [권한] 모임장(OWNER) 또는 운영진(MANAGER)
     *
     * [프론트엔드 요청]
     * POST /api/groups/1/meetings
     */
    @PostMapping("/{id}/meetings")
    @Operation(summary = "정모 생성")
    public ResponseEntity<List<GroupMemberDTO>> createMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(groupService.getPendingMembers(id, member.getEmail()));
    }
}
