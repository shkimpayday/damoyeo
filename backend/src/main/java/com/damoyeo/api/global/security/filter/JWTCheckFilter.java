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
 * JWT 토큰 검증 필터.
 * 요청마다 Authorization 헤더를 확인하고, 유효한 토큰이 있으면 SecurityContext에 인증 정보를 저장한다.
 * SecurityConfig에서 UsernamePasswordAuthenticationFilter 앞에 등록된다.
 */
@RequiredArgsConstructor
@Slf4j
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    /**
     * 인증 검사를 건너뛸 경로를 결정한다.
     * 로그인/회원가입/공개 API 등은 토큰 없이 접근 가능해야 하므로 true를 반환한다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/member/login") ||
                path.startsWith("/api/member/signup") ||
                path.startsWith("/api/member/refresh") ||
                path.startsWith("/api/member/check/") ||
                path.startsWith("/api/member/kakao") ||
                path.startsWith("/api/email/") ||
                path.startsWith("/api/email/send") ||
                path.startsWith("/api/meetings/upcoming") ||
                path.startsWith("/api/payments/cancel") ||
                path.startsWith("/api/payments/fail") ||
                path.startsWith("/ws/") ||  // WebSocket 인증은 JWTChannelInterceptor에서 처리
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/uploads") ||
                path.equals("/favicon.ico")) {
            return true;
        }

        if ("GET".equals(method)) {
            if (path.startsWith("/api/categories")) {
                return true;
            }
            // 이벤트 상세/배너는 공개. 관리자 목록(/api/events)은 제외
            if (path.equals("/api/events/banners") ||
                    path.matches("/api/events/\\d+")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 토큰이 없어도 접근은 허용하되, 토큰이 있으면 인증 정보를 제공하는 경로.
     * 비로그인 사용자도 모임 목록/상세를 볼 수 있도록 처리한다.
     */
    private boolean isOptionalAuthPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equals(method)) {
            if (path.startsWith("/api/groups") && !path.contains("/my") && !path.contains("/manage") && !path.contains("/pending")) {
                return true;
            }
            if (path.matches("/api/meetings/\\d+")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isOptionalAuthPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            sendError(response, "ACCESS_TOKEN_REQUIRED");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Map<String, Object> claims = jwtUtil.validateToken(token);

            String email = (String) claims.get("email");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");

            @SuppressWarnings("unchecked")
            List<String> roleNames = (List<String>) claims.get("roleNames");

            MemberDTO memberDTO = MemberDTO.builder()
                    .email(email)
                    .nickname(nickname)
                    .social(social != null && social)
                    .roleNames(roleNames)
                    .build();

            // "USER" → "ROLE_USER" (Spring Security 규칙)
            List<SimpleGrantedAuthority> authorities = roleNames.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(memberDTO, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());

            if (isOptionalAuthPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            // 프론트엔드 jwtAxios 인터셉터가 ERROR_ACCESS_TOKEN을 감지하면 자동으로 토큰을 갱신한다
            sendError(response, "ERROR_ACCESS_TOKEN");
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        Gson gson = new Gson();
        writer.print(gson.toJson(Map.of("error", message)));
        writer.close();
    }
}
