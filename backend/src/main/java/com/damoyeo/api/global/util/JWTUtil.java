package com.damoyeo.api.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT(JSON Web Token) 유틸리티 클래스
 *
 * [JWT란?]
 * 로그인 상태를 서버가 기억하지 않고, 토큰으로 관리하는 방식입니다.
 * 토큰 안에 사용자 정보(이메일, 역할 등)가 암호화되어 들어있습니다.
 *
 * [JWT 구조] (점(.)으로 3부분 구분)
 * xxxxx.yyyyy.zzzzz
 *   │      │     └─ Signature: 토큰 위변조 검증용 서명
 *   │      └─ Payload: 사용자 정보 (email, nickname, roleNames 등)
 *   └─ Header: 토큰 타입, 알고리즘 정보
 *
 * [토큰 종류]
 * 1. Access Token (10분): API 요청 시 사용
 * 2. Refresh Token (24시간): Access Token 재발급 용도
 *
 * [사용 흐름]
 * 1. 로그인 성공 → Access Token + Refresh Token 발급
 * 2. API 요청 시 → Authorization: Bearer {Access Token} 헤더 포함
 * 3. Access Token 만료 → Refresh Token으로 새 Access Token 발급
 *
 * [사용되는 곳]
 * - APILoginSuccessHandler: 로그인 성공 시 토큰 생성
 * - JWTCheckFilter: 요청마다 토큰 검증
 * - RefreshController: 토큰 재발급
 */
@Component
public class JWTUtil {

    /**
     * JWT 서명에 사용할 비밀 키
     *
     * application.properties의 com.damoyeo.jwt.secret 값을 주입받습니다.
     * 이 키가 유출되면 토큰을 위조할 수 있으므로 절대 노출되면 안 됩니다!
     *
     * [주의] 256비트(32자) 이상이어야 합니다.
     */
    @Value("${com.damoyeo.jwt.secret}")
    private String secretKey;

    /**
     * Access Token 만료 시간 (밀리초)
     * 기본값: 600000 (10분)
     *
     * Access Token은 자주 사용되므로 짧은 만료 시간을 가집니다.
     * 만료되면 Refresh Token으로 재발급받습니다.
     */
    @Value("${com.damoyeo.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Refresh Token 만료 시간 (밀리초)
     * 기본값: 86400000 (24시간)
     *
     * Refresh Token은 Access Token 재발급 용도로만 사용됩니다.
     * Access Token보다 긴 만료 시간을 가집니다.
     */
    @Value("${com.damoyeo.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 서명에 사용할 SecretKey 객체 생성
     *
     * [왜 Keys.hmacShaKeyFor()를 사용하는가?]
     * HMAC-SHA256 알고리즘에 맞는 키 형식으로 변환합니다.
     * 단순 문자열을 그대로 사용하면 보안에 취약합니다.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 생성
     *
     * @param claims 토큰에 담을 정보 (email, nickname, roleNames, social)
     * @return 생성된 JWT 문자열
     *
     * Map<String, Object> claims = memberDTO.getClaims();
     * String accessToken = jwtUtil.generateAccessToken(claims);
     *
     * [사용되는 곳]
     * - APILoginSuccessHandler.onAuthenticationSuccess()
     * - MemberController.modify()
     * - RefreshController.refresh()
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     *
     * @param claims 토큰에 담을 정보
     * @return 생성된 JWT 문자열
     *
     * [Access Token과 다른 점]
     * - 만료 시간이 더 깁니다 (24시간 vs 10분)
     * - API 요청에는 사용되지 않고, 토큰 재발급에만 사용됩니다.
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성 (내부 메서드)
     *
     * @param claims 토큰에 담을 정보
     * @param expiration 만료 시간 (밀리초)
     * @return 생성된 JWT 문자열
     *
     * [JWT 구성 요소]
     * - Header: typ=JWT, alg=HS256 (setHeaderParam으로 설정)
     * - Payload: claims (setClaims로 설정)
     * - Signature: secretKey로 서명 (signWith로 설정)
     */
    private String generateToken(Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")                    // 토큰 타입
                .setClaims(claims)                               // 사용자 정보 (email, nickname 등)
                .setIssuedAt(new Date())                         // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 서명 (위변조 방지)
                .compact();                                      // 문자열로 변환
    }

    /**
     * JWT 토큰 검증 및 정보 추출
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰에서 추출한 사용자 정보 (email, nickname, roleNames, social)
     * @throws 토큰이 유효하지 않으면 예외 발생
     *
     * [검증 내용]
     * 1. 서명이 유효한지 (위변조 확인)
     * 2. 만료되지 않았는지
     *
     * [사용되는 곳]
     * - JWTCheckFilter.doFilterInternal(): 모든 API 요청에서 토큰 검증
     * - RefreshController.refresh(): 토큰 재발급 시 기존 토큰 검증
     */
    public Map<String, Object> validateToken(String token) {
        // 토큰 파싱 및 검증
        // 서명이 유효하지 않거나 만료되었으면 예외 발생
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // 서명 검증에 사용할 키
                .build()
                .parseClaimsJws(token)           // 토큰 파싱 (검증 포함)
                .getBody();                      // Payload 추출

        // 필요한 정보만 Map으로 반환
        return Map.of(
                "email", claims.get("email"),
                "nickname", claims.get("nickname"),
                "roleNames", claims.get("roleNames"),
                "social", claims.get("social")
        );
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token 확인할 JWT 토큰
     * @return 만료되었으면 true, 유효하면 false
     *
     * [사용되는 곳]
     * 프론트엔드에서 토큰 재발급 여부를 판단할 때 사용할 수 있습니다.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 만료 시간이 현재 시간보다 이전인지 확인
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // 파싱 실패 시 (위변조, 형식 오류 등) 만료된 것으로 처리
            return true;
        }
    }

    /**
     * Refresh Token 만료 시간 반환
     *
     * @return 만료 시간 (밀리초)
     *
     * 프론트엔드에서 쿠키 만료 시간 설정에 사용할 수 있습니다.
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
