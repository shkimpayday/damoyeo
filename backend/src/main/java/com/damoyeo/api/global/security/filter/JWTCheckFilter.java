package com.damoyeo.api.global.security.filter;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.global.util.JWTUtil;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * JWT 토큰 검증 필터
 * ============================================================================
 *
 * [이 필터의 역할]
 * 모든 HTTP 요청에서 JWT 토큰을 검증하고, 유효하면 사용자 정보를 SecurityContext에 저장합니다.
 * 마치 건물 입구의 보안 요원처럼, 모든 요청이 이 필터를 통과합니다.
 *
 * [동작 흐름]
 * 1. 요청이 들어옴
 * 2. shouldNotFilter()로 검증이 필요 없는 경로인지 확인
 * 3. 필요 없으면 바로 통과 (로그인, 회원가입 등)
 * 4. 필요하면 Authorization 헤더에서 토큰 추출
 * 5. JWTUtil로 토큰 검증
 * 6. 검증 성공 → SecurityContext에 사용자 정보 저장 → Controller로 전달
 * 7. 검증 실패 → 401 Unauthorized 응답
 *
 * [사용되는 곳]
 * - SecurityConfig.filterChain()에서 필터 체인에 등록됨
 * - UsernamePasswordAuthenticationFilter 앞에 위치
 *
 * ▶ OncePerRequestFilter
 *   - 요청당 한 번만 실행되는 필터입니다.
 *   - 리다이렉트 등으로 같은 요청이 여러 번 처리되어도 한 번만 실행됩니다.
 *
 * ▶ @RequiredArgsConstructor
 *   - final 필드(jwtUtil)를 받는 생성자를 자동 생성합니다.
 */
@RequiredArgsConstructor
@Slf4j
public class JWTCheckFilter extends OncePerRequestFilter {

    /**
     * JWT 토큰 검증 유틸리티
     *
     * 토큰의 서명을 검증하고, 토큰에서 사용자 정보를 추출합니다.
     */
    private final JWTUtil jwtUtil;

