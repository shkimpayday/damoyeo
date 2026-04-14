package com.damoyeo.api.domain.meeting.controller;

import com.damoyeo.api.domain.meeting.dto.*;
import com.damoyeo.api.domain.meeting.service.MeetingService;
import com.damoyeo.api.domain.member.dto.MemberDTO;
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
 * 정모(Meeting) REST API 컨트롤러
 *
 * 정모 관련 HTTP 요청을 처리하는 REST API 엔드포인트를 제공합니다.
 *
 * [기본 URL]
 * /api/meetings
 *
 * [API 목록]
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │ Method │ URL                     │ 설명                │ 인증 │
 * ├────────────────────────────────────────────────────────────────────────┤
 * │ POST   │ /api/meetings           │ 정모 생성           │ O   │
 * │ GET    │ /api/meetings/{id}      │ 정모 상세 조회      │ △   │
 * │ PUT    │ /api/meetings/{id}      │ 정모 수정           │ O   │
 * │ DELETE │ /api/meetings/{id}      │ 정모 취소           │ O   │
 * │ GET    │ /api/meetings/group/{id}│ 모임의 정모 목록    │ △   │
 * │ GET    │ /api/meetings/upcoming  │ 다가오는 정모 목록  │ X   │
 * │ GET    │ /api/meetings/my        │ 내 정모 목록        │ O   │
 * │ POST   │ /api/meetings/{id}/attend│ 참석 등록          │ O   │
 * │ DELETE │ /api/meetings/{id}/attend│ 참석 취소          │ O   │
 * │ GET    │ /api/meetings/{id}/attendees│ 참석자 목록     │ X   │
 * └────────────────────────────────────────────────────────────────────────┘
 * (O: 필수, △: 선택(myStatus용), X: 불필요)
 *
 * [Swagger 문서]
 * @Tag로 API 그룹화, @Operation으로 각 API 설명
 * http://localhost:8080/swagger-ui.html 에서 확인 가능
 *
 * [사용 어노테이션 설명]
 * - @RestController: @Controller + @ResponseBody (JSON 응답)
 * - @RequestMapping: 기본 URL 경로 설정
 * - @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * - @Slf4j: 로깅
 * - @AuthenticationPrincipal: JWT에서 추출한 사용자 정보 주입
 * - @Valid: Bean Validation 수행
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meeting", description = "정모 관리 API")
public class MeetingController {

    /** 정모 서비스 (비즈니스 로직 처리) */
    private final MeetingService meetingService;

    // CRUD 기본 기능

    /**
     * 정모 생성
     *
     * [처리 흐름]
     * 1. JWT에서 사용자 정보 추출 (@AuthenticationPrincipal)
     * 2. 요청 데이터 검증 (@Valid)
     * 3. MeetingService.create() 호출
     * 4. 생성된 정모 정보 반환
     *
     * [권한]
     * 해당 모임의 승인된 멤버만 가능
     *
     * [프론트엔드 요청]
     * POST /api/meetings
     * Authorization: Bearer {accessToken}
     * Content-Type: application/json
     * {
     *   "groupId": 1,
     *   "title": "5월 첫째 주 러닝",
     *   "meetingDate": "2024-05-04T10:00:00",
     *   ...
     * }
     *
     * @param member JWT에서 추출한 사용자 정보
     * @param request 정모 생성 요청 데이터
     * @return 생성된 정모 정보
     */
    @PostMapping
    @Operation(summary = "정모 생성")
    public ResponseEntity<MeetingDTO> create(
            @AuthenticationPrincipal MemberDTO member,
            @Valid @RequestBody MeetingCreateRequest request) {
        log.info("Create meeting: {} by {}", request.getTitle(), member.getEmail());
        return ResponseEntity.ok(meetingService.create(member.getEmail(), request));
    }

