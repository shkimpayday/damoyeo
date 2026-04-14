package com.damoyeo.api.domain.member.controller;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.member.dto.MemberModifyRequest;
import com.damoyeo.api.domain.member.dto.MemberSignupRequest;
import com.damoyeo.api.domain.member.dto.PublicProfileDTO;
import com.damoyeo.api.domain.member.service.MemberService;
import com.damoyeo.api.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 회원 관리 API Controller
 *
 * 회원가입, 프로필 조회/수정, 중복 확인 등 회원 관련 API를 제공합니다.
 *
 * POST /api/member/signup         - 회원가입
 * GET  /api/member/profile        - 프로필 조회 (인증 필요)
 * PUT  /api/member/modify         - 프로필 수정 (인증 필요)
 * POST /api/member/profile/image  - 프로필 이미지 업로드 (인증 필요)
 * GET  /api/member/check/email    - 이메일 중복 확인
 * GET  /api/member/check/nickname - 닉네임 중복 확인
 *
 * [참고] 로그인(POST /api/member/login)은 Spring Security가 처리합니다.
 * SecurityConfig.filterChain()에서 formLogin() 설정 참조
 *
 * ▶ @RestController: JSON 응답 반환
 * ▶ @RequestMapping("/api/member"): 기본 경로 설정
 * ▶ @Tag: Swagger API 문서 그룹핑
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    /**
     * 회원가입
     *
     * [프론트엔드 요청]
     * POST /api/member/signup
     * Content-Type: application/json
     * {
     *   "email": "user@example.com",
     *   "password": "1234",
     *   "nickname": "홍길동"
     * }
     *
     * @Valid: 요청 데이터 유효성 검사 (@NotBlank, @Email 등)
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public ResponseEntity<MemberDTO> signup(@Valid @RequestBody MemberSignupRequest request) {
        log.info("Signup request: {}", request.getEmail());
        MemberDTO member = memberService.signup(request);
        return ResponseEntity.ok(member);
    }

    /**
     * 프로필 조회
     *
     * 현재 로그인한 사용자의 프로필 정보를 반환합니다.
     *
     * @AuthenticationPrincipal: JWT 토큰에서 추출한 사용자 정보
     * JWTCheckFilter에서 SecurityContext에 저장한 MemberDTO를 주입받습니다.
     *
     * [프론트엔드 요청]
     * GET /api/member/profile
     * Authorization: Bearer {accessToken}
     */
    @GetMapping("/profile")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 회원의 프로필을 조회합니다.")
    public ResponseEntity<MemberDTO> getProfile(@AuthenticationPrincipal MemberDTO member) {
        log.info("Get profile: {}", member.getEmail());
        return ResponseEntity.ok(memberService.getByEmail(member.getEmail()));
    }

    /**
     * 프로필 수정
     *
     * [특징]
     * - 수정 후 새로운 JWT 토큰을 발급합니다.
     * - 닉네임이 변경되면 토큰에도 반영되어야 하기 때문입니다.
     *
     * [프론트엔드 처리]
     * 응답으로 받은 새 토큰으로 쿠키를 업데이트합니다.
     *
     * [프론트엔드 요청]
     * PUT /api/member/modify
     * Authorization: Bearer {accessToken}
     * {
     *   "nickname": "새닉네임",
     *   "introduction": "안녕하세요"
     * }
     */
    @PutMapping("/modify")
    @Operation(summary = "프로필 수정", description = "회원 프로필을 수정합니다.")
    public ResponseEntity<Map<String, Object>> modify(@AuthenticationPrincipal MemberDTO member, @Valid @RequestBody MemberModifyRequest request) {
        log.info("Modify member: {}", member.getEmail());

        MemberDTO modified = memberService.modify(member.getEmail(), request);

        String accessToken = jwtUtil.generateAccessToken(modified.getClaims());
        String refreshToken = jwtUtil.generateRefreshToken(modified.getClaims());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", modified.getEmail(),
                "nickname", modified.getNickname(),
                "profileImage", modified.getProfileImage() != null ? modified.getProfileImage() : "",
                "roleNames", modified.getRoleNames(),
                "social", modified.isSocial()
        ));
    }

    /**
     * 이메일 중복 확인
     *
     * 회원가입 폼에서 이메일 입력 시 실시간으로 중복 확인에 사용합니다.
     *
     * [프론트엔드 요청]
     * GET /api/member/check/email?email=user@example.com
     *
     * [응답]
     * { "exists": true } 또는 { "exists": false }
     */
    @GetMapping("/check/email")
    @Operation(summary = "이메일 중복 확인")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = memberService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 닉네임 중복 확인
     *
     * 회원가입/닉네임 변경 시 실시간 중복 확인에 사용합니다.
     *
     * [프론트엔드 요청]
     * GET /api/member/check/nickname?nickname=홍길동
     */
    @GetMapping("/check/nickname")
    @Operation(summary = "닉네임 중복 확인")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean exists = memberService.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 프로필 이미지 업로드
     *
     * 이미지 파일을 업로드하고 회원의 프로필 이미지를 업데이트합니다.
     * 기존 이미지가 있으면 삭제 후 새 이미지로 교체합니다.
     *
     * [지원 형식]
     * jpg, jpeg, png, gif, webp (최대 10MB)
     *
     * [프론트엔드 요청]
     * POST /api/member/profile/image
     * Authorization: Bearer {accessToken}
     * Content-Type: multipart/form-data
     * file: (binary)
     *
     * [응답]
     * { "imageUrl": "/uploads/profiles/uuid.jpg" }
     */
    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다.")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@AuthenticationPrincipal MemberDTO member, @RequestParam("file") MultipartFile file) {
        log.info("Upload profile image for member: {}", member.getEmail());
        String imageUrl = memberService.uploadProfileImage(member.getEmail(), file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    /**
     * 위치 정보 업데이트
     *
     * [프론트엔드 요청]
     * PUT /api/member/location
     * Authorization: Bearer {accessToken}
     * {
     *   "lat": 37.5665,
     *   "lng": 126.9780,
     *   "address": "서울특별시 중구"
     * }
     */
    @PutMapping("/location")
    @Operation(summary = "위치 정보 업데이트", description = "회원의 위치 정보를 업데이트합니다.")
    public ResponseEntity<Map<String, String>> updateLocation(
            @AuthenticationPrincipal MemberDTO member,
            @RequestBody Map<String, Object> request) {
        log.info("Update location for member: {}", member.getEmail());

        Double lat = request.get("lat") != null ? ((Number) request.get("lat")).doubleValue() : null;
        Double lng = request.get("lng") != null ? ((Number) request.get("lng")).doubleValue() : null;
        String address = (String) request.get("address");

        memberService.updateLocation(member.getEmail(), lat, lng, address);
        return ResponseEntity.ok(Map.of("message", "위치 정보가 업데이트되었습니다."));
    }

    /**
     * 공개 프로필 조회
     *
     * 다른 회원의 공개 프로필 정보를 조회합니다.
     * 이메일, 비밀번호 등 민감한 정보는 포함되지 않습니다.
     *
     * [프론트엔드 요청]
     * GET /api/member/{memberId}/profile
     * Authorization: Bearer {accessToken}
     */
    @GetMapping("/{memberId}/profile")
    @Operation(summary = "공개 프로필 조회", description = "회원의 공개 프로필을 조회합니다.")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(@PathVariable Long memberId) {
        log.info("Get public profile: memberId={}", memberId);
        return ResponseEntity.ok(memberService.getPublicProfile(memberId));
    }

    /**
     * 카카오 OAuth 로그인
     *
     * [처리 흐름]
     * 1. 프론트엔드: 카카오 인가 페이지 → 사용자 동의 → redirect_uri?code={code}로 리다이렉트
     * 2. 프론트엔드: code와 redirectUri를 이 엔드포인트로 전달
     * 3. 백엔드: 카카오 토큰 발급 → 사용자 정보 조회 → 회원 처리 → JWT 발급
     *
     * [프론트엔드 요청]
     * GET /api/member/kakao?code={code}&redirectUri={uri}
     *
     * [응답]
     * {
     *   "id": 1,
     *   "accessToken": "eyJ...",
     *   "refreshToken": "eyJ...",
     *   "email": "user@example.com",
     *   "nickname": "홍길동",
     *   "profileImage": "https://...",
     *   "roleNames": ["USER"],
     *   "social": true
     * }
     *
     * @param code        카카오 인가 코드
     * @param redirectUri 카카오 로그인 시 사용한 Redirect URI
     */
    @GetMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 OAuth 인가 코드로 로그인합니다.")
    public ResponseEntity<Map<String, Object>> kakaoLogin(
            @RequestParam String code,
            @RequestParam String redirectUri) {

        log.info("Kakao login request: redirectUri={}", redirectUri);

        MemberDTO member = memberService.kakaoLogin(code, redirectUri);

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(member.getClaims());
        String refreshToken = jwtUtil.generateRefreshToken(member.getClaims());

        // null 값을 허용하기 위해 HashMap 사용 (Map.of는 null 불가)
        Map<String, Object> result = new HashMap<>();
        result.put("id", member.getId());
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("email", member.getEmail());
        result.put("nickname", member.getNickname());
        result.put("profileImage", member.getProfileImage() != null ? member.getProfileImage() : "");
        result.put("roleNames", member.getRoleNames());
        result.put("social", member.isSocial());

        return ResponseEntity.ok(result);
    }
}