    /**
     * 필터를 적용하지 않을 경로를 결정합니다.
     *
     * [왜 필요한가?]
     * 로그인, 회원가입 등은 토큰 없이도 접근할 수 있어야 합니다.
     * 이 메서드가 true를 반환하면 doFilterInternal()을 실행하지 않고 바로 통과합니다.
     *
     * @param request HTTP 요청 객체
     * @return true면 필터 건너뛰기, false면 필터 실행
     *
     * [건너뛰는 경로 목록]
     * - /api/member/login: 로그인
     * - /api/member/signup: 회원가입
     * - /api/member/refresh: 토큰 갱신
     * - /api/member/check/*: 이메일/닉네임 중복 확인
     * - /api/member/kakao: 카카오 로그인
     * - /swagger-ui/*, /api-docs/*: API 문서
     * - GET /api/groups/*: 모임 조회 (단, /my, /manage 제외)
     * - GET /api/categories/*: 카테고리 조회
     * - GET /api/meetings/{id}: 정모 상세 조회
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // ========== 항상 인증 없이 접근 가능한 경로 ==========
        if (path.startsWith("/api/member/login") ||
                path.startsWith("/api/member/signup") ||
                path.startsWith("/api/member/refresh") ||
                path.startsWith("/api/member/check/") ||
                path.startsWith("/api/member/kakao") ||
                path.startsWith("/api/email/") ||  // 이메일 인증 API
                path.startsWith("/api/email/send") ||
                path.startsWith("/api/meetings/upcoming") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/uploads")) {  // 정적 파일 (프로필 이미지 등)
            return true;
        }

        // ========== GET 요청 중 인증 없이 접근 가능한 경로 ==========
        // 읽기 전용 API는 누구나 볼 수 있어야 합니다.
        // 단, 토큰이 있으면 검증하여 사용자 정보를 제공합니다. (optionalAuthPaths에서 처리)
        if ("GET".equals(method)) {
            // 카테고리 목록 조회
            if (path.startsWith("/api/categories")) {
                return true;
            }
            // 이벤트/배너 조회 (공개 API)
            if (path.startsWith("/api/events")) {
                return true;
            }
        }

        return false;  // 그 외 경로는 필터 적용 (토큰 검증 필요)
    }

    /**
     * 인증이 선택적인 경로인지 확인합니다.
     * 이 경로들은 토큰이 없어도 접근 가능하지만, 토큰이 있으면 사용자 정보를 제공합니다.
     */
    private boolean isOptionalAuthPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equals(method)) {
            // 모임 목록/상세 조회 (내 모임, 관리 페이지 제외)
            if (path.startsWith("/api/groups") && !path.contains("/my") && !path.contains("/manage") && !path.contains("/pending")) {
                return true;
            }
            // 정모 상세 조회 (숫자 ID)
            if (path.matches("/api/meetings/\\d+")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 실제 JWT 토큰 검증 로직
     *
     * [처리 흐름]
     * 1. Authorization 헤더에서 토큰 추출
     * 2. "Bearer " 접두사 제거
     * 3. JWTUtil로 토큰 검증 및 사용자 정보 추출
     * 4. MemberDTO 생성
     * 5. SecurityContext에 인증 정보 저장
     * 6. 다음 필터로 요청 전달
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 다음 필터로 연결
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ========== 1. Authorization 헤더 확인 ==========
        // 프론트엔드에서 "Authorization: Bearer {토큰}" 형태로 보냅니다.
        String authHeader = request.getHeader("Authorization");

        // 헤더가 없거나 Bearer로 시작하지 않는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 선택적 인증 경로면 토큰 없이도 통과 (비로그인 사용자 접근 허용)
            if (isOptionalAuthPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            sendError(response, "ACCESS_TOKEN_REQUIRED");
            return;
        }

        // ========== 2. 토큰 추출 ==========
        // "Bearer " (7글자)를 제거하고 순수 토큰만 추출
        String token = authHeader.substring(7);

        try {
            // ========== 3. 토큰 검증 ==========
            // 서명이 유효하지 않거나 만료되었으면 예외 발생
            Map<String, Object> claims = jwtUtil.validateToken(token);

            // ========== 4. 사용자 정보 추출 ==========
            String email = (String) claims.get("email");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");

            @SuppressWarnings("unchecked")  // 타입 캐스팅 경고 무시
            List<String> roleNames = (List<String>) claims.get("roleNames");

            // ========== 5. MemberDTO 생성 ==========
            // 토큰에서 추출한 정보로 사용자 객체 생성
            MemberDTO memberDTO = MemberDTO.builder()
                    .email(email)
                    .nickname(nickname)
                    .social(social != null && social)
                    .roleNames(roleNames)
                    .build();

            // ========== 6. 권한 목록 생성 ==========
            // "USER" → "ROLE_USER" 형태로 변환 (Spring Security 규칙)
            List<SimpleGrantedAuthority> authorities = roleNames.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            // ========== 7. SecurityContext에 인증 정보 저장 ==========
            // 이후 Controller에서 @AuthenticationPrincipal로 사용자 정보를 가져올 수 있습니다.
            // 예: @AuthenticationPrincipal MemberDTO member
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(memberDTO, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ========== 8. 다음 필터로 요청 전달 ==========
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // 토큰 검증 실패 (만료, 위변조, 형식 오류 등)
            log.error("JWT validation error: {}", e.getMessage());

            // 선택적 인증 경로면 토큰 검증 실패해도 통과 (비로그인 상태로 처리)
            if (isOptionalAuthPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            sendError(response, "ERROR_ACCESS_TOKEN");
        }
    }

    /**
     * 에러 응답 전송 헬퍼 메서드
     *
     * @param response HTTP 응답 객체
     * @param message 에러 메시지 (프론트엔드에서 이 메시지로 처리 분기)
     *
     * [에러 메시지 종류]
     * - ACCESS_TOKEN_REQUIRED: Authorization 헤더가 없음
     * - ERROR_ACCESS_TOKEN: 토큰이 유효하지 않음 (만료, 위변조 등)
     *
     * [프론트엔드 처리]
     * jwtUtil.ts의 응답 인터셉터에서 ERROR_ACCESS_TOKEN을 감지하면
     * 자동으로 /api/member/refresh를 호출하여 토큰을 갱신합니다.
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Gson gson = new Gson();
        writer.print(gson.toJson(Map.of("error", message)));
        writer.close();
    }
}