    /**
     * 정모 상세 조회
     *
     * [특징]
     * - 로그인 여부와 관계없이 조회 가능
     * - 로그인한 경우: myStatus (현재 사용자의 참석 상태) 포함
     * - 비로그인: myStatus = null
     *
     * [프론트엔드 요청]
     * GET /api/meetings/123
     *
     * @param id 정모 ID (path variable)
     * @param member JWT 사용자 정보 (선택, null 가능)
     * @return 정모 상세 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "정모 상세 조회")
    public ResponseEntity<MeetingDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        // 비로그인 사용자는 member가 null
        String email = member != null ? member.getEmail() : null;
        return ResponseEntity.ok(meetingService.getById(id, email));
    }

    /**
     * 정모 수정
     *
     * [권한]
     * - 정모 생성자
     * - 또는 모임 관리자 (OWNER, MANAGER)
     *
     * [Partial Update]
     * null인 필드는 무시하고 기존 값 유지
     *
     * [프론트엔드 요청]
     * PUT /api/meetings/123
     * Authorization: Bearer {accessToken}
     * { "title": "새 제목", "location": "새 장소" }
     *
     * @param id 정모 ID
     * @param member JWT 사용자 정보
     * @param request 수정할 정보
     * @return 수정된 정모 정보
     */
    @PutMapping("/{id}")
    @Operation(summary = "정모 수정")
    public ResponseEntity<MeetingDTO> modify(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member,
            @Valid @RequestBody MeetingModifyRequest request) {
        return ResponseEntity.ok(meetingService.modify(id, member.getEmail(), request));
    }

    /**
     * 정모 취소 (소프트 삭제)
     *
     * [권한]
     * - 정모 생성자
     * - 또는 모임 관리자 (OWNER, MANAGER)
     *
     * [동작]
     * 실제 삭제가 아닌 status를 CANCELLED로 변경
     *
     * [프론트엔드 요청]
     * DELETE /api/meetings/123
     * Authorization: Bearer {accessToken}
     *
     * @param id 정모 ID
     * @param member JWT 사용자 정보
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "정모 취소")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        meetingService.delete(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    // 목록 조회

    /**
     * 특정 모임의 정모 목록 조회 (전체)
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - 취소된 정모 제외
     * - 정모 날짜 오름차순
     *
     * [프론트엔드 요청]
     * GET /api/meetings/group/123
     *
     * @param groupId 모임 ID
     * @param member JWT 사용자 정보 (선택)
     * @return 정모 목록
     */
    @GetMapping("/group/{groupId}")
    @Operation(summary = "모임의 정모 목록 (전체)")
    public ResponseEntity<List<MeetingDTO>> getByGroupId(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDTO member) {
        String email = member != null ? member.getEmail() : null;
        return ResponseEntity.ok(meetingService.getByGroupId(groupId, email));
    }

    /**
     * 특정 모임의 예정된 정모 목록 조회
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - SCHEDULED, ONGOING 상태만
     * - 정모 날짜 오름차순
     *
     * [프론트엔드 요청]
     * GET /api/meetings/group/123/upcoming
     *
     * @param groupId 모임 ID
     * @param member JWT 사용자 정보 (선택)
     * @return 예정된 정모 목록
     */
    @GetMapping("/group/{groupId}/upcoming")
    @Operation(summary = "모임의 예정된 정모 목록")
    public ResponseEntity<List<MeetingDTO>> getUpcomingByGroupId(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDTO member) {
        String email = member != null ? member.getEmail() : null;
        return ResponseEntity.ok(meetingService.getUpcomingByGroupId(groupId, email));
    }

    /**
     * 특정 모임의 지난 정모 목록 조회
     *
     * [조건]
     * - 해당 모임에 속한 정모만
     * - COMPLETED 상태만
     * - 정모 날짜 내림차순 (최근 완료된 정모가 먼저)
     *
     * [프론트엔드 요청]
     * GET /api/meetings/group/123/past
     *
     * @param groupId 모임 ID
     * @param member JWT 사용자 정보 (선택)
     * @return 지난 정모 목록
     */
    @GetMapping("/group/{groupId}/past")
    @Operation(summary = "모임의 지난 정모 목록")
    public ResponseEntity<List<MeetingDTO>> getPastByGroupId(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDTO member) {
        String email = member != null ? member.getEmail() : null;
        return ResponseEntity.ok(meetingService.getPastByGroupId(groupId, email));
    }

