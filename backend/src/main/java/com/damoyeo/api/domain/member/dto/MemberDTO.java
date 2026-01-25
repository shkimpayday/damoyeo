package com.damoyeo.api.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * 회원 정보 DTO
 * ============================================================================
 *
 * [역할]
 * 회원 정보 데이터 전송 객체
 * Entity와 Controller 사이에서 데이터 교환에 사용
 *
 * [사용되는 곳]
 * - Service → Controller: 회원 정보 반환
 * - JWTCheckFilter: SecurityContext에 저장
 * - @AuthenticationPrincipal: 현재 로그인 사용자 정보
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private Long id;

    /** 이메일 - JWT 토큰의 subject */
    private String email;

    /** 비밀번호 - API 응답에는 포함하지 않음! */
    private String password;

    /** 닉네임 - JWT claim에 포함 */
    private String nickname;

    private String profileImage;

    private String introduction;

    /** 소셜 로그인 여부 */
    private boolean social;

    /** 권한 목록 (예: ["USER"], ["USER", "ADMIN"]) */
    @Builder.Default
    private List<String> roleNames = new ArrayList<>();

    /**
     * JWT 토큰에 담을 클레임(Claims) 생성
     *
     * password는 보안상 포함하지 않습니다.
     *
     * [사용]
     * - APILoginSuccessHandler: 로그인 성공 시 토큰 생성
     * - MemberController.modify(): 프로필 수정 후 토큰 재발급
     * - RefreshController: 토큰 갱신
     */
    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("email", email);
        claims.put("nickname", nickname);
        claims.put("profileImage", profileImage);
        claims.put("social", social);
        claims.put("roleNames", roleNames);
        return claims;
    }
}
