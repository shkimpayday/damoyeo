package com.damoyeo.api.domain.member.controller;

import com.damoyeo.api.global.exception.CustomException;
import com.damoyeo.api.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * JWT 토큰 갱신 Controller
 *
 * Access Token이 만료되었을 때 Refresh Token으로 새 토큰을 발급합니다.
 *
 * [토큰 갱신 흐름]
 * 1. Access Token 만료됨
 * 2. 프론트엔드가 API 호출 시 ERROR_ACCESS_TOKEN 응답 받음
 * 3. jwtUtil.ts의 응답 인터셉터가 감지
 * 4. 이 API를 호출하여 새 토큰 발급
 * 5. 쿠키 업데이트 후 원래 요청 재시도
 *
 * [토큰 유효 기간]
 * - Access Token: 10분
 * - Refresh Token: 24시간
 *
 * [왜 분리되어 있는가?]
 * Access Token은 API 요청에 사용되므로 유출 시 위험합니다.
 * 짧은 만료 시간으로 보안을 강화하고,
 * Refresh Token으로 주기적으로 갱신합니다.
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증 API")
public class RefreshController {

    private final JWTUtil jwtUtil;

    /**
     * 토큰 갱신
     *
     * Refresh Token을 검증하고 새로운 Access Token + Refresh Token을 발급합니다.
     *
     * [프론트엔드 요청]
     * GET /api/member/refresh
     * Authorization: Bearer {refreshToken}
     *
     * [응답]
     * {
     *   "accessToken": "새 Access Token",
     *   "refreshToken": "새 Refresh Token"
     * }
     *
     * [주의]
     * - Authorization 헤더에 Refresh Token을 전달합니다 (Access Token 아님!)
     * - Refresh Token도 만료되었으면 ERROR_REFRESH_TOKEN 에러 반환
     * - 이 경우 다시 로그인해야 합니다.
     */
    @GetMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException("Refresh token is required", HttpStatus.UNAUTHORIZED);
        }

        String refreshToken = authHeader.substring(7);

        try {
            Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

            // claims에는 email, nickname, roleNames 등이 포함되어 있음
            String newAccessToken = jwtUtil.generateAccessToken(claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(claims);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", newRefreshToken
            ));

        } catch (Exception e) {
            // Refresh Token이 만료되었거나 유효하지 않음
            // 프론트엔드는 이 에러를 받으면 로그인 페이지로 이동
            log.error("Refresh token validation failed: {}", e.getMessage());
            throw new CustomException("ERROR_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED);
        }
    }
}
