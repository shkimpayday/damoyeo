package com.damoyeo.api.global.security;

import com.damoyeo.api.domain.member.dto.MemberDTO;
import com.damoyeo.api.domain.member.entity.Member;
import com.damoyeo.api.domain.member.repository.MemberRepository;
import com.damoyeo.api.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * 사용자 인증 정보 조회 서비스
 * ============================================================================
 *
 * [이 클래스의 역할]
 * 로그인 시 이메일로 DB에서 사용자 정보를 조회합니다.
 * Spring Security가 로그인 처리 중 자동으로 이 서비스를 호출합니다.
 *
 * [호출 흐름]
 * 1. 사용자가 로그인 폼 제출 (email, password)
 * 2. Spring Security가 이 서비스의 loadUserByUsername(email) 호출
 * 3. DB에서 해당 이메일의 Member 조회
 * 4. MemberDTO로 변환 후 CustomUserDetails로 감싸서 반환
 * 5. Spring Security가 반환된 비밀번호와 입력된 비밀번호 비교
 * 6. 일치 → 로그인 성공, 불일치 → 로그인 실패
 *
 * [사용되는 곳]
 * - Spring Security 내부에서 자동 호출됨
 * - 직접 호출하는 코드는 없음
 *
 * ▶ UserDetailsService 인터페이스
 *   - Spring Security의 핵심 인터페이스입니다.
 *   - loadUserByUsername() 하나만 구현하면 됩니다.
 *   - "username"이라고 되어 있지만, 우리는 email을 사용합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * 회원 정보 조회 Repository
     *
     * DB에서 Member 엔티티를 조회합니다.
     */
    private final MemberRepository memberRepository;

    /**
     * 이메일로 사용자 정보 조회
     *
     * Spring Security가 로그인 처리 시 자동으로 호출합니다.
     * 메서드 이름이 loadUserByUsername이지만, 실제로는 email을 받습니다.
     * (SecurityConfig에서 usernameParameter("email")로 설정했기 때문)
     *
     * @param email 로그인 폼에서 입력한 이메일
     * @return 사용자 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 이메일에 해당하는 사용자가 없을 때
     *
     * [반환 객체 설명]
     * CustomUserDetails는 MemberDTO를 감싼 객체입니다.
     * Spring Security가 이 객체에서:
     * - getPassword(): 암호화된 비밀번호 (BCrypt)
     * - getAuthorities(): 권한 목록 (ROLE_USER 등)
     * 을 가져와서 인증에 사용합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}", email);

        // ========== 1. DB에서 Member 조회 ==========
        // getWithRoles(): 권한 정보(MemberRole)를 함께 조회하는 메서드
        Member member = memberRepository.getWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // ========== 2. Member 엔티티 → MemberDTO 변환 ==========
        // 엔티티를 직접 사용하지 않고 DTO로 변환합니다.
        // 이유: 엔티티는 JPA 관리 대상이라 예상치 못한 문제가 생길 수 있음
        MemberDTO memberDTO = MemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())              // 비밀번호 검증에 필요
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .social(member.isSocial())
                .roleNames(member.getMemberRoleList().stream()
                        .map(Enum::name)                    // MemberRole.USER → "USER"
                        .collect(Collectors.toList()))
                .build();

        // ========== 3. CustomUserDetails로 감싸서 반환 ==========
        // Spring Security가 이해할 수 있는 형태로 변환
        return new CustomUserDetails(memberDTO);
    }
}