    /**
     * 다가오는 정모 목록 조회
     *
     * [조건]
     * - 현재 시간 이후의 정모
     * - SCHEDULED 상태만
     * - 정모 날짜 오름차순
     *
     * [특징]
     * 인증 불필요 (비로그인도 조회 가능)
     *
     * [프론트엔드 요청]
     * GET /api/meetings/upcoming
     *
     * @return 다가오는 정모 목록
     */
    @GetMapping("/upcoming")
    @Operation(summary = "다가오는 정모 목록")
    public ResponseEntity<List<MeetingDTO>> getUpcomingMeetings() {
        return ResponseEntity.ok(meetingService.getUpcomingMeetings());
    }

    /**
     * 내가 참석 예정인 정모 목록
     *
     * [조건]
     * - 내가 ATTENDING 상태로 등록한 정모
     * - 현재 시간 이후
     * - 정모 날짜 오름차순
     *
     * [프론트엔드 요청]
     * GET /api/meetings/my
     * Authorization: Bearer {accessToken}
     *
     * @param member JWT 사용자 정보
     * @return 내 정모 목록
     */
    @GetMapping("/my")
    @Operation(summary = "내 정모 목록")
    public ResponseEntity<List<MeetingDTO>> getMyMeetings(
            @AuthenticationPrincipal MemberDTO member) {
        return ResponseEntity.ok(meetingService.getMyMeetings(member.getEmail()));
    }

    // 참석 관리

    /**
     * 정모 참석 등록
     *
     * [처리 흐름]
     * 1. 모임 멤버 확인
     * 2. 기존 등록 여부 확인 (있으면 상태만 변경)
     * 3. 정원 확인 (ATTENDING일 때만)
     * 4. 참석 정보 저장
     *
     * [프론트엔드 요청]
     * POST /api/meetings/123/attend?status=ATTENDING
     * Authorization: Bearer {accessToken}
     *
     * status 파라미터:
     * - ATTENDING (기본값): 참석 예정
     * - MAYBE: 미정
     * - NOT_ATTENDING: 불참
     *
     * @param id 정모 ID
     * @param member JWT 사용자 정보
     * @param status 참석 상태 (기본: ATTENDING)
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @PostMapping("/{id}/attend")
    @Operation(summary = "정모 참석 등록")
    public ResponseEntity<Map<String, String>> attend(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member,
            @RequestParam(required = false, defaultValue = "ATTENDING") String status) {
        meetingService.attend(id, member.getEmail(), status);
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 정모 참석 취소
     *
     * [동작]
     * 참석 정보를 완전히 삭제합니다.
     *
     * [프론트엔드 요청]
     * DELETE /api/meetings/123/attend
     * Authorization: Bearer {accessToken}
     *
     * @param id 정모 ID
     * @param member JWT 사용자 정보
     * @return 성공 결과 { "result": "SUCCESS" }
     */
    @DeleteMapping("/{id}/attend")
    @Operation(summary = "정모 참석 취소")
    public ResponseEntity<Map<String, String>> cancelAttend(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberDTO member) {
        meetingService.cancelAttend(id, member.getEmail());
        return ResponseEntity.ok(Map.of("result", "SUCCESS"));
    }

    /**
     * 정모 참석자 목록 조회
     *
     * [특징]
     * - 모든 상태의 참석자 포함 (ATTENDING, MAYBE, NOT_ATTENDING)
     * - 프론트엔드에서 상태별로 그룹핑 가능
     * - 인증 불필요
     *
     * [프론트엔드 요청]
     * GET /api/meetings/123/attendees
     *
     * @param id 정모 ID
     * @return 참석자 목록
     */
    @GetMapping("/{id}/attendees")
    @Operation(summary = "정모 참석자 목록")
    public ResponseEntity<List<MeetingAttendeeDTO>> getAttendees(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getAttendees(id));
    }
}
