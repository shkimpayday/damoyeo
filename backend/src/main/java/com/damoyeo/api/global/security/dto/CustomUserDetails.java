package com.damoyeo.api.global.security.dto;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security 사용자 상세 정보 클래스
 *
 * [이 클래스의 역할]
 * Spring Security가 인증에 사용하는 사용자 정보를 담는 객체입니다.
 * MemberDTO를 감싸서 Spring Security가 이해할 수 있는 형태로 변환합니다.
 *
 * [왜 필요한가?]
 * Spring Security는 사용자 정보를 UserDetails 인터페이스로 받습니다.
 * 우리의 MemberDTO를 직접 사용할 수 없으므로,
 * MemberDTO를 감싸는 래퍼 클래스가 필요합니다.
 *
 * [사용 흐름]
 * 1. 로그인 요청 (email, password)
 * 2. CustomUserDetailsService.loadUserByUsername() 호출
 * 3. DB에서 Member 조회 → MemberDTO로 변환
 * 4. MemberDTO를 이 클래스로 감싸서 반환
 * 5. Spring Security가 비밀번호 검증
 * 6. 로그인 성공 → APILoginSuccessHandler에서 사용자 정보 접근
 *
 * ▶ UserDetails 인터페이스
 *   - Spring Security의 핵심 인터페이스입니다.
 *   - 사용자명(username), 비밀번호(password), 권한(authorities) 등을 정의합니다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    /**
     * 실제 사용자 정보를 담고 있는 MemberDTO
     *
     * 이 객체에서 이메일, 비밀번호, 권한 등을 가져와
     * UserDetails 인터페이스 메서드들을 구현합니다.
     */
    private final MemberDTO member;

    /**
     * 생성자
     *
     * @param member MemberDTO 객체 (DB에서 조회한 사용자 정보)
     */
    public CustomUserDetails(MemberDTO member) {
        this.member = member;
    }

    /**
     * 사용자의 권한 목록 반환
     *
     * [Spring Security 권한 규칙]
     * 권한명 앞에 "ROLE_" 접두사를 붙여야 합니다.
     * 예: "USER" → "ROLE_USER", "ADMIN" → "ROLE_ADMIN"
     *
     * [사용되는 곳]
     * - @PreAuthorize("hasRole('ADMIN')") 등 권한 검사에서 사용
     * - SecurityContext에 저장되어 요청 전체에서 접근 가능
     *
     * @return 권한 객체 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getRoleNames().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * 비밀번호 반환
     *
     * Spring Security가 로그인 시 입력된 비밀번호와 비교합니다.
     * BCrypt로 암호화된 비밀번호입니다.
     *
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    /**
     * 사용자명(식별자) 반환
     *
     * Spring Security는 기본적으로 "username"을 사용자 식별자로 사용합니다.
     * 우리 시스템에서는 email을 사용자 식별자로 사용합니다.
     *
     * @return 이메일 (사용자 식별자)
     */
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    /**
     * 계정 만료 여부 (만료 안됨 = true)
     *
     * 계정 만료 기능을 사용하지 않으므로 항상 true 반환.
     * false를 반환하면 로그인이 거부됩니다.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;  // 만료되지 않음
    }

    /**
     * 계정 잠금 여부 (잠기지 않음 = true)
     *
     * 계정 잠금 기능을 사용하지 않으므로 항상 true 반환.
     * false를 반환하면 로그인이 거부됩니다.
     *
     * [활용 예시]
     * 비밀번호 5회 틀리면 잠금 → false 반환하도록 구현 가능
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;  // 잠기지 않음
    }

    /**
     * 자격 증명(비밀번호) 만료 여부 (만료 안됨 = true)
     *
     * 비밀번호 주기적 변경 정책을 사용하지 않으므로 항상 true 반환.
     * false를 반환하면 비밀번호 변경을 요구하는 페이지로 리다이렉트 가능.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 만료되지 않음
    }

    /**
     * 계정 활성화 여부 (활성화됨 = true)
     *
     * 계정 비활성화 기능을 사용하지 않으므로 항상 true 반환.
     * false를 반환하면 로그인이 거부됩니다.
     *
     * [활용 예시]
     * 이메일 인증 전까지 비활성화 → 이메일 인증 후 true로 변경
     * 관리자가 계정 정지 → false로 변경
     */
    @Override
    public boolean isEnabled() {
        return true;  // 활성화됨
    }
}
