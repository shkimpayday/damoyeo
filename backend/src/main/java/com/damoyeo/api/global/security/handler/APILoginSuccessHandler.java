package com.damoyeo.api.global.security.handler;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.global.security.dto.CustomUserDetails;
import com.damoyeo.api.global.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * ============================================================================
 * 로그인 성공 핸들러
 * ============================================================================
 *
 * [이 클래스의 역할]
 * Spring Security가 로그인 인증에 성공했을 때 호출됩니다.
 * Access Token과 Refresh Token을 생성하여 JSON 응답으로 반환합니다.
 *
 * [호출 시점]
 * POST /api/member/login 요청이 성공했을 때
 * Spring Security 내부에서 자동으로 이 핸들러를 호출합니다.
 *
 * [처리 흐름]
 * 1. 로그인 성공 (이메일/비밀번호 일치)
 * 2. CustomUserDetailsService가 DB에서 사용자 정보 조회
 * 3. 비밀번호 검증 성공
 * 4. 이 핸들러의 onAuthenticationSuccess() 호출
 * 5. JWT 토큰 생성 및 JSON 응답
 *
 * [사용되는 곳]
 * - SecurityConfig.filterChain()에서 .successHandler()로 등록됨
 *
 * ▶ AuthenticationSuccessHandler
 *   - Spring Security에서 제공하는 인터페이스입니다.
 *   - 인증 성공 시 실행할 로직을 정의합니다.
 */
@RequiredArgsConstructor
@Slf4j
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * JWT 토큰 생성 유틸리티
     *
     * Access Token과 Refresh Token을 생성합니다.
     */
    private final JWTUtil jwtUtil;

    /**
     * 로그인 성공 시 호출되는 메서드
     *
     * @param request HTTP 요청 (사용하지 않음)
     * @param response HTTP 응답 (JSON으로 토큰 반환)
     * @param authentication 인증 정보 (로그인한 사용자 정보 포함)
     *
     * [응답 형식]
     * {
     *   "accessToken": "eyJ...",
     *   "refreshToken": "eyJ...",
     *   "email": "user@example.com",
     *   "nickname": "사용자",
     *   "profileImage": "image.jpg",
     *   "roleNames": ["USER"],
     *   "social": false
     * }
     *
     * [프론트엔드 처리]
     * 프론트엔드에서 이 응답을 받아 쿠키에 저장합니다.
     * - memberApi.loginPost() → setCookie("member", data)
     * - 이후 모든 API 요청에 accessToken이 자동으로 포함됩니다.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("Login success: {}", authentication.getName());

        // ========== 1. 인증된 사용자 정보 가져오기 ==========
        // authentication.getPrincipal()은 CustomUserDetailsService에서 반환한 객체입니다.
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        MemberDTO member = userDetails.getMember();

        // ========== 2. JWT 토큰에 담을 정보 (Claims) 준비 ==========
        // email, nickname, roleNames, social 등
        Map<String, Object> claims = member.getClaims();

        // ========== 3. 토큰 생성 ==========
        // Access Token: 10분 유효 (API 요청에 사용)
        // Refresh Token: 24시간 유효 (Access Token 재발급에 사용)
        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        // ========== 4. 응답 데이터 구성 ==========
        // 프론트엔드에서 필요한 모든 정보를 포함합니다.
        Map<String, Object> result = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "email", member.getEmail(),
                "nickname", member.getNickname(),
                "profileImage", member.getProfileImage() != null ? member.getProfileImage() : "",
                "roleNames", member.getRoleNames(),
                "social", member.isSocial()
        );

        // ========== 5. JSON 응답 전송 ==========
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Gson gson = new Gson();
        writer.print(gson.toJson(result));
        writer.close();
    }
}
