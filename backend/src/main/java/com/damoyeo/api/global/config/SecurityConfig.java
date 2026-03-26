package com.damoyeo.api.global.config;

import com.damoyeo.api.global.security.filter.JWTCheckFilter;
import com.damoyeo.api.global.security.handler.APILoginFailHandler;
import com.damoyeo.api.global.security.handler.APILoginSuccessHandler;
import com.damoyeo.api.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ============================================================================
 * Spring Security 설정 클래스
 * ============================================================================
 *
 * [이 클래스의 역할]
 * 애플리케이션의 보안 설정을 담당합니다.
 * - 어떤 URL은 로그인 없이 접근 가능한지
 * - 어떤 URL은 로그인이 필요한지
 * - JWT 토큰 검증은 어떻게 하는지
 * - CORS(다른 도메인에서의 요청)는 어떻게 처리하는지
 *
 * [요청 처리 흐름]
 * 1. 클라이언트 요청 → JWTCheckFilter (토큰 검증)
 * 2. 토큰 유효 → Controller로 전달
 * 3. 토큰 무효/없음 → 401 Unauthorized 응답
 *
 * ▶ @Configuration: 이 클래스가 Spring 설정 클래스임을 표시
 * ▶ @EnableWebSecurity: Spring Security 활성화
 * ▶ @EnableMethodSecurity: @PreAuthorize 등 메서드 레벨 보안 활성화
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 토큰 생성/검증 유틸리티
     * 로그인 성공 시 토큰 생성, 요청마다 토큰 검증에 사용됩니다.
     */
    private final JWTUtil jwtUtil;

    /**
     * 허용할 CORS 출처 목록 (콤마 구분)
     * 환경변수 CORS_ALLOWED_ORIGINS 또는 application.properties의 cors.allowed-origins 값을 사용합니다.
     * 배포 시: CORS_ALLOWED_ORIGINS=https://damoyeo.com,https://www.damoyeo.com
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsRaw;

    /**
     * 비밀번호 암호화 방식 설정
     *
     * [왜 필요한가?]
     * DB에 비밀번호를 평문으로 저장하면 보안에 취약합니다.
     * BCrypt는 단방향 해시 알고리즘으로, 복호화가 불가능합니다.
     *
     * [사용 예시]
     * 회원가입 시: passwordEncoder.encode("1234") → "$2a$10$xxxx..."
     * 로그인 시: passwordEncoder.matches("1234", 암호화된값) → true/false
     *
     * [사용되는 곳]
     * - MemberServiceImpl.signup(): 회원가입 시 비밀번호 암호화
     * - Spring Security 내부: 로그인 시 비밀번호 비교
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     *
     * [필터 체인이란?]
     * HTTP 요청이 Controller에 도달하기 전에 거치는 보안 검사 단계들입니다.
     * 마치 공항의 보안 검색대처럼, 요청이 여러 필터를 순서대로 통과합니다.
     *
     * [설정 내용]
     * 1. CORS 설정: 프론트엔드(5173)에서의 요청 허용
     * 2. CSRF 비활성화: REST API에서는 CSRF 토큰 불필요
     * 3. 세션 비활성화: JWT 사용하므로 서버 세션 불필요 (Stateless)
     * 4. 폼 로그인 설정: /api/member/login으로 로그인 처리
     * 5. JWT 필터 추가: 모든 요청에서 JWT 토큰 검증
     * 6. URL 권한 설정: 어떤 URL이 인증 필요한지 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ========== CORS 설정 ==========
                // 다른 도메인(프론트엔드)에서의 요청을 허용합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ========== CSRF 비활성화 ==========
                // CSRF(Cross-Site Request Forgery) 보호를 비활성화합니다.
                // REST API는 세션을 사용하지 않으므로 CSRF 토큰이 불필요합니다.
                // 만약 활성화하면 모든 POST/PUT/DELETE 요청에 CSRF 토큰이 필요해집니다.
                .csrf(AbstractHttpConfigurer::disable)

                // ========== 세션 관리 ==========
                // STATELESS: 서버에 세션을 저장하지 않습니다.
                // JWT 토큰을 사용하므로 서버가 로그인 상태를 기억할 필요가 없습니다.
                // 모든 요청에서 JWT 토큰으로 사용자를 식별합니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ========== 폼 로그인 설정 ==========
                // POST /api/member/login으로 로그인 요청을 처리합니다.
                .formLogin(form -> form
                        .loginPage("/api/member/login")         // 로그인 페이지 URL (실제로는 API)
                        .loginProcessingUrl("/api/member/login") // 로그인 처리 URL
                        .usernameParameter("email")              // 아이디 파라미터명 (기본값: username)
                        .passwordParameter("pw")                 // 비밀번호 파라미터명 (기본값: password)
                        .successHandler(new APILoginSuccessHandler(jwtUtil))  // 로그인 성공 시
                        .failureHandler(new APILoginFailHandler()))           // 로그인 실패 시

                // ========== JWT 필터 추가 ==========
                // UsernamePasswordAuthenticationFilter 앞에 JWT 필터를 추가합니다.
                // 모든 요청에서 Authorization 헤더의 JWT 토큰을 검증합니다.
                .addFilterBefore(new JWTCheckFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)

                // ========== URL 권한 설정 ==========
                // 어떤 URL이 인증 없이 접근 가능한지 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로들
                        .requestMatchers("/api/member/login", "/api/member/signup",
                                "/api/member/refresh", "/api/member/check/**",
                                "/api/member/kakao").permitAll()
                        // 이메일 인증 API
                        .requestMatchers("/api/email/**").permitAll()
                        // WebSocket 엔드포인트 (JWT는 JWTChannelInterceptor에서 검증)
                        .requestMatchers("/ws/**").permitAll()
                        // Swagger API 문서
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        // 카테고리 조회 (누구나 가능)
                        .requestMatchers("/api/categories/**").permitAll()
                        // 정적 파일 (프로필 이미지 등)
                        .requestMatchers("/uploads/**").permitAll()
                        // 나머지 요청 (여기서는 모두 허용, 실제로는 JWTCheckFilter에서 처리)
                        .anyRequest().permitAll());

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     *
     * [왜 필요한가?]
     * 브라우저는 보안상 다른 도메인으로의 요청을 기본적으로 차단합니다.
     * 프론트엔드(localhost:5173)에서 백엔드(localhost:8080)로 요청하려면
     * CORS 설정이 필요합니다.
     *
     * [설정 내용]
     * - allowedOrigins: 허용할 출처 (프론트엔드 주소)
     * - allowedMethods: 허용할 HTTP 메서드
     * - allowedHeaders: 허용할 헤더 (* = 모든 헤더)
     * - allowCredentials: 쿠키 포함 요청 허용
     * - maxAge: preflight 요청 캐시 시간 (1시간)
     *
     * [CORS 에러가 나면?]
     * 브라우저 콘솔에 "CORS policy" 에러가 나면 이 설정을 확인하세요.
     * 프론트엔드 주소가 allowedOrigins에 포함되어 있어야 합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 (프론트엔드 주소)
        // 배포 시: CORS_ALLOWED_ORIGINS 환경변수 또는 cors.allowed-origins 프로퍼티로 설정
        List<String> allowedOrigins = Arrays.asList(allowedOriginsRaw.split(","));
        configuration.setAllowedOrigins(allowedOrigins);

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더 (* = 모든 헤더)
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠키를 포함한 요청 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 결과를 캐시하는 시간 (초)
        // 브라우저가 OPTIONS 요청을 매번 보내지 않도록 합니다.
        configuration.setMaxAge(3600L);

        // 모든 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
